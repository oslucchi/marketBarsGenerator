package barsGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import barsGenerator.Block.Trend;

public class MarketSimulator {
	private Random rand;
	private ApplicationProperties props;
	private MarketBar mb;
	
	private double targetPrice;
    
	private boolean trendFollowing = false;
	private int trendSign;
	private int barsFollowingTrend;
	private double directionToGo = 1;
	private Trend trendCur;

	public MarketSimulator()
	{
		props = ApplicationProperties.getInstance();
		this.rand = new Random(System.currentTimeMillis()); 
		barsFollowingTrend = 0;
	}

	private void evaluateTrendEnter(Block block)
	{
		directionToGo = Math.signum(targetPrice - mb.getClose());

//		if (trendsInPeriod >= mt.getMaxTrendsInPeriod())
//		{
//			trendFollowing = false;;
//			trendSign = 0;
//			barsFollowingTrend = 0;
//			return;
//		}
		
		if (!trendFollowing)
		{
			if (rand.nextDouble() > 1 - props.getProbabilityToEnterTrend())
			{
				trendFollowing = true;
				barsFollowingTrend = (int)((trendCur.maxBarPerTrend - trendCur.minBarPerTrend) * rand.nextDouble()) +
									 trendCur.minBarPerTrend;
				double trendDirection = rand.nextDouble() -.5 + (directionToGo > 0 ? .1 : -.1);
				if (trendDirection < 0)
				{
					trendSign = -1;
				}
				else
				{
					trendSign = 1;
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
				trendFollowing = false;;
				trendSign = 0;
				barsFollowingTrend = 0;
			}
		}
	}
	
	private double barChange(Block block)
	{
		// Set the probability of a negative number same as positive;
		double currentVolatility = Math.abs((trendCur.targetPrice - mb.getClose()) / trendCur.targetPrice);
		double currentVSExpectedVolatilityRatio = currentVolatility / Math.abs(trendCur.deltaPoints / trendCur.startPrice);
		double volatilityAdjustment = 0; // will help shifting the probability of up and down towards the expected direction

		if (currentVSExpectedVolatilityRatio > 1)
		{
			currentVSExpectedVolatilityRatio = 1;
			volatilityAdjustment = 0.45 * directionToGo;
		}
		
		if (trendFollowing)
		{
			volatilityAdjustment = trendSign * .25;
			directionToGo = trendSign;
		}
		
		// Calculating the intrabar volatility
		double randomVol = volatilityAdjustment + rand.nextDouble() - 0.5;
		double changeWidth = 1; 
		if (Math.signum(randomVol) != directionToGo)
		{
			changeWidth -= (Math.pow(11, currentVSExpectedVolatilityRatio) - .95) / 10; 
		}
		double intrabarVol = block.getMaxIntrabarVol() * changeWidth * randomVol;
		
		return intrabarVol;
	}
	
	private double calculateVolume()
	{
		double volumeChange = (rand.nextDouble() - .5) * 2;
        return props.getInitialVolume() + volumeChange * props.getInitialVolume();
	}
	
	public List<MarketBar> blockHandler(Block block, double startPrice, long timestamp)
	{
		List<MarketBar> blockBars = new ArrayList<MarketBar>();
		Trend trendPrev = block.getTrend(0);
		trendPrev.closePrice = startPrice;
		trendPrev.timestamp = timestamp - props.getBarsIntervalInMinutes() * 60000;
		
		for(int y = 0; y < block.getNumOfTrends(); y++)
		{
			trendCur = block.getTrend(y + 1);
			trendCur.startPrice = trendPrev.closePrice;
			trendCur.targetPrice = trendCur.startPrice + trendCur.deltaPoints;
			trendCur.timestamp = trendPrev.timestamp + props.getBarsIntervalInMinutes() * 60000;
			mb = new MarketBar(trendPrev.timestamp, props.getBarsIntervalInMinutes(), 0, 0); // the simulated previous makt bar
			mb.setClose(trendCur.startPrice);

			for(int i = 0; i < trendCur.duration; i++)
			{
				evaluateTrendEnter(block);
				double priceChange = barChange(block);
				MarketBar newBar = new MarketBar(mb.getTimestamp(), props.getBarsIntervalInMinutes(), priceChange, trendSign);
				newBar.setOpen(mb.getClose());
				newBar.setClose(mb.getClose() + mb.getClose() * priceChange);
				double barSizeBand = rand.nextDouble();
				double barSize = 0;
				for(int k = 0; k < props.getBarsShadowNumOfBarsPercentage().length; k++)
				{
					if (barSizeBand < props.getBarsShadowNumOfBarsPercentage()[k])
					{
						barSize = props.getBarsShadowAverageBarSizePercentage()[k];
						break;
					}
				}
				
				newBar.setHigh(Math.max(newBar.getOpen(), newBar.getClose()) * (1 + barSize / 100));
				newBar.setLow(Math.min(newBar.getOpen(), newBar.getClose()) * (1 - barSize / 100));

				newBar.setVolume(calculateVolume());

				blockBars.add(newBar);
				mb = blockBars.get(blockBars.size() - 1);
			}
			trendPrev = trendCur;
		}

		return blockBars;
	}
}
