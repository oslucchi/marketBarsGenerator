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
	private int innerTrendSign = 0;
	private int barsFollowingTrend;
	private int directionToGo = 1;
	private Trend trendCur;
	private boolean approachingEndOfTrend = false;

	public MarketSimulator()
	{
		props = ApplicationProperties.getInstance();
		barsFollowingTrend = 0;
	}

	private void closeLastInnerTrend()
	{
		InnerTrend iTrend;
		iTrend = trendCur.innerTrends.get(trendCur.innerTrends.size() - 1);	
		iTrend.barEnd = trendCur.currentBar - 1;
		iTrend.numOfBars -= barsFollowingTrend;
		iTrend.closePrice = previousBar.getClose();
		trendFollowing = false;
	}

	private void startNewInnerTrend(int direction)
	{
		InnerTrend iTrend;
		barsFollowingTrend = props.getRand().nextInt(trendCur.maxBarPerTrend - trendCur.minBarPerTrend) + 
							 trendCur.minBarPerTrend - 1;
		if (barsFollowingTrend + trendCur.currentBar > trendCur.duration)
		{
			barsFollowingTrend = trendCur.duration - trendCur.currentBar;
		}
		iTrend = new InnerTrend(direction, barsFollowingTrend);
		iTrend.barStart = trendCur.currentBar;		
		iTrend.openPrice = currentBar.getOpen();
		trendCur.innerTrends.add(iTrend);
		trendFollowing = true;
	}
	
	private double evaluateRevertDirection(double priceChange)
	{
		int limitBroken = 0;
		
		if (previousBar.getClose() + priceChange > trendCur.higher)
		{
			limitBroken = 1;
			priceChange *= -1;
		}
		else if (previousBar.getClose() + priceChange < trendCur.lower)
		{
			limitBroken = -1;
			priceChange *= -1;
		}
		if (limitBroken == 0)
		{
			return priceChange;
		}
		
		// A limit was going to be broken, make sure that the trend will go towards
		// the opposite direction
		if (trendFollowing)
		{
			if (limitBroken == trendCur.direction)
			{
				closeLastInnerTrend();
				startNewInnerTrend(limitBroken * -1);
			}
		}
		else
		{
			startNewInnerTrend(limitBroken * -1);
		}
		return(priceChange);
	}

	/*
	 * evaluates if the current bar is part of an already running inner trend
	 * If yes evaluate if it is reaching the higher / lower limits of the trend and revert if necessary
	 */
	private void evaluateTrendEnter()
	{
		InnerTrend iTrend;
		
		if (!approachingEndOfTrend &&
			(trendCur.currentBar >= trendCur.duration - props.getBarsToEndOfTrend()))
		{
			// if the number of bars left are less than the considerApproachingEndOfTrend value in %
			// starts having an eye of regard to the convergency of the trend towards the target point
			log.debug("Current bar " + trendCur.currentBar + " entering the approachingEnd phase");

			if (trendFollowing)
			{
				iTrend = trendCur.innerTrends.get(trendCur.innerTrends.size() - 1);
				if (iTrend.direction != trendCur.direction)
				{
					closeLastInnerTrend();
				}
			}
			startNewInnerTrend(trendCur.direction);
			approachingEndOfTrend = true;			
		}
		
		if (!trendFollowing)
		{
			if (props.getRand().nextDouble() > 1 - props.getProbabilityToEnterTrend())
			{
				directionToGo = (props.getRand().nextBoolean() ? 1 : - 1);
				startNewInnerTrend(directionToGo);
			}
			else
			{
				directionToGo = (int) Math.signum(trendCur.targetPrice - currentBar.getOpen());
			}
		}
		else
		{
			directionToGo = trendCur.innerTrends.get(trendCur.innerTrends.size() - 1).direction;
			if (--barsFollowingTrend == 0)
			{
				closeLastInnerTrend();
			}
		}
	
		currentBar.setTrendFollowing(directionToGo);
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

		// Randomly select the effective direction, giving more chances to the 
		// current direction to go
		double random;
		random = directionToGo * 0.75 +
				 (directionToGo < 0 ? 1 : -1 ) * props.getRand().nextDouble();

		barSize *= Math.signum(random);
		currentBar.setIntrabarVol(barSize);
		
		barSize *= trendCur.maxBarSize;
		return barSize;
	}
	
	
	private double calculateVolume()
	{
		double volumeChange = (props.getRand().nextDouble() - .5) * 2;
        return props.getInitialVolume() + volumeChange * props.getInitialVolume();
	}
	
	public List<MarketBar> blockHandler(Block block, double openPrice, double closePrice, long timestamp)
	{
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
			switch(trendCur.direction)
			{
			case Trend.LONG:
				trendCur.lower = trendCur.openPrice;
				trendCur.higher = trendCur.openPrice + trendCur.deltaPoints;
				break;
			case Trend.SHORT:
				trendCur.lower = trendCur.openPrice + trendCur.deltaPoints;
				trendCur.higher = trendCur.openPrice;
				break;
			case Trend.LATERAL:
				trendCur.lower = trendCur.openPrice - trendCur.deltaPoints / 2;
				trendCur.higher = trendCur.openPrice + trendCur.deltaPoints / 2;
				break;
			}
			
			trendCur.closePrice = trendCur.openPrice + trendCur.deltaPoints;
			trendCur.timestampStart = trendPrev.timestampEnd + 
									  props.getBarsIntervalInMinutes() * 60000;
			trendCur.currentBar = 0;
			approachingEndOfTrend = false;

			log.debug("****** NEW TRAND STARTING (trend Id " + trendCur.id + "). Traget price is " +
					  trendCur.targetPrice);
			
			// iterate on calculating bars for the duration of the trend
			for(int i = 0; i < trendCur.duration; i++)
			{
				trendCur.targetPrice = trendCur.openPrice + 
									   trendCur.deltaPoints / trendCur.duration * (i + 1);
				
				trendCur.currentBar++;

				// Create the new bar to be calculated
				currentBar = new MarketBar(previousBar.getTimestamp(), props.getBarsIntervalInMinutes() * 60000, 0, 0);

				if (currentBar.isStartOfDayBar())
				{
				}
				
				// the openPrice of the current bar is set to the last close price 
				// (e.g. no preopen phase is currently considered)
				currentBar.setOpen(previousBar.getClose()); 
				
				// within the current trend, minitrends (aka as inner trend) may happen if configured so
				// Evaluate if an innert trend starts on this bar and record it eventually
				evaluateTrendEnter();
				
				double priceChange = calculateBarSize();
				
				priceChange = Math.round(priceChange * (1 / props.getMarketTick())) * props.getMarketTick();
				log.debug("Bar " + trendCur.currentBar + " " +
						  "open " + currentBar.getOpen() + " target " + trendCur.targetPrice + " " +
						  "direction to go " + (innerTrendSign == 1 ? "LONG" : "SHORT") + " " +
						  "intrabarVol " + String.format("%8.6f%%", currentBar.getIntrabarVol()) + " " +
						  "priceChange " + priceChange);
				
				priceChange = evaluateRevertDirection(priceChange);
				
				currentBar.setClose(previousBar.getClose() + priceChange);
				currentBar.setHighAndLow(i, trendCur.shadowSizeAmplifier);
				currentBar.setVolume(calculateVolume());

				blockBars.add(currentBar);
				previousBar = currentBar;
			}
			if ((currentBar.getClose() != trendCur.openPrice + trendCur.deltaPoints) &&
				(trendCur.direction != 0))
			{
				currentBar.setClose(trendCur.openPrice + trendCur.deltaPoints);
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
