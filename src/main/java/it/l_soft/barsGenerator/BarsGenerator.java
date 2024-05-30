package it.l_soft.barsGenerator;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.Thread.State;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

public class BarsGenerator {
	static final Logger log = Logger.getLogger(BarsGenerator.class);

	public static void main(String[] args) throws ParseException, IOException, InvalidFormatException, CloneNotSupportedException {
		ApplicationProperties props;

		if ((args.length > 0) && (args[0].compareTo("-f") == 0))
		{
			props = ApplicationProperties.getInstance(args[1]);
		}
		else
		{
			props = ApplicationProperties.getInstance();
		}
    	MarketSimulator simulator = new MarketSimulator();
    	SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    	System.out.println("Simulation startin on date: " + props.getStartDate());
    	System.out.println("Writing results in: " + System.getProperty("user.dir"));
    	log.debug("\n\n\n------------> Process starts on " + props.getStartDate() + 
    			  " <------------");
    	Date startTimeStamp = sdf.parse(props.getStartDate() + " " + props.getStartTime());
    	Calendar cal = Calendar.getInstance();
    	cal.setTime(startTimeStamp);
    	cal.add(Calendar.DAY_OF_YEAR, -1);
    	cal.set(Calendar.HOUR_OF_DAY, (int)(props.getMarketOpenedHours() + props.getMktOpenTime()[0]));
    	cal.set(Calendar.MINUTE, props.getMktOpenTime()[1]);
    	cal.set(Calendar.SECOND, props.getMktOpenTime()[2]);
    	startTimeStamp = cal.getTime();

    	MarketBar mb = new MarketBar(startTimeStamp.getTime(), - props.getBarsIntervalInMinutes() * 60000, 0, 0);
    	mb.setClose(props.getStartPrice());
    	mb.setOpen(props.getStartPrice());
    	
        List<MarketBar> allBars = new ArrayList<>();
        List<Block> period = new ArrayList<>();
        allBars.add(mb);
        int i = 0;
        if (props.getPublishData())
        {
        	System.out.println("Wating for a client to connect...");
        	// wait for at least 1 client to be connected
        	while(!simulator.isAnyClientConnected())
        	{
        		try {
					Thread.sleep(1000);
				} 
        		catch (InterruptedException e) {
        			System.out.println("Wait for clients to connect aborted. Quitting the execution");
        			System.exit(0);
        		}
        	}
        	System.out.println("Client connected, let's go!");
        }

    	for(i = 0; i < props.getTotalNumberOfPeriodsToGenerate(); i++)
        {
			Block blockToRun;
			blockToRun = props.getBlock(props.getBlocksSequence()[i] - 1).clone();
	        List<MarketBar> bars = simulator.blockHandler(blockToRun, mb.getOpen(), mb.getClose(), mb.getTimestamp());
	        period.add(blockToRun);        
	        allBars.addAll(bars);
	        
	        mb = bars.get(bars.size() - 1);
        }
        allBars.remove(0);
        
		if (props.getPublishData())
		{
			log.trace("The server was running in publishing mode. End of duties.");
			simulator.publisher.interrupt();
			int count = 0;
			log.trace("Waiting for publisher to terminate");
			while((simulator.publisher.getState() != State.TERMINATED) && (count < 10))
			{
				try {
					Thread.sleep(1000);
					count++;
				}
				catch (InterruptedException e) {
					;
				}
				log.trace("*");
			}
			System.exit(0);
		}

        String pathToSave = 
        		(props.getCSVArchiveFolderPath() != null ? 
        				props.getCSVArchiveFolderPath()  + File.separator : 
        				System.getProperty("user.dir") + File.separator + "output" + File.separator);
        
	    String runExtension = new SimpleDateFormat("yyMMdd_HHmmss").format(new Date());
        String outFileName = 
        		(props.getOutputFileNamePreamble() != null ? props.getOutputFileNamePreamble() : "tradiaBars"); 

	    PrintWriter tradiaWriter = new PrintWriter(pathToSave + outFileName + "_" + runExtension + ".csv", "UTF-8");
	    int barIdx = 0;
	    int pCount = 0;
	    for(Block block: period)
	    {
	    	log.debug("\n\nBlock " + pCount++ + " -- used blckId: " + block.getId());
	    	for(i = 1; i < block.getTrends().length; i++)
	    	{
	    		Trend trend = block.getTrend(i);
		    	log.debug(
		    			String.format("*** New trend: direction %s - duration %d - open %8.2f - target %8.2f - close %8.2f", 
			  						  (trend.direction == 1 ? "long" : (trend.direction == 0 ? "lateral" : "short")),
		    						  trend.duration,
									  trend.openPrice,
									  trend.targetPrice,
									  trend.closePrice
	    						  ));
		    	log.debug(String.format("%-14.14s %10.8s %10.8s %10.8s %10.8s %10.8s %12.12s %6.6s", 
		                "Time", "Open", "High", "Low", "Close", "Volume", "Applied Vol", "Trend"));
		    	log.debug(allBars.get(barIdx));
		    	for(int y = 0; y < trend.duration; y++)
		    	{
		    	    tradiaWriter.println(allBars.get(barIdx++).tradiaOutput());
		    	}
		    	log.debug(allBars.get(barIdx -1 ));
	    	}
	    }
	    tradiaWriter.close();
	    
	    
	    ExcelOutputHandler excel = new ExcelOutputHandler(runExtension);

	    excel.writeHeaderRows(period);
	    excel.writeDataRows(allBars);
	    excel.writeChanges();
    }
}
