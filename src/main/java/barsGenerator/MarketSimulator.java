package barsGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;

import barsGenerator.Block.InnerTrend;
import barsGenerator.Block.Trend;

public class MarketSimulator {
	final Logger log = Logger.getLogger(this.getClass());

	private Random rand;
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
		this.rand = new Random(System.currentTimeMillis()); 
		barsFollowingTrend = 0;
	}

	private void evaluateTrendEnter()
	{
		if ((trendCur.targetPrice - previousBar.getClose() < block.getMaxIntrabarVol() * trendCur.targetPrice) &&
			(trendCur.direction == 1))
		{
			// long, the close price should never go over trend target
			directionToGo = -1;
			forceRebound = true;
		}
		else if ((currentBar.getOpen() - trendCur.targetPrice < block.getMaxIntrabarVol() * currentBar.getOpen()) &&
				 (trendCur.direction == -1))
		{
			// short, the close price should never go below trend open 
			directionToGo = 1;
			forceRebound = true;
		}
		else
		{
			if (trendCur.direction == 0)
			{
				directionToGo = Math.signum(trendCur.targetPrice - previousBar.getClose()); // keep the focus to the target
			}
			else
			{
				directionToGo = trendCur.direction;
			}
			forceRebound = false;
		}
		
		if (!approachingEndOfTrend)
		{
			if (!trendFollowing)
			{
				double random = rand.nextDouble();
				if (random > 1 - props.getProbabilityToEnterTrend())
				{
					trendFollowing = true;
					random = rand.nextDouble();
					barsFollowingTrend = (int)((trendCur.maxBarPerTrend - trendCur.minBarPerTrend) * random) +
										 trendCur.minBarPerTrend;
					if (!forceRebound)
					{
						random = rand.nextDouble();
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
					}
					trendCur.innerTrends.add(block.new InnerTrend(trendSign, barsFollowingTrend));
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
				}
				else if (forceRebound)
				{
					InnerTrend iTrend = trendCur.innerTrends.get(trendCur.innerTrends.size() - 1);
					iTrend.numOfBars = iTrend.numOfBars - barsFollowingTrend + 1;
					trendCur.innerTrends.add(block.new InnerTrend(trendSign, barsFollowingTrend + 1));
					trendSign = (int) directionToGo;
				}
			}
		}
		else
		{
			if (trendFollowing)
			{
				InnerTrend iTrend = trendCur.innerTrends.get(trendCur.innerTrends.size() - 1);
				iTrend.numOfBars = iTrend.numOfBars - barsFollowingTrend + 1;
				trendFollowing = false;
				barsFollowingTrend = 0;
			}
			trendSign = (trendCur.direction != 0 ? trendCur.direction : 
						 (int) Math.signum(trendCur.targetPrice - previousBar.getClose()));
		}
	}
	
	private double nextBarVol()
	{
		double random;
		
		random = rand.nextDouble() - .5;
		if (forceRebound)
		{
			random += directionToGo * .5;
			log.trace(String.format("Bar %d - Rebound forced as %6.4f. open %8.2f - target %8.2f - last close %8.2f - last open %8.2f", 
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
			random = random + trendSign * .5;
		}
		else
		{
			random = random + (trendSign == 0 ? directionToGo : trendSign) * .1;
		}
		double intrabarVol = block.getMaxIntrabarVol() * random;
		return intrabarVol;
	}
	
	private double calculateVolume()
	{
		double volumeChange = (rand.nextDouble() - .5) * 2;
        return props.getInitialVolume() + volumeChange * props.getInitialVolume();
	}
	
	private double calculateHigh(double shadowSize, double barSize)
	{
		double reference = Math.max(currentBar.getOpen(), currentBar.getClose());
        return  reference + barSize * shadowSize * props.getShadowSizeInBarPercentage();
	}
	
	private double calculateLow(double shadowSize, double barSize)
	{
		double reference = Math.min(currentBar.getOpen(), currentBar.getClose());
        return  reference - barSize * shadowSize * props.getShadowSizeInBarPercentage();
	}
	
	private double calculateShadowSize()
	{
		double shadowSize = 1;
		double shadowSizeBand = rand.nextDouble();
		for(int k = 0; k < props.getBarsShadowNumOfBarsPercentage().length; k++)
		{
			if (shadowSizeBand <= props.getBarsShadowNumOfBarsPercentage()[k])
			{
				shadowSize = props.getBarsShadowAverageBarSizePercentage()[k];
				break;
			}
		}
		return shadowSize;
	}
	
	public List<MarketBar> blockHandler(Block block, double openPrice, double closePrice, long timestamp)
	{
		this.block = block;
		List<MarketBar> blockBars = new ArrayList<MarketBar>();
		Trend trendPrev = block.getTrend(0);
		trendPrev.closePrice = openPrice;
		trendPrev.timestamp = timestamp - props.getBarsIntervalInMinutes() * 60000;
		
		previousBar = new MarketBar(trendPrev.timestamp, props.getBarsIntervalInMinutes() * 60000, 0, 0); // the simulated previous mkt bar
		previousBar.setClose(closePrice);
		previousBar.setOpen(openPrice);
		for(int y = 0; y < block.getNumOfTrends(); y++)
		{
			trendSign = 0;
			trendCur = block.getTrend(y + 1);
			trendCur.openPrice = previousBar.getClose();
			trendCur.targetPrice = trendCur.openPrice + trendCur.deltaPoints;
			trendCur.timestamp = trendPrev.timestamp + props.getBarsIntervalInMinutes() * 60000;
			trendCur.currentBar = 0;
			approachingEndOfTrend = false;

			log.trace("*** Trend " + y);
			for(int i = 0; i < trendCur.duration - 1; i++)
			{
				if (i > trendCur.duration - trendCur.duration * props.getConsiderApproachingEndOfTrend())
				{
					approachingEndOfTrend = true;
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
				
				if ((trendCur.direction != 0) &&
					((previousBar.getClose() + priceChange >= 
						(trendCur.direction == 1 ? trendCur.targetPrice : trendCur.openPrice)) ||
					(previousBar.getClose() + priceChange <= 
						(trendCur.direction == -1 ? trendCur.targetPrice : trendCur.openPrice)))) 
				{
					priceChange *= -1;
					
				}
				
				currentBar.setClose(previousBar.getClose() + priceChange);
				
				//Calculate HIGH and low based on the configured probability distribution 
				double barSize = Math.abs(currentBar.getClose() - currentBar.getOpen());

				double shadowSize = calculateShadowSize();
				currentBar.setHigh(calculateHigh(shadowSize, barSize));
				if (!props.getSameHighAndLowDepth())
				{
					shadowSize = calculateShadowSize();
				}
				currentBar.setLow(calculateLow(shadowSize, barSize));

				currentBar.setVolume(calculateVolume());

				blockBars.add(currentBar);
				previousBar = currentBar;
			}
			currentBar = new MarketBar(previousBar.getTimestamp(), props.getBarsIntervalInMinutes() * 60000, 0, 0);
			currentBar.setOpen(previousBar.getClose()); // the openPrice is set to the last close price
			currentBar.setClose(trendCur.targetPrice); // the openPrice is set to the last close price
			double barSize = Math.abs(currentBar.getClose() - currentBar.getOpen());

			double shadowSize = calculateShadowSize();
			currentBar.setHigh(calculateHigh(shadowSize, barSize));
			if (!props.getSameHighAndLowDepth())
			{
				shadowSize = calculateShadowSize();
			}
			currentBar.setLow(calculateLow(shadowSize, barSize));
			currentBar.setVolume(calculateVolume());
			blockBars.add(currentBar);
			previousBar = currentBar;
			
			trendPrev = trendCur;
			trendFollowing = false;
		}

		return blockBars;
	}
}
