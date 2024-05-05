package it.l_soft.barsGenerator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Random;

import org.apache.log4j.Logger;

public class ApplicationProperties {
	private static ApplicationProperties instance = null;
	final Logger log = Logger.getLogger(this.getClass());
	
	private String decimalSeparator;
	private String fieldSeparator;

	private int numOfBlocks;
	private int	maxBarSize;
	private int barsIntervalInMinutes;
	private int initialVolume;
	private boolean sameHighAndLowDepth;
	private boolean useRandomOnBothHighAndLow;
	private boolean forceConvergenceOnLastBar;
	private double[] shadowSizeInBarPercentage;
	private double probabilityToEnterTrend;
	private double considerApproachingEndOfTrend;
	private String startDate;
	private double marketOpenedHours;
	private int totalNumberOfPeriodsToGenerate;
	private boolean blocksSequenceRandom;
	private int[] blocksSequence;
	private double startPrice;
	private double[] barsShadow_numOfBarsPercentage;
	private double[] barsShadow_averageBarSizePercentage;
	private Block[] blocks;
	private Random rand;
	private static String propertiesPath = null;

	public static ApplicationProperties getInstance(String propPath)
	{
		if (propPath != null)
		{
			propertiesPath = propPath;
		}

		if (instance == null)
		{
			instance = new ApplicationProperties();
		}
		return(instance);
	}
	
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
		rand = new Random(System.currentTimeMillis());

		log.trace("ApplicationProperties start");
		Properties properties = new Properties();
		
    	try 
    	{
        	InputStream in;
        	if (propertiesPath == null)
        	{
        		in = ApplicationProperties.class.getResourceAsStream("/package.properties");
        	    System.out.println("ApplicationPropertes using package props'");
        	}
        	else
        	{
        		String confFilePath = System.getProperty("user.dir") + File.separator + propertiesPath;
        		
        	    File initialFile = new File(confFilePath);
        	    System.out.println("ApplicationPropertes using '" + confFilePath + "'");

        	    in = new FileInputStream(initialFile);
        	}
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
			variable = "decimalSeparator";
			try {
				decimalSeparator = properties.getProperty(variable).trim();			}
			catch(Exception e)
			{
				decimalSeparator = null;
			}
			variable = "fieldSeparator";
			try {
				fieldSeparator = properties.getProperty(variable).trim();
			}
			catch(Exception e)
			{
				fieldSeparator = null;
			}
			variable = "numOfBlocks";
			numOfBlocks = Integer.parseInt(properties.getProperty(variable).trim());
			variable = "barsIntervalInMinutes";
			barsIntervalInMinutes = Integer.parseInt(properties.getProperty(variable).trim());
			variable = "maxBarSize";
			maxBarSize = Integer.parseInt(properties.getProperty(variable).trim());
			variable = "sameHighAndLowDepth";
			sameHighAndLowDepth = Boolean.parseBoolean(properties.getProperty(variable).trim());
			variable = "useRandomOnBothHighAndLow";
			useRandomOnBothHighAndLow = Boolean.parseBoolean(properties.getProperty(variable).trim());
			variable = "forceConvergenceOnLastBar";
			forceConvergenceOnLastBar = Boolean.parseBoolean(properties.getProperty(variable).trim());
			variable = "startPrice";
	        startPrice = Double.parseDouble(properties.getProperty(variable).trim());
	        variable = "initialVolume";
	        initialVolume = Integer.parseInt(properties.getProperty(variable).trim());
	        variable = "marketOpenedHours";
	        marketOpenedHours = Double.parseDouble(properties.getProperty(variable).trim()) / 100;
	        variable = "considerApproachingEndOfTrend";
	        considerApproachingEndOfTrend = Double.parseDouble(properties.getProperty(variable).trim()) / 100;
	        variable = "probabilityToEnterTrend";
	        probabilityToEnterTrend = Double.parseDouble(properties.getProperty(variable).trim());
	        variable = "totalNumberOfPeriodsToGenerate";
	        totalNumberOfPeriodsToGenerate = Integer.parseInt(properties.getProperty(variable).trim());
	        variable = "startDate";
	        startDate = properties.getProperty(variable).trim();
	        variable = "blocksSequenceRandom";
	        blocksSequenceRandom = Boolean.parseBoolean(properties.getProperty(variable).trim());
	        
	        variable = "shadowSizeInBarPercentage";
			values = properties.getProperty(variable).split(",");
			shadowSizeInBarPercentage = new double[values.length];
			for(int i = 0; i < values.length; i++)
			{
				shadowSizeInBarPercentage[i] = Double.parseDouble(values[i].trim());
			}

			variable = "blocksSequence";
			values = properties.getProperty(variable).split(",");
			blocksSequence = new int[blocksSequenceRandom ? totalNumberOfPeriodsToGenerate : values.length];
			for(int i = 0; i < values.length; i++)
			{
				blocksSequence[i] = Integer.parseInt(values[i].trim());
			}
			if (blocksSequenceRandom)
			{
				for(int i = values.length; i < totalNumberOfPeriodsToGenerate; i++)
				{
					blocksSequence[i] = rand.nextInt(numOfBlocks) + 1;
				}
			}
			else
			{
				totalNumberOfPeriodsToGenerate = values.length;
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
				blocks[i - 1] = new Block(iValue, dValue, i);
				blocks[i - 1].pushTrend(new Trend(), 0);
				for(int y = 1; y <= iValue; y++)
				{
					Trend trend = new Trend();
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

	public String getDecimalSeparator() {
		return decimalSeparator;
	}

	public String getFieldSeparator() {
		return fieldSeparator;
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

	public boolean getBlocksSequenceRandom() {
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

	public double getShadowSizeInBarPercentage(int index) {
		return shadowSizeInBarPercentage[index] / 100;
	}

	public double getConsiderApproachingEndOfTrend() {
		return considerApproachingEndOfTrend;
	}

	public boolean getUseRandomOnBothHighAndLow() {
		return useRandomOnBothHighAndLow;
	}

	public Random getRand() {
		return rand;
	}

	public boolean getForceConvergenceOnLastBar() {
		return forceConvergenceOnLastBar;
	}
}