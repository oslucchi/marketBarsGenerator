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

import barsGenerator.Block.Trend;

public class BarsGenerator {
    public static void main(String[] args) throws ParseException, IOException, InvalidFormatException {
    	ApplicationProperties props = ApplicationProperties.getInstance();
    	MarketSimulator simulator = new MarketSimulator();
    	SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:SS");

    	System.out.println("Simulation startin on date: " + props.getStartDate());
    	System.out.println("Writing results in: " + System.getProperty("user.dir"));
    	MarketBar mb = new MarketBar(sdf.parse(props.getStartDate()).getTime() - props.getBarsIntervalInMinutes() * 60000, 0, 0, 0);
    	mb.setClose(props.getStartPrice());
    	mb.setOpen(props.getStartPrice());
    	
        List<MarketBar> allBars = new ArrayList<>();
        allBars.add(mb);
        for(int i = 0; i < props.getNumOfBlocks(); i++)
        {
			System.out.println("Block " + i);
	        List<MarketBar> bars = simulator.blockHandler(props.getBlock(i), mb.getOpen(), mb.getClose(), mb.getTimestamp());
	        allBars.addAll(bars);
	        mb = bars.get(bars.size() - 1);
        }
        allBars.remove(0);
        
        
	    String pathToSave = System.getProperty("user.dir") + 
				    File.separator + "output" + File.separator;
	    String runExtension = new SimpleDateFormat("yyMMdd_HHmmss_").format(new Date());

	    PrintWriter tradiaWriter = new PrintWriter(pathToSave + runExtension + "tradiaBars.csv", "UTF-8");
	    int barIdx = 0;
	    for(Block block: props.getBlocks())
	    {
	    	for(int i = 1; i < block.getTrends().length; i++)
	    	{
	    		Trend trend = block.getTrend(i);
		    	System.out.println(
		    			String.format("\n*** New trend: direction %s - duration %d - open %8.2f - target %8.2f - close %8.2f", 
			  						  (trend.direction == 1 ? "long" : (trend.direction == 0 ? "lateral" : "short")),
		    						  trend.duration,
									  trend.openPrice,
									  trend.targetPrice,
									  trend.closePrice
	    						  ));
			    System.out.println(String.format("%-14.14s %10.8s %10.8s %10.8s %10.8s %10.8s %12.12s %6.6s", 
		                "Time", "Open", "High", "Low", "Close", "Volume", "Applied Vol", "Trend"));
	            System.out.println(allBars.get(barIdx));
		    	for(int y = 0; y < trend.duration; y++)
		    	{
		    	    tradiaWriter.println(allBars.get(barIdx++).tradiaOutput());
		    	}
	    		System.out.println(allBars.get(barIdx -1 ));
	    	}
	    }
	    tradiaWriter.close();
	    
	    
	    ExcelOutputHandler excel = new ExcelOutputHandler(runExtension);

	    excel.writeHeaderRows(props.getBlocks());
	    excel.writeDataRows(allBars);
	    excel.writeChanges();
    }
}
