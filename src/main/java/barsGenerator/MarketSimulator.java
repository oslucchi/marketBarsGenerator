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

	
	public MarketSimulator()
	{
		props = ApplicationProperties.getInstance();
		this.rand = new Random(System.currentTimeMillis()); 
	}

	private void evaluateTrendEnter()
	{
		if (mt.getMaxTrendsInPeriod() <= 0)
			return;
		
		if (!trendFollowing)
		{
			if (rand.nextDouble() > 1 - props.getProbabilityToEnterTrend())
			{
				trendFollowing = true;
				// allow always 40% of the configured bars to be in the trend
				barsFollowingTrend = (int)(mt.getMaxBarsInTrend() * (.4 + rand.nextDouble() * .6));
				
				if (rand.nextDouble() < .5)
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
				trendFollowing = false;;
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
	
	private double barChange()
	{
		// Set the probability of a negative number same as positive;
		double drift = Math.abs((targetPrice - mb.getClose()) / targetPrice);

		if (drift / Math.abs(mt.getVolatility()) > 1)
		{
			drift = (mb.getClose() > targetPrice ? -0.45 : 0.45);
		}
		else
		{
			if (trendFollowing)
			{
				drift = trendSign * .45;
			}
			else
			{
				if (targetPrice < mb.getClose())
				{
					drift *= -1;
				}
			}
		}
		
		// Calculating the intrabar volatility
		double intrabarVol = mt.getMaxIntrabarVol() * (drift + 	(rand.nextDouble() - 0.5));
		
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
		
		for(int i = 0; i < mt.getDuration(); i++)
		{
			evaluateTrendEnter();
			double priceChange = barChange();
			MarketBar newBar = new MarketBar(mb.getTimestamp(), props.getInterval(), priceChange, trendSign);
			newBar.setOpen(mb.getClose());
			newBar.setClose(mb.getClose() + mb.getClose() * priceChange);
			newBar.setHigh(Math.max(newBar.getOpen(), newBar.getClose()) * (1 + mt.getMaxVolHighLow() / 100 * rand.nextDouble()));
			newBar.setLow(Math.min(newBar.getOpen(), newBar.getClose()) * (1 - mt.getMaxVolHighLow() / 100 * rand.nextDouble()));
	        
	        newBar.setVolume(calculateVolume());

			periodBars.add(newBar);
			mb = periodBars.get(periodBars.size() - 1);
		}
		return periodBars;
	}
}
