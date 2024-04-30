package barsGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MarketSimulator {
	private Random rand;
	private ApplicationProperties props;
	private MarketTrend mt;
	private MarketBar mb;
	
	private double targetPrice;
    
	private boolean trendFollowing = false;
	private int trendSign;
	private int barsFollowingTrend;
	private double directionToGo = 1;
	private int trendsInPeriod = 0;

	
	public MarketSimulator()
	{
		props = ApplicationProperties.getInstance();
		this.rand = new Random(System.currentTimeMillis()); 
		trendsInPeriod = 0;
		barsFollowingTrend = 0;
	}

	private void evaluateTrendEnter()
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
				trendsInPeriod++; 
				trendFollowing = true;
				// allow always at least 40% of the max configured bars to be in the trend
				barsFollowingTrend = (int)(mt.getMaxBarsInTrend() * (.4 + rand.nextDouble() * .6));
				mt.setBarsFollowingTrend(mt.getBarsFollowingTrend() + barsFollowingTrend);
				double trendDirection = rand.nextDouble() -.5 + (directionToGo > 0 ? .1 : -.1);
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
	
	private double barChange(int run)
	{
		// Set the probability of a negative number same as positive;
		double currentVolatility = Math.abs((targetPrice - mb.getClose()) / targetPrice);
		double currentVSExpectedVolatilityRatio = currentVolatility / Math.abs(mt.getVolatility());
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
		double intrabarVol = mt.getMaxIntrabarVol() * changeWidth * randomVol;
		
		return intrabarVol;
	}
	
	private double calculateVolume()
	{
		double volumeChange = (rand.nextDouble() - .5) * 2;
        return props.getInitialVolume() + volumeChange * props.getInitialVolume();
	}
	
	public List<MarketBar> periodHandler(MarketTrend mt)
	{
		List<MarketBar> periodBars = new ArrayList<MarketBar>();

		this.mt = mt;
		// initializing data for the period to start
		targetPrice = mt.getStartPrice() + mt.getStartPrice() * mt.getVolatility(); // the target price for this period
		mb = new MarketBar(mt.getTimestampStart(), props.getInterval(), 0, 0); // the simulated previous makt bar
		mb.setClose(mt.getStartPrice());
		trendsInPeriod = 0;
		
		for(int i = 0; i < mt.getDuration(); i++)
		{
			evaluateTrendEnter();
			double priceChange = barChange(i);
			MarketBar newBar = new MarketBar(mb.getTimestamp(), props.getInterval(), priceChange, trendSign);
			newBar.setOpen(mb.getClose());
			newBar.setClose(mb.getClose() + mb.getClose() * priceChange);
			newBar.setHigh(Math.max(newBar.getOpen(), newBar.getClose()) * (1 + mt.getMaxVolHighLow() / 100 * rand.nextDouble()));
			newBar.setLow(Math.min(newBar.getOpen(), newBar.getClose()) * (1 - mt.getMaxVolHighLow() / 100 * rand.nextDouble()));
	        
	        newBar.setVolume(calculateVolume());

			periodBars.add(newBar);
			mb = periodBars.get(periodBars.size() - 1);
		}
		mt.setMaxTrendsInPeriod(trendsInPeriod);
		
		return periodBars;
	}
}
