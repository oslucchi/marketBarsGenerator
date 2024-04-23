package barsGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MarketSimulator {
	private Random rand;
	private ApplicationProperties props;
	final private double EPSILON = 0.00001;

	
	private boolean inTheRange(double value, double target, double epsilon)
	{
		if (epsilon <0) epsilon *= -1;
		return(Math.abs(value) < Math.abs(target) + epsilon);
	}
	
	public MarketSimulator(ApplicationProperties props)
	{
		this.props = props;
		this.rand = new Random(System.currentTimeMillis()); 
	}

	public List<MarketBar> generateBars(MarketTrend trend) {
	    List<MarketBar> bars = new ArrayList<>();
	    double lastClose = trend.startPrice;
	    double lastVolume = trend.initialVolume;
	    long timestamp = trend.timestampStart;
	    double targetPrice = lastClose + lastClose * trend.volatility / 100;
	    boolean trendFollowing = false;
	    int barsFollowingTrend = 0;
	    int currentTrendMaBars = (int) (trend.maxBarsInTrend * rand.nextDouble()) + 2;
	    
	    for (int i = 0; i < trend.duration; i++) {
	    	// Get the current distance from the targetPrice
	    	double upright = targetPrice - lastClose;
		    // Calculate what the composed interest should be to get to the target price
	    	// from the current price. The change should be somewhere in the neighbor of it
	    	double composedInterest = Math.pow(Math.abs(upright/lastClose), ((double) 1 / (trend.duration - i)));
	    	
	    	composedInterest = (composedInterest > Math.abs(trend.maxIntrabarVol) ? Math.abs(trend.maxIntrabarVol) : composedInterest);
	    	// Based on the period volatility, the intrabar volatility will have more
	    	// chances to be positive or negative
	    	double intraBarVol = Math.abs(upright/lastClose);
	    	if (inTheRange(intraBarVol, 0., EPSILON) && (i < trend.duration - 1))
	    	{
	    		intraBarVol = trend.volatility / 100 * (rand.nextDouble() + 0.01) ;
	    		composedInterest = Math.abs(intraBarVol);
	    	}
	    	else
	    	{
	    		intraBarVol = (Math.abs(intraBarVol) > composedInterest ?  composedInterest : intraBarVol);
	    	}
	    	
	    	if (trend.enableTrends & !trendFollowing && trend.maxTrendsInPeriod > 0)
	    	{
	    		if (rand.nextDouble() >= .5)
	    		{
	    			trend.maxTrendsInPeriod--;
	    			trendFollowing = true;
	    		}
	    	}
	    	if (trendFollowing && (barsFollowingTrend++ < currentTrendMaBars))
	    	{
	    		if (Math.signum(intraBarVol) != Math.signum(trend.volatility))
	    		{
	    			intraBarVol *= -1;
	    		}
	    	}
	    	else
	    	{
    			trendFollowing = false;
    			currentTrendMaBars = (int) (trend.maxBarsInTrend * rand.nextDouble()) + 2;
    			barsFollowingTrend = 0;
	    		intraBarVol *= (upright == 0 ? -1 : Math.signum(upright));  
	    	}
	    	intraBarVol += (rand.nextDouble() - .5) * Math.abs(intraBarVol) * .3;
	    	
	    	double newPrice = lastClose + lastClose * intraBarVol;
			double high = Math.max(lastClose, newPrice) + rand.nextDouble() * composedInterest * newPrice;
			double low = Math.min(lastClose, newPrice) - rand.nextDouble() * composedInterest * newPrice;
	        double volumeChange = Math.abs((rand.nextGaussian() * trend.volatility / 100)) * 50; // Volume spikes with larger price changes
	        double volume = lastVolume + volumeChange;
	        MarketBar mb = new MarketBar(lastClose, newPrice, high, low, volume, timestamp, 
	        							 props.getInterval(), intraBarVol, trendFollowing);
	        timestamp = mb.timestamp;
	        bars.add(mb);
	        lastClose = newPrice;
 	    }
	    return bars;
	}
}
