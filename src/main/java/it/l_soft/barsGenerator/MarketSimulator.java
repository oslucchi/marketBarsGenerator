package it.l_soft.barsGenerator;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class MarketSimulator {
	final Logger log = Logger.getLogger(this.getClass());
	final int LOW = 0;
	final int HIGH = 1;

	private ApplicationProperties props;
	private MarketBar previousBar, currentBar;
	
	private boolean trendFollowing = false;
	private int trendSign = 0;
	private int barsFollowingTrend;
	private double directionToGo = 1;
	private Trend trendCur;
	private Block block;
	private boolean forceRebound = false;
	private boolean approachingEndOfTrend = false;

	public MarketSimulator()
	{
		props = ApplicationProperties.getInstance();
		barsFollowingTrend = 0;
	}

	private void evaluateTrendEnter()
	{
		forceRebound = false;
		if ((Math.abs(trendCur.targetPrice - previousBar.getClose()) < block.getMaxIntrabarVol() * trendCur.targetPrice) &&
			(trendCur.direction == 1))
		{
			// long, the close price should never go over trend target
			directionToGo = -1;
			forceRebound = true;
			log.debug("price would go over the target price during a trend long, rebouncing");
		}
		else if ((Math.abs(currentBar.getOpen() - trendCur.targetPrice) < block.getMaxIntrabarVol() * currentBar.getOpen()) &&
				 (trendCur.direction == -1))
		{
			// short, the close price should never go below trend open 
			directionToGo = 1;
			forceRebound = true;
			log.debug("price would go below the open price during a trend short, rebouncing");
		}
		else if (trendCur.direction == 0)
		{
			if ((Math.abs(currentBar.getOpen() - trendCur.targetPrice) < block.getMaxIntrabarVol() * currentBar.getOpen()) ||
				(Math.abs(trendCur.targetPrice - previousBar.getClose()) < block.getMaxIntrabarVol() * trendCur.targetPrice))
			{
				directionToGo = Math.signum(trendCur.targetPrice - previousBar.getClose()); // keep the focus to the target
				forceRebound = true;
				log.debug("price moving off limits in lateral trend, rebouncing");
			}
		}
		else
		{
			directionToGo = trendCur.direction;
		}
		
		if (!approachingEndOfTrend)
		{
			if (!trendFollowing)
			{
				double random = props.getRand().nextDouble();
				if (random > 1 - props.getProbabilityToEnterTrend())
				{
					trendFollowing = true;
					random = props.getRand().nextDouble();
					barsFollowingTrend = (int)((trendCur.maxBarPerTrend - trendCur.minBarPerTrend) * random) +
										 trendCur.minBarPerTrend;
					if (!forceRebound)
					{
						random = props.getRand().nextDouble();
						double trendDirection = random -.5 + (directionToGo > 0 ? .1 : -.1);
						if (trendDirection < 0)
						{
							trendSign = -1;
						}
						else
						{
							trendSign = 1;
						}
					}
					else
					{
						trendSign = (int) directionToGo;
						log.debug("The new innertrend will be generated to execute a rebound");
					}
					trendCur.innerTrends.add(new InnerTrend(trendSign, barsFollowingTrend));
					log.debug("Entering a new " + (trendSign == 1 ? "long" : "short") + 
							   " inner trend of length " + barsFollowingTrend );
				}
				else
				{
					trendSign = 0;
					barsFollowingTrend = 0;
				}
			}
			else
			{
				if (--barsFollowingTrend <= 0)
				{
					trendFollowing = false;
					trendSign = 0;
					barsFollowingTrend = 0;
					log.debug("The inner trend is over. Close price on previousBar was " + 
							  previousBar.getClose());
				}
				else if (forceRebound)
				{
					InnerTrend iTrend = trendCur.innerTrends.get(trendCur.innerTrends.size() - 1);
					iTrend.numOfBars = iTrend.numOfBars - barsFollowingTrend + 1;
					trendCur.innerTrends.add(new InnerTrend(trendSign, barsFollowingTrend + 1));
					trendSign = (int) directionToGo;
					log.debug("A rebound was called during an inner trend is over. Close it on " +
							  iTrend.numOfBars + " bars and previous close at " + 
							  previousBar.getClose());
					log.debug("Start a rebound " + (trendSign == 1 ? "long" : "short") + 
							  " minitrand " + barsFollowingTrend + " bars long ");
				}
			}
		}
		else
		{
			if (trendFollowing)
			{
				log.debug("Entered the approachingEnd phase while in trend. Stopping it");
				InnerTrend iTrend = trendCur.innerTrends.get(trendCur.innerTrends.size() - 1);
				iTrend.numOfBars = iTrend.numOfBars - barsFollowingTrend + 1;
				trendFollowing = false;
				barsFollowingTrend = 0;
			}
			directionToGo = trendSign = (int) Math.signum(trendCur.targetPrice - previousBar.getClose());
		}
	}
	
	private double calculateBarSize()
	{
		double barSize = 1;
		double barSizeBand = props.getRand().nextDouble();
		for(int k = 0; k < props.getBarsSizeNumOfBarsPercentage().length; k++)
		{
			if (barSizeBand <= props.getBarsSizeNumOfBarsPercentage()[k])
			{
				barSize = props.getBarsSizeAverageBarSizePercentage()[k];
				break;
			}
		}
		return barSize;
	}
	
	private double nextBarVol()
	{
		double intrabarVol;
		double barSize;
		double random;
		random = (props.getRand().nextDouble() - .5);

		if (trendCur.direction == 0)
		{
			if (previousBar.getClose() > trendCur.openPrice)
			{
				random -= 0.5 - ((trendCur.higher - previousBar.getClose()) / 
							trendCur.deltaPoints);
			}
			else
			{
				random += 0.5 - ((previousBar.getClose() - trendCur.lower) / 
							trendCur.deltaPoints);
			}
			intrabarVol = (trendCur.deltaPoints * random) / previousBar.getClose();
		}
		else
		{
			barSize = calculateBarSize();
			
			if (forceRebound)
			{
				random += directionToGo * .5;
				log.trace(
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
				random = random + (trendSign == 0 ? directionToGo : trendSign) * .1;
			}
			intrabarVol = block.getMaxIntrabarVol() * barSize * random;
		}
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
		log.debug("-> HANDLING BLOCK Id " + block.getId());
		for(int y = 0; y < block.getNumOfTrends(); y++)
		{
			trendSign = 0;
			trendCur = block.getTrend(y + 1);
			trendCur.id = y + 1;
			trendCur.openPrice = previousBar.getClose();
			log.debug("****** NEW TRAND STARTING (trend Id " + trendCur.id + ")");
			if (trendCur.direction != 0)
			{
				trendCur.targetPrice = trendCur.openPrice + trendCur.deltaPoints;
				trendCur.higher = (trendCur.direction == 1 ? trendCur.targetPrice : trendCur.openPrice);
				trendCur.lower = (trendCur.direction == 1 ? trendCur.openPrice : trendCur.targetPrice);
			}
			else
			{
				trendCur.targetPrice = trendCur.openPrice;
				trendCur.higher = trendCur.openPrice + trendCur.deltaPoints / 2;
				trendCur.lower = trendCur.openPrice - trendCur.deltaPoints / 2;
			}
			
			trendCur.timestampStart = trendPrev.timestampEnd + props.getBarsIntervalInMinutes() * 60000;
			
			trendCur.currentBar = 0;
			approachingEndOfTrend = false;

			log.trace("*** Trend " + y);
			int duration = (props.getForceConvergenceOnLastBar() ? trendCur.duration - 1: trendCur.duration);
			for(int i = 0; i < duration; i++)
			{
				if ((i > trendCur.duration - trendCur.duration * props.getConsiderApproachingEndOfTrend()) &&
					(!approachingEndOfTrend))
				{
					approachingEndOfTrend = true;
					log.debug("Current bar " + i + " entering the approachingEnd phase");
				}
				trendCur.currentBar++;
				currentBar = new MarketBar(previousBar.getTimestamp(), props.getBarsIntervalInMinutes() * 60000, 0, 0);
				currentBar.setOpen(previousBar.getClose()); // the openPrice is set to the last close price
				
				// Evaluate if an innertMiniTrend starts and record it eventually
				evaluateTrendEnter();
				currentBar.setTrendFollowing(trendFollowing ? 1 * trendSign : 0);
				
				// Get where the next bar should go based on the current price, the open and target and the 
				// presence of an innerMiniTrend
				currentBar.setIntrabarVol(nextBarVol());
				double priceChange = previousBar.getClose() * currentBar.getIntrabarVol();
				priceChange = Math.round(priceChange * (1 / props.getMarketTick())) * props.getMarketTick();
				
				if ((trendCur.direction != 0) &&
					((previousBar.getClose() + priceChange >= 
						(trendCur.direction == 1 ? trendCur.targetPrice : trendCur.openPrice)) ||
					(previousBar.getClose() + priceChange <= 
						(trendCur.direction == -1 ? trendCur.targetPrice : trendCur.openPrice)))) 
				{
					priceChange *= -1;
					
				}				
				currentBar.setClose(previousBar.getClose() + priceChange);
				currentBar.setHighAndLow();;
				currentBar.setVolume(calculateVolume());

				blockBars.add(currentBar);
				previousBar = currentBar;
			}

			if (props.getForceConvergenceOnLastBar())
			{
				currentBar = new MarketBar(previousBar.getTimestamp(), props.getBarsIntervalInMinutes() * 60000, 0, 0);
				currentBar.setOpen(previousBar.getClose()); // the openPrice is set to the last close price
				currentBar.setClose(trendCur.targetPrice); // the openPrice is set to the last close price
				currentBar.setHighAndLow();			
				currentBar.setVolume(calculateVolume());
				
				blockBars.add(currentBar);
				previousBar = currentBar;
			}
			trendCur.closePrice = currentBar.getClose();
			trendCur.timestampEnd = currentBar.getTimestamp();
			trendPrev = trendCur;
			trendFollowing = false;
		}

		return blockBars;
	}
}
