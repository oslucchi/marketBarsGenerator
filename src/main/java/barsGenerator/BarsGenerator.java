package barsGenerator;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BarsGenerator {
    public static void main(String[] args) {
    	ApplicationProperties props = ApplicationProperties.getInstance();
    	MarketSimulator simulator = new MarketSimulator(props);
    	MarketBar mb = new MarketBar(0, props.getStartPrice(), 0, 0, 0, new Date().getTime(), 0, 0, false);
    	
        List<MarketBar> allBars = new ArrayList<>();
        allBars.add(mb);
        for(int i = 0; i < props.getTotalPeriods(); i++)
        {
            MarketTrend trend = new MarketTrend(props, i, allBars.get(allBars.size() - 1));
	        List<MarketBar> bars = simulator.generateBars(trend);
	        allBars.addAll(bars);
        }
        allBars.remove(0);
	    int idx = 0;
	    for(int i = 0; i < props.getDuration().length; i++)
	    {
	    	System.out.println(
	    			String.format("\n*** New period: duration %d - volatility %4.2f - start price %8.2f - target %8.2f", 
		  						  props.getDuration()[i],
								  props.getVolatility()[i],
								  allBars.get(idx).open,
								  allBars.get(idx).open + allBars.get(i).open * props.getVolatility()[i] / 100
    						  ));
		    System.out.println(String.format("%-14.14s %10.8s %10.8s %10.8s %10.8s %10.8s %12.12s %6.6s", 
	                "Time", "Open", "High", "Low", "Close", "Volume", "Applied Vol", "Trend"));
	    	for(int y = 0; y < props.getDuration()[i]; y++)
	    	{
	            System.out.println(allBars.get(idx++));
	    	}
	    }
    }
}
