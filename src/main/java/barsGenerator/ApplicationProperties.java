package barsGenerator;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

import barsGenerator.Block.Trend;

public class ApplicationProperties {
	private static ApplicationProperties instance = null;
	final Logger log = Logger.getLogger(this.getClass());
	
	private int numOfBlocks = 6;
	private int	maxBarSize = 100;
	private int barsIntervalInMinutes = 30;
	private int initialVolume;
	private boolean sameHighAndLowDepth;
	private double shadowSizeInBarPercentage;
	private double probabilityToEnterTrend = .08;
	private double considerApproachingEndOfTrend;
	private String startDate = "04/10/2021 00:00";
	private double marketOpenedHours = 24;
	private int totalNumberOfPeriodsToGenerate = 6;
	private boolean blocksSequenceRandom = false;
	private int[] blocksSequence = {1, 2, 3, 4, 5, 6};
	private double startPrice = 1000;
	private double[] barsShadow_numOfBarsPercentage = {.10, .80, .95, .99, 1};
	private double[] barsShadow_averageBarSizePercentage = {.05, .30, .04, .70, 1.00};
	private Block[] blocks;

	public static ApplicationProperties getInstance()
	{
		if (instance == null)
		{
			instance = new ApplicationProperties();
		}
		return(instance);
	}
	
	private ApplicationProperties()
	{
		String variable = "";
		String[] values;
		log.trace("ApplicationProperties start");
		Properties properties = new Properties();
		log.debug("path of abs / '" + ApplicationProperties.class.getResource("/").getPath() + "'");
		
    	try 
    	{
    		log.debug("application path '" + ApplicationProperties.class.getResource("/").getPath() + "'");
    		log.debug("path of abs package.properties '" + 
  				  ApplicationProperties.class.getResource("/package.properties").getPath() + "'");
        	InputStream in = ApplicationProperties.class.getResourceAsStream("/package.properties");
        	if (in == null)
        	{
        		log.error("resource path not found");
        		return;
        	}
        	properties.load(in);
	    	in.close();
		}
    	catch(IOException e) 
    	{
			log.warn("Exception " + e.getMessage(), e);
    		return;
		}
    	
		try
    	{
			variable = "numOfBlocks";
			numOfBlocks = Integer.parseInt(properties.getProperty("numOfBlocks").trim());
			variable = "barsIntervalInMinutes";
			barsIntervalInMinutes = Integer.parseInt(properties.getProperty("barsIntervalInMinutes").trim());
			variable = "maxBarSize";
			maxBarSize = Integer.parseInt(properties.getProperty("maxBarSize").trim());
			variable = "sameHighAndLowDepth";
			sameHighAndLowDepth =  Boolean.parseBoolean(properties.getProperty("sameHighAndLowDepth").trim());
			variable = "startPrice";
	        startPrice = Double.parseDouble(properties.getProperty("startPrice").trim());
	        variable = "marketOpenedHours";
	        marketOpenedHours = Double.parseDouble(properties.getProperty("marketOpenedHours").trim()) / 100;
	        variable = "shadowSizeInBarPercentage";
	        shadowSizeInBarPercentage = Double.parseDouble(properties.getProperty("shadowSizeInBarPercentage").trim()) / 100;
	        variable = "considerApproachingEndOfTrend";
	        considerApproachingEndOfTrend = Double.parseDouble(properties.getProperty("considerApproachingEndOfTrend").trim()) / 100;
	        variable = "probabilityToEnterTrend";
	        probabilityToEnterTrend = Double.parseDouble(properties.getProperty("probabilityToEnterTrend").trim());
	        variable = "totalNumberOfPeriodsToGenerate";
	        totalNumberOfPeriodsToGenerate = Integer.parseInt(properties.getProperty("totalNumberOfPeriodsToGenerate").trim());
	        variable = "startDate";
	        startDate = properties.getProperty("startDate").trim();
	        variable = "blocksSequenceRandom";
	        blocksSequenceRandom = Boolean.parseBoolean(properties.getProperty("startDate").trim());
	        
	        variable = "blocksSequence";
			values = properties.getProperty("blocksSequence").split(",");
			blocksSequence = new int[values.length];
			for(int i = 0; i < values.length; i++)
			{
				blocksSequence[i] = Integer.parseInt(values[i].trim());
			}
			
			variable = "barsShadow_numOfBarsPercentage";
			values = properties.getProperty("barsShadow.numOfBarsPercentage").split(",");
			barsShadow_numOfBarsPercentage = new double[values.length];
			for(int i = 0; i < values.length; i++)
			{
				barsShadow_numOfBarsPercentage[i] = Double.parseDouble(values[i].trim()) / 100;
			}
			
			variable = "barsShadow_averageBarSizePercentage";
			values = properties.getProperty("barsShadow.averageBarSizePercentage").split(",");
			barsShadow_averageBarSizePercentage = new double[values.length];
			for(int i = 0; i < values.length; i++)
			{
				barsShadow_averageBarSizePercentage[i] = Double.parseDouble(values[i].trim()) / 100;
			}
			
			blocks = new Block[numOfBlocks];
			for(int i = 1; i <= numOfBlocks; i++)
			{
		        variable = "B" + i + ".numOfTrendsInBlock";
		        int iValue = Integer.parseInt(properties.getProperty(variable).trim());
		        variable = "B" + i + ".maxIntrabarVol";
		        double dValue = Double.parseDouble(properties.getProperty(variable).trim()) / 100.0;
				blocks[i - 1] = new Block(iValue, dValue);
				blocks[i - 1].pushTrend(blocks[i - 1].new Trend(), 0);
				for(int y = 1; y <= iValue; y++)
				{
					Trend trend = blocks[i - 1].new Trend();
			        variable = "B" + i + ".T" + y +".direction";
			        trend.direction = Integer.parseInt(properties.getProperty(variable).trim());
			        
			        variable = "B" + i + ".T" + y +".duration";
			        trend.duration = Integer.parseInt(properties.getProperty(variable).trim());
			        
			        variable = "B" + i + ".T" + y +".deltaPoints";
			        trend.deltaPoints = Integer.parseInt(properties.getProperty(variable).trim()) * 
			        									 (trend.direction != 0 ? trend.direction : 1);
			        
			        variable = "B" + i + ".T" + y +".enableMiniTrends";
			        trend.enableMiniTrends = Boolean.parseBoolean(properties.getProperty(variable).trim());
			        
			        variable = "B" + i + ".T" + y +".maxBarPerTrend";
			        trend.maxBarPerTrend = Integer.parseInt(properties.getProperty(variable).trim());
			        					
			        variable = "B" + i + ".T" + y +".minBarPerTrend";
			        trend.minBarPerTrend = Integer.parseInt(properties.getProperty(variable).trim());
			        
			        blocks[i - 1].pushTrend(trend, y);
				}
				
			}
			
    	}
    	catch(NumberFormatException e)
    	{
    		log.error("The format for the variable '" + variable + "' is incorrect (" +
    					 properties.getProperty("sessionExpireTime") + ")", e);
    	}		
	}

	public int getNumOfBlocks() {
		return numOfBlocks;
	}

	public int getBarsIntervalInMinutes() {
		return barsIntervalInMinutes;
	}

	public int getMaxBarSize() {
		return maxBarSize;
	}

	public int getInitialVolume() {
		return initialVolume;
	}

	public double getProbabilityToEnterTrend() {
		return probabilityToEnterTrend / 100;
	}

	public String getStartDate() {
		return startDate;
	}

	public double getMarketOpenedHours() {
		return marketOpenedHours;
	}

	public int getTotalNumberOfPeriodsToGenerate() {
		return totalNumberOfPeriodsToGenerate;
	}

	public boolean isBlocksSequenceRandom() {
		return blocksSequenceRandom;
	}

	public int[] getBlocksSequence() {
		return blocksSequence;
	}

	public double getStartPrice() {
		return startPrice;
	}

	public double[] getBarsShadowNumOfBarsPercentage() {
		return barsShadow_numOfBarsPercentage;
	}

	public double[] getBarsShadowAverageBarSizePercentage() {
		return barsShadow_averageBarSizePercentage;
	}

	public Block getBlock(int index) {
		return blocks[index];
	}

	public Block[] getBlocks() {
		return blocks;
	}

	public boolean getSameHighAndLowDepth() {
		return sameHighAndLowDepth;
	}

	public double getShadowSizeInBarPercentage() {
		return shadowSizeInBarPercentage;
	}

	public double getConsiderApproachingEndOfTrend() {
		return considerApproachingEndOfTrend;
	}
	
}