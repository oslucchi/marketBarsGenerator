package it.l_soft.barsGenerator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

public class MarketSimulator {
	final Logger log = Logger.getLogger(this.getClass());
	final int LOW = 0;
	final int HIGH = 1;

	private ApplicationProperties props;
	private MarketBar previousBar, currentBar;
	
	private boolean trendFollowing = false;
	private int innerTrendSign = 0;
	private int barsFollowingTrend;
	private int directionToGo = 1;
	private double totIntradayVol = 0;
	private Trend trendCur;
	private Block block;
	private boolean forceRebound = false;
	private boolean approachingEndOfTrend = false;
	private boolean endOfTrendDutiesDone = false;

	public MarketSimulator()
	{
		props = ApplicationProperties.getInstance();
		barsFollowingTrend = 0;
	}

	private void setDirectionToGo(double limitUp, double limitDown, double distanceUp, double distanceDown)
	{
		forceRebound = false;
		
		// Check the distance between the current open price and the higher / lower 
		// based on the direction of the trend
		switch(trendCur.direction)
		{
		case Trend.LATERAL:
			// this is the case of a lateral trend where the price should never cross the higher and the lower values
			// at each step
			directionToGo = (currentBar.getOpen() > trendCur.targetPrice ? -1 : 1);
			
			if (currentBar.getOpen() > trendCur.higher)
			{
				forceRebound = true;
			}
			else if (currentBar.getOpen() < trendCur.lower)
			{
				forceRebound = true;
			}
			
			log.debug("Current open " + currentBar.getOpen() + " target " + trendCur.targetPrice + " " +
					  "higher " + trendCur.higher + " " +
					  "lower " + trendCur.lower + " " +
					  "direction to go " + (directionToGo == 1 ? "LONG" : "SHORT") + " " +
					  "force rebound " + forceRebound);
			break;

		case Trend.LONG:	
			if (currentBar.getOpen() > trendCur.higher)
			{
				// long, the close price should never go over trend target
				// we found to be at a *dangerous* distance of less of an intrabar volatility from the higher
				// let's revert the direction to go down
				directionToGo = -1;
				forceRebound = true;
				log.trace("price would go over the target price during a trend long, rebouncing");
			}
			else
			{
				directionToGo = trendCur.direction;
			}
			break;
			
		case Trend.SHORT:
			if (currentBar.getOpen() < trendCur.lower)
			{
				// short, the close price should never go below trend open 
				// same as above but on the other direction
				// let's revert the direction to go down
				directionToGo = 1;
				forceRebound = true;
				log.trace("price would go below the open price during a trend short, rebouncing");
			}
			else
			{
				directionToGo = trendCur.direction;
			}
		}
	}
	
	/*
	 * evaluates if the current bar is part of an already running inner trend
	 * If yes evaluate if it is reaching the higher / lower limits of the trend and revert if necessary
	 */
	private void evaluateTrendEnter()
	{
		double limitUp = block.getMaxIntrabarVol() * trendCur.higher;
		double limitDown = block.getMaxIntrabarVol() * trendCur.lower;
		double distanceUp = trendCur.higher - currentBar.getOpen();
		double distanceDown = currentBar.getOpen() - trendCur.lower;
		
		setDirectionToGo(limitUp, limitDown, distanceUp, distanceDown);

		// directionToGo suggests now where the next bar should be placed
		// we can also use distanceUp and Down to amplify the magnitude of the intrabarVol
		if (!approachingEndOfTrend)
		{
			// not yet in the endOfTrend period, no need to strictly focus to the target
			if (!trendFollowing)
			{
				// not in an inner trend yet. Consider the chance to enter.
				double random = props.getRand().nextDouble();
				if ((random > 1 - props.getProbabilityToEnterTrend()) || forceRebound)
				{
					trendFollowing = true;
					log.trace("Starting and inner trend at bar " + trendCur.currentBar);
					
					// randomly define how many bars will be in trend using data from the trend configuration 
					barsFollowingTrend = props.getRand().nextInt(trendCur.maxBarPerTrend - trendCur.minBarPerTrend) +
										 trendCur.minBarPerTrend;
					
					// if a rebound was found necessary, the direction should always remain that 
					// identified above and recorded in directionToGo.
					// Otherwise the trend should have a chance to towards the target price increasing with the 
					// distance of the current price from it
					if (forceRebound)
					{
						innerTrendSign = (int) directionToGo;
						log.trace("The new innertrend will be generated to perform a rebound");
					}
					else
					{
						// let's start with the trend direction having a 50% chance to go short and 50% to go long
						double trendDirection = random -.5;
						switch(directionToGo)
						{
						case Trend.LONG:
							trendDirection += (Math.abs(trendCur.deltaPoints - distanceUp) / trendCur.deltaPoints) / 2;
							break;
						case Trend.SHORT:
							trendDirection += (Math.abs(trendCur.deltaPoints - distanceDown) / trendCur.deltaPoints) / 2;
							break;
						}
						if (trendDirection < 0)
						{
							innerTrendSign = -1;
						}
						else
						{
							innerTrendSign = 1;
						}
					}
					InnerTrend iTrend = new InnerTrend(innerTrendSign, barsFollowingTrend);
					iTrend.openPrice = currentBar.getOpen();
					iTrend.barStart = trendCur.currentBar;
					trendCur.innerTrends.add(iTrend);
				}
				else
				{
					innerTrendSign = 0;
					barsFollowingTrend = 0;
				}
			}
			else
			{
				// already in an inner trend, evaluate the need if the required number of bars 
				// have been generated or the need to rebounce
				if (--barsFollowingTrend <= 0)
				{
					trendFollowing = false;
					innerTrendSign = 0;
					barsFollowingTrend = 0;
					InnerTrend iTrend = trendCur.innerTrends.get(trendCur.innerTrends.size() - 1);
					iTrend.closePrice = previousBar.getClose();
					iTrend.barEnd = trendCur.currentBar;
					log.debug("An Inner " + (innerTrendSign == 1 ? "LONG" : "SHORT") + " " +
							  (iTrend.rebound ? "REBOUND" : "") + " trend is over. " +
							  "Running on bars from " + iTrend.barStart + " to " + iTrend.barEnd + ". " +
							  "Opened at " + iTrend.openPrice + " - closed at " + iTrend.closePrice);
				}
				else if (forceRebound)
				{
					// Close the previous trend in the trend array list and start a new wone bouncing the sign
					InnerTrend iTrend = trendCur.innerTrends.get(trendCur.innerTrends.size() - 1);
					iTrend.numOfBars = iTrend.numOfBars - barsFollowingTrend + 1;
					iTrend.closePrice = previousBar.getClose();
					iTrend.barEnd = trendCur.currentBar - 1;
					log.debug("A rebound was called during an inner " + 
							  (innerTrendSign == 1 ? "LONG" : "SHORT") + " trend, close it. " +
							  "Running on bars from " + iTrend.barStart + " to " + iTrend.barEnd + ". " +
							  "Opened at " + iTrend.openPrice + " - closed at " + iTrend.closePrice);
					
					iTrend = new InnerTrend(innerTrendSign, barsFollowingTrend + 1);
					iTrend.openPrice = currentBar.getOpen();
					iTrend.barStart = trendCur.currentBar;
					iTrend.rebound = true;
					trendCur.innerTrends.add(iTrend);
					innerTrendSign = (int) directionToGo;
				}
			}
		}
		else if (!endOfTrendDutiesDone)
		{
			InnerTrend iTrend;
			if (trendCur.innerTrends.size() > 0)
			{
				iTrend = trendCur.innerTrends.get(trendCur.innerTrends.size() - 1);
			}
			else
			{
				iTrend = new InnerTrend(innerTrendSign, barsFollowingTrend);
				iTrend.openPrice = currentBar.getOpen();
				iTrend.barStart = trendCur.currentBar;
			}

			// we are now approaching the end. if we are already following a trend, bounce it if 
			// it is going in the opposite direction
			// it should never reenter this code again till the end
			if (trendFollowing && (innerTrendSign != directionToGo))
			{
				log.debug("Entered the approachingEnd phase while in trend. Forcing it to stop");
				iTrend.numOfBars = iTrend.numOfBars - barsFollowingTrend + 1;
				iTrend.closePrice = previousBar.getClose();
				iTrend.barEnd = trendCur.currentBar;
				log.debug("An Inner " + (innerTrendSign == 1 ? "LONG" : "SHORT") + " " +
						  (iTrend.rebound ? "REBOUND" : "") + " trend is over. " +
						  "Running on bars from " + iTrend.barStart + " to " + iTrend.barEnd + ". " +
						  "Opened at " + iTrend.openPrice + " - closed at " + iTrend.closePrice);
				
				// create a new inner trend towards the right direction
				barsFollowingTrend = trendCur.duration - trendCur.currentBar;
				directionToGo = innerTrendSign = (int) Math.signum(trendCur.targetPrice - currentBar.getOpen());
				log.debug("End of trend approaching, start a " + (innerTrendSign == 1 ? "LONG" : "SHORT") +
						  " trend moving towards the target price from where we are");
				iTrend = new InnerTrend(innerTrendSign, barsFollowingTrend);
				iTrend.barStart = trendCur.currentBar;
				iTrend.barEnd = iTrend.barStart + barsFollowingTrend;
				trendCur.innerTrends.add(iTrend);
			}
			else
			{
				log.debug("Entered the approachingEnd phase while in " +
						  (innerTrendSign == 1 ? "LONG" : "SHORT") + " trend. Keep it going ");
				iTrend.numOfBars += trendCur.duration - trendCur.currentBar - barsFollowingTrend;
				iTrend.barEnd = trendCur.duration;
			}
			trendFollowing = true;
			endOfTrendDutiesDone = true;
		}
	}
	
	private double calculateBarSize()
	{
		double barSize = 1;
		double barSizeBand = props.getRand().nextDouble();
		for(int k = 0; k < props.getBarsSizeNumOfBarsPercentage().length; k++)
		{
			if (barSizeBand <= props.getBarsSizeNumOfBarsPercentage(k))
			{
				barSize = props.getBarsSizeAverageBarSizePercentage(k) * trendCur.barSizeAmplifier;
				break;
			}
		}
		log.trace("Probability for barSize " + String.format("%8.6f%%", barSizeBand) + " " +
				  "barSize retrieved " + String.format("%8.6f%%", barSize));
		return barSize;
	}
	
	private double nextBarVol()
	{
		double intrabarVol;
		double barSize;
		double random;
		random = (props.getRand().nextDouble() - .5);

		barSize = calculateBarSize();
		if (trendCur.direction == 0)
		{
			if (forceRebound)
			{
				random += directionToGo * .5;
			}
			else
			{
				random = random + (innerTrendSign == 0 ? directionToGo : innerTrendSign) * .25;
			}
			intrabarVol = block.getMaxIntrabarVol() * barSize * random;
		}
		else
		{			
			if (forceRebound)
			{
				random += directionToGo * .5;
				log.debug(
						String.format("Bar %d - Rebound forced as %6.4f. open %8.2f - target %8.2f - last close %8.2f - last open %8.2f", 
									trendCur.currentBar,  
									random,
									trendCur.openPrice,
									trendCur.targetPrice,
									previousBar.getOpen(),
									previousBar.getClose()
						));
			}
			else if (approachingEndOfTrend)
			{
					// random = random + trendSign * .5;
				random = Math.abs(props.getRand().nextDouble() / 4) + .75;
				random *= directionToGo;
			}
			else
			{
				random = random + (innerTrendSign == 0 ? directionToGo : innerTrendSign) * .1;
			}
			intrabarVol = block.getMaxIntrabarVol() * barSize * random;
			log.debug("innerTrendSign " + innerTrendSign + " directionToGo " + directionToGo + " " +
					  "random part " + String.format("%8.6f%%", random) + " " +
					  "barSize " + barSize + " " +
					  "intrabarVol " + String.format("%8.6f%%", intrabarVol));
			
//			if (trendCur.capIntradayVol &&
//				(Math.abs(totIntradayVol + intrabarVol) > currentBar.getIntradayVol()) && 
//				!approachingEndOfTrend)
//			{
//				intrabarVol *= -1;
//			}
		}

		totIntradayVol += intrabarVol;
		return intrabarVol;
	}
	
	private double calculateVolume()
	{
		double volumeChange = (props.getRand().nextDouble() - .5) * 2;
        return props.getInitialVolume() + volumeChange * props.getInitialVolume();
	}
	
	public List<MarketBar> blockHandler(Block block, double openPrice, double closePrice, long timestamp)
	{
        this.block = block;
		List<MarketBar> blockBars = new ArrayList<MarketBar>();
		Trend trendPrev = block.getTrend(0);
		trendPrev.closePrice = openPrice;
		trendPrev.timestampEnd = timestamp;
		
		previousBar = new MarketBar(trendPrev.timestampEnd, 0, 0, 0); // the simulated previous mkt bar
		previousBar.setClose(closePrice);
		previousBar.setOpen(openPrice);
		log.debug("=> HANDLING BLOCK Id " + block.getId());
		// Iterate over all the Trends defined within the block
		for(int y = 0; y < block.getNumOfTrends(); y++)
		{
			innerTrendSign = 0;
			trendCur = block.getTrend(y + 1);
			trendCur.id = y + 1;
			trendCur.openPrice = previousBar.getClose();
			if (trendCur.direction != 0)
			{
				// if the trend is long or short, set the target accordingly with its direction
				// and the required delta points at the end
				// also set the higher and lower values reachable within the tren to allow 
				// bouncing when the price get to the limit
				trendCur.targetPrice = trendCur.openPrice + trendCur.deltaPoints;
				trendCur.higher = (trendCur.direction == 1 ? trendCur.targetPrice : trendCur.openPrice);
				trendCur.lower = (trendCur.direction == 1 ? trendCur.openPrice : trendCur.targetPrice);
			}
			else
			{
				// same as above but, for lateral trends, set the target based on the required 
				// delta points to reach at the end at the end
				// the delta points is the channel width within which the price could move
				// on each bar
				trendCur.targetPrice = trendCur.openPrice + trendCur.deltaPoints;
				trendCur.higher = trendCur.openPrice + trendCur.lateralBounceAtDeltaPoints;
				trendCur.lower = trendCur.openPrice - trendCur.lateralBounceAtDeltaPoints;
			}
			
			trendCur.timestampStart = trendPrev.timestampEnd + 
									  props.getBarsIntervalInMinutes() * 60000;
			trendCur.currentBar = 0;
			approachingEndOfTrend = false;
			endOfTrendDutiesDone = false;

			log.debug("****** NEW TRAND STARTING (trend Id " + trendCur.id + "). Traget price is " +
					  trendCur.targetPrice);
			
			// iterate on calculating bars for the duration of the trend
			int duration = (props.getForceConvergenceOnLastBar() ? trendCur.duration - 1: trendCur.duration);
			for(int i = 0; i < duration; i++)
			{
				if ((i > trendCur.duration - trendCur.duration * props.getConsiderApproachingEndOfTrend()) &&
					(!approachingEndOfTrend))
				{
					// if the number of bars left are less than the considerApproachingEndOfTrend value in %
					// starts having an eye of regard to the convergency of the trend towards the target point
					approachingEndOfTrend = true;
					log.debug("Current bar " + i + " entering the approachingEnd phase");
				}
				trendCur.currentBar++;

				// Create the new bar to be calculated
				currentBar = new MarketBar(previousBar.getTimestamp(), props.getBarsIntervalInMinutes() * 60000, 0, 0);

				if (currentBar.getStartOfDayBar())
				{
					// on the first bar of the day, if the current trend requires to cap the intraday volatility,
					// do the required calculation and set the cap value
					if (trendCur.capIntradayVol)
					{
						currentBar.setIntradayVol(props.getMaxIntradayVol() * 
												  currentBar.getIntradayVol() * 100 *
												  currentBar.getOpen());
					}
					else
					{
						currentBar.setIntradayVol(0);
					}
					
					log.debug(
							"Day " + 
							new SimpleDateFormat("yyyy/MM/dd")
								.format(new Date(previousBar.getTimestamp())) +
							" closed with intradayVol at " + 
							String.format("%8.5f", totIntradayVol * 100) +
							" cap for the next day is " + 
							String.format("%8.5f", currentBar.getIntradayVol()));
					totIntradayVol = 0;
				}
				else
				{
					// TODO: double check if this could create isses.
					currentBar.setIntradayVol(previousBar.getIntradayVol());
				}
				
				// the openPrice of the current bar is set to the last close price 
				// (e.g. no preopen phase is currently considered)
				currentBar.setOpen(previousBar.getClose()); 
				
				// within the current trend, minitrends (aka as inner trend) may happen if configured so
				// Evaluate if an innert trend starts on this bar and record it eventually
				evaluateTrendEnter();
				
				currentBar.setTrendFollowing(trendFollowing ? 1 * innerTrendSign : 0);
				// Get where the next bar should go based on the current price, the open and target and the 
				// presence of an innerMiniTrend
				if (Math.abs(totIntradayVol) > currentBar.getIntradayVol() && 
						trendCur.capIntradayVol)
				{
					System.out.println("Block " + block.getId() + " - Trend " + trendCur.id + " - bar " + i +
										" maxIntradayVol " + String.format("%8.5f", currentBar.getIntradayVol() * 100) + 
										" current intraday Vol " + 
										String.format("%8.5f", totIntradayVol * 100));
				}
				currentBar.setIntrabarVol(nextBarVol());
				double priceChange = previousBar.getClose() * currentBar.getIntrabarVol();
				priceChange = Math.round(priceChange * (1 / props.getMarketTick())) * props.getMarketTick();
				log.debug("Bar " + trendCur.currentBar + " " +
						  "open " + currentBar.getOpen() + " target " + trendCur.targetPrice + " " +
						  "direction to go " + (innerTrendSign == 1 ? "LONG" : "SHORT") + " " +
						  "intrabarVol " + String.format("%8.6f%%", currentBar.getIntrabarVol()) + " " +
						  "priceChange " + priceChange);


				if ((trendCur.direction != 0) &&
					((currentBar.getOpen() + priceChange >= 
						(trendCur.direction == 1 ? trendCur.targetPrice : trendCur.openPrice)) ||
					(currentBar.getOpen() + priceChange <= 
						(trendCur.direction == -1 ? trendCur.targetPrice : trendCur.openPrice)))) 
				{
					log.debug("The calculated close price " + (currentBar.getOpen() + priceChange) +
							  " will cross a limit, revert the change sign");
					priceChange *= -1;
					
				}				
				currentBar.setClose(previousBar.getClose() + priceChange);
				currentBar.setHighAndLow(i, trendCur.shadowSizeAmplifier);
				currentBar.setVolume(calculateVolume());

				blockBars.add(currentBar);
				previousBar = currentBar;
			}

			if (props.getForceConvergenceOnLastBar())
			{
				currentBar = new MarketBar(previousBar.getTimestamp(), props.getBarsIntervalInMinutes() * 60000, 0, 0);
				currentBar.setOpen(previousBar.getClose()); // the openPrice is set to the last close price
				currentBar.setClose(trendCur.targetPrice); // the openPrice is set to the last close price
				currentBar.setHighAndLow(trendCur.currentBar, trendCur.shadowSizeAmplifier);			
				currentBar.setVolume(calculateVolume());
				
				blockBars.add(currentBar);
				previousBar = currentBar;
			}
			trendCur.closePrice = currentBar.getClose();
			trendCur.timestampEnd = currentBar.getTimestamp();
			trendPrev = trendCur;
			trendFollowing = false;
			log.debug("****** TRAND Id " + trendCur.id + 
					  " is over. CLosed at price " + trendCur.closePrice + "\n\n");
		}

		return blockBars;
	}
}
