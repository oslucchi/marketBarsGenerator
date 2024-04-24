package barsGenerator;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class BarsGenerator {
    public static void main(String[] args) throws ParseException, FileNotFoundException, UnsupportedEncodingException {
    	ApplicationProperties props = ApplicationProperties.getInstance();
    	MarketSimulator simulator = new MarketSimulator();
    	SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    	System.out.println(props.getStartDate());
    	System.out.println(sdf.format(sdf.parse(props.getStartDate())));
    	MarketBar mb = new MarketBar(sdf.parse(props.getStartDate()).getTime() - props.getInterval(), 0, 0, 0);
    	mb.setClose(props.getStartPrice());
    	
        List<MarketBar> allBars = new ArrayList<>();
        allBars.add(mb);
        for(int i = 0; i < props.getTotalPeriods(); i++)
        {
            MarketTrend mt = new MarketTrend(i);
            mt.setTimestampStart(allBars.get(allBars.size() - 1).getTimestamp());
            mt.setStartPrice(allBars.get(allBars.size() - 1).getClose());
	        List<MarketBar> bars = simulator.periodHandler(mt);
	        allBars.addAll(bars);
        }
        allBars.remove(0);
	    int idx = 0;
	    PrintWriter tradiaWriter = new PrintWriter("/tmp/tradiaBars.csv", "UTF-8");
	    PrintWriter localWriter = new PrintWriter("/tmp/bars.csv", "UTF-8");
	    for(int i = 0; i < props.getDuration().length; i++)
	    {
	    	System.out.println(
	    			String.format("\n*** New period: duration %d - volatility %4.2f - start price %8.2f - target %8.2f", 
		  						  props.getDuration()[i],
								  props.getVolatility()[i],
								  allBars.get(idx).getOpen(),
								  allBars.get(idx).getOpen() + allBars.get(i).getOpen() * props.getVolatility()[i] / 100
    						  ));
		    System.out.println(String.format("%-14.14s %10.8s %10.8s %10.8s %10.8s %10.8s %12.12s %6.6s", 
	                "Time", "Open", "High", "Low", "Close", "Volume", "Applied Vol", "Trend"));
            System.out.println(allBars.get(idx));
	    	for(int y = 0; y < props.getDuration()[i]; y++)
	    	{
	    	    localWriter.println(allBars.get(idx).csvOutput());
	    	    tradiaWriter.println(allBars.get(idx++).tradiaOutput());
	    	}
    		System.out.println(allBars.get(idx -1 ));
	    }
	    localWriter.close();
	    tradiaWriter.close();
    }
}
