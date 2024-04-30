package barsGenerator;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

public class BarsGenerator {
    public static void main(String[] args) throws ParseException, IOException, InvalidFormatException {
    	ApplicationProperties props = ApplicationProperties.getInstance();
    	MarketSimulator simulator = new MarketSimulator();
    	SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    	System.out.println("Simulation startin on date: " + props.getStartDate());
    	System.out.println("Writing results in: " + System.getProperty("user.dir"));
    	MarketBar mb = new MarketBar(sdf.parse(props.getStartDate()).getTime() - props.getBarsIntervalInMinutes(), 0, 0, 0);
    	mb.setClose(props.getStartPrice());
    	
        List<MarketBar> allBars = new ArrayList<>();
        allBars.add(mb);
        for(int i = 0; i < props.getNumOfBlocks(); i++)
        {
	        List<MarketBar> bars = simulator.blockHandler(props.getBlock(i), props.getStartPrice(), mb.getTimestamp());
	        allBars.addAll(bars);
        }
        allBars.remove(0);
	    String pathToSave = System.getProperty("user.dir") + 
				    File.separator + "output" + File.separator;
	    String runExtension = new SimpleDateFormat("yyMMdd_HHmmss_").format(new Date());
//        ExcelOutputHandler excel = new ExcelOutputHandler(runExtension);

//	    excel.writeHeaderRows(periodTrends);
//	    excel.writeDataRows(allBars);
//	    excel.writeChanges();
	    
        PrintWriter localWriter = new PrintWriter(pathToSave + runExtension + "bars.csv", "UTF-8");
        String sep = "";
	    int idx = 0;
//	    for(MarketTrend mt : periodTrends)
//	    {
//    	    localWriter.print((sep + mt.getDuration()).replace(".", ","));
//    	    sep = "; ";
//	    }
//	    localWriter.println("");
//	    sep = "";
//	    for(MarketTrend mt : periodTrends)
//	    {
//    	    localWriter.print((sep + mt.getVolatility()).replace(".", ","));
//    	    sep = "; ";
//	    }
//	    localWriter.println("");
//	    sep = "";
//	    for(MarketTrend mt : periodTrends)
//	    {
//    	    localWriter.print((sep + mt.getMaxTrendsInPeriod() + "; " + mt.getMaxBarsInTrend()).replace(".", ","));
//    	    sep = "; ";
//	    }
	    for(int i = 0; i < 8; i++)
    	{
	    	localWriter.println("");
    	}
	    
	    PrintWriter tradiaWriter = new PrintWriter(pathToSave + runExtension + "tradiaBars.csv", "UTF-8");
//	    for(int i = 0; i < props.getDuration().length; i++)
//	    {
//	    	System.out.println(
//	    			String.format("\n*** New period: duration %d - volatility %4.2f - start price %8.2f - target %8.2f", 
//		  						  props.getDuration()[i],
//								  props.getVolatility()[i],
//								  allBars.get(idx).getOpen(),
//								  allBars.get(idx).getOpen() + allBars.get(i).getOpen() * props.getVolatility()[i] / 100
//    						  ));
//		    System.out.println(String.format("%-14.14s %10.8s %10.8s %10.8s %10.8s %10.8s %12.12s %6.6s", 
//	                "Time", "Open", "High", "Low", "Close", "Volume", "Applied Vol", "Trend"));
//            System.out.println(allBars.get(idx));
//	    	for(int y = 0; y < props.getDuration()[i]; y++)
//	    	{
//	    	    localWriter.println(allBars.get(idx).csvOutput());
//	    	    tradiaWriter.println(allBars.get(idx++).tradiaOutput());
//	    	}
//    		System.out.println(allBars.get(idx -1 ));
//	    }
	    localWriter.close();
	    tradiaWriter.close();
    }
}
