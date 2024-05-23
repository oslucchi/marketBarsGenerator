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
	
	private String outputFileNamePreamble;
	private String CSVArchiveFolderPath;
	private String ExcelArchiveFolderPath;
	
	private String decimalSeparator;
	private String fieldSeparator;

	private int numOfBlocks;
	private double	marketTick;
	private int barsIntervalInMinutes;
	private int initialVolume;
	private boolean sameHighAndLowDepth;
	private boolean useRandomOnBothHighAndLow;
	private boolean forceConvergenceOnLastBar;
	private boolean capIntradayVol;
	private double[] shadowSize_numOfBarsPercentage;
	private double[] shadowSize_averageBarSizePercentage;
	private double probabilityToEnterTrend;
	private int barsToEndOfTrend;
	private double shadowSizeToFollowTrendDirectionAt;
	private String startDate;
	private String startTime;
	private double marketOpenedHours;
	private int totalNumberOfPeriodsToGenerate;
	private boolean blocksSequenceRandom;
	private int[] blocksSequence;
	private double startPrice;
	private double[] barsSize_numOfBarsPercentage;
	private double[] barsSize_averageBarSizePercentage;
	private double[] intradayVolDistrPerc;
	private double[] intradayVolDistValue;
	private Double maxIntradayVol;
	private int[] mktOpenTime = new int[3];
	private boolean useCurrentBarSizeAsReferenceForShadows;
	
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
				decimalSeparator = properties.getProperty(variable).trim();		
			}
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
			
			variable = "outputFileNamePreamble";
			try {
				outputFileNamePreamble = properties.getProperty(variable).trim();
			}
			catch(Exception e)
			{
				outputFileNamePreamble = null;
			}
			variable = "CSVArchiveFolderPath";
			try {
				CSVArchiveFolderPath = properties.getProperty(variable).trim();
			}
			catch(Exception e)
			{
				CSVArchiveFolderPath = null;
			}
			variable = "ExcelArchiveFolderPath";
			try {
				ExcelArchiveFolderPath = properties.getProperty(variable).trim();
			}
			catch(Exception e)
			{
				ExcelArchiveFolderPath = null;
			}

			variable = "numOfBlocks";
			numOfBlocks = Integer.parseInt(properties.getProperty(variable).trim());
			variable = "barsIntervalInMinutes";
			barsIntervalInMinutes = Integer.parseInt(properties.getProperty(variable).trim());
			variable = "marketTick";
			marketTick = Double.parseDouble(properties.getProperty(variable).trim());
			variable = "sameHighAndLowDepth";
			sameHighAndLowDepth = Boolean.parseBoolean(properties.getProperty(variable).trim());
			variable = "useRandomOnBothHighAndLow";
			useRandomOnBothHighAndLow = Boolean.parseBoolean(properties.getProperty(variable).trim());
			variable = "forceConvergenceOnLastBar";
			forceConvergenceOnLastBar = Boolean.parseBoolean(properties.getProperty(variable).trim());
			variable = "startPrice";
	        startPrice = Double.parseDouble(properties.getProperty(variable).trim());
	        variable = "shadowSizeToFollowTrendDirectionAt";
	        shadowSizeToFollowTrendDirectionAt = 
	        		Double.parseDouble(properties.getProperty(variable).trim()) / 100;
	        variable = "initialVolume";
	        initialVolume = Integer.parseInt(properties.getProperty(variable).trim());
	        variable = "marketOpenedHours";
	        marketOpenedHours = Double.parseDouble(properties.getProperty(variable).trim());
	        variable = "barsToEndOfTrend";
	        barsToEndOfTrend = Integer.parseInt(properties.getProperty(variable).trim()) / 100;
	        variable = "maxIntradayVol";
	        maxIntradayVol = Double.parseDouble(properties.getProperty(variable).trim()) / 100;
	        variable = "probabilityToEnterTrend";
	        probabilityToEnterTrend = Double.parseDouble(properties.getProperty(variable).trim());
	        variable = "totalNumberOfPeriodsToGenerate";
	        totalNumberOfPeriodsToGenerate = Integer.parseInt(properties.getProperty(variable).trim());
	        variable = "startDate";
	        startDate = properties.getProperty(variable).trim();
	        variable = "startTime";
	        startTime = properties.getProperty(variable).trim();

	        variable = "blocksSequenceRandom";
	        blocksSequenceRandom = Boolean.parseBoolean(properties.getProperty(variable).trim());
	        
	        variable = "shadowSize.numOfBarsPercentage";
			values = properties.getProperty(variable).split(",");
			shadowSize_numOfBarsPercentage = new double[values.length];
			for(int i = 0; i < values.length; i++)
			{
				shadowSize_numOfBarsPercentage[i] = Double.parseDouble(values[i].trim()) / 100;
			}

	        variable = "shadowSize.averageBarSizePercentage";
			values = properties.getProperty(variable).split(",");
			shadowSize_averageBarSizePercentage = new double[values.length];
			for(int i = 0; i < values.length; i++)
			{
				shadowSize_averageBarSizePercentage[i] = Double.parseDouble(values[i].trim()) / 100;
			}

			variable = "barsSize_numOfBarsPercentage";
			values = properties.getProperty("barsSize.numOfBarsPercentage").split(",");
			barsSize_numOfBarsPercentage = new double[values.length];
			for(int i = 0; i < values.length; i++)
			{
				barsSize_numOfBarsPercentage[i] = Double.parseDouble(values[i].trim()) / 100;
			}
			
			variable = "barsSize_averageBarSizePercentage";
			values = properties.getProperty("barsSize.averageBarSizePercentage").split(",");
			barsSize_averageBarSizePercentage = new double[values.length];
			for(int i = 0; i < values.length; i++)
			{
				barsSize_averageBarSizePercentage[i] = Double.parseDouble(values[i].trim()) / 100;
			}
			
			variable = "intradayVolDistrPerc";
			values = properties.getProperty(variable).split(",");
			intradayVolDistrPerc = new double[values.length];
			for(int i = 0; i < values.length; i++)
			{
				intradayVolDistrPerc[i] = Double.parseDouble(values[i].trim()) / 100;
			}
			variable = "intradayVolDistValue";
			values = properties.getProperty(variable).split(",");
			intradayVolDistValue = new double[values.length];
			for(int i = 0; i < values.length; i++)
			{
				intradayVolDistValue[i] = Double.parseDouble(values[i].trim()) / 100;
			}
			

			blocks = new Block[numOfBlocks];
			for(int i = 1; i <= numOfBlocks; i++)
			{
		        variable = "B" + i + ".numOfTrendsInBlock";
		        int iValue = Integer.parseInt(properties.getProperty(variable).trim());
				blocks[i - 1] = new Block(iValue, 0, i);
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
			        variable = "B" + i + ".T" + y + ".lateralBounceAtDeltaPoints";
			        try {
				        trend.lateralBounceAtDeltaPoints = Integer.parseInt(properties.getProperty(variable).trim());
			        }
			        catch(Exception e)
			        {
			        	if (trend.direction == 0)
		        		{
			        		trend.lateralBounceAtDeltaPoints = startPrice * .03;
		        		}
			        	else
		        		{
			        		trend.lateralBounceAtDeltaPoints = 0;
		        		}
			        }
			        variable = "B" + i + ".T" + y + ".maxBarSize";
			        trend.maxBarSize = (double) Integer.parseInt(properties.getProperty(variable).trim());
			        
			        variable = "B" + i + ".T" + y + ".barSizeAmplifier";
			        try {
				        trend.barSizeAmplifier = Double.parseDouble(properties.getProperty(variable).trim());
			        }
			        catch(Exception e)
			        {
			        	trend.barSizeAmplifier = 1;
			        }
			        
			        variable = "B" + i + ".T" + y + ".shadowSizeAmplifier";
			        try {
				        trend.shadowSizeAmplifier = Double.parseDouble(properties.getProperty(variable).trim());
			        }
			        catch(Exception e)
			        {
			        	trend.shadowSizeAmplifier = 1;
			        }
			        
			        variable = "B" + i + ".T" + y +".capIntradayVol";
			        trend.capIntradayVol = Boolean.parseBoolean(properties.getProperty(variable).trim());
			        
			        variable = "B" + i + ".T" + y +".enableMiniTrends";
			        trend.enableMiniTrends = Boolean.parseBoolean(properties.getProperty(variable).trim());
			        
			        variable = "B" + i + ".T" + y +".maxBarPerTrend";
			        trend.maxBarPerTrend = Integer.parseInt(properties.getProperty(variable).trim());
			        					
			        variable = "B" + i + ".T" + y +".minBarPerTrend";
			        trend.minBarPerTrend = Integer.parseInt(properties.getProperty(variable).trim());
			        
			        blocks[i - 1].pushTrend(trend, y);
				}
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
					int blockId = 0;		
					while(true)
					{
						blockId = rand.nextInt(numOfBlocks);
						Trend[] prevBlockTrends = blocks[blocksSequence[i - 1]].getTrends();
						
						if ((prevBlockTrends[prevBlockTrends.length - 1].direction == 0) &&
							(blocks[blockId].getTrends()[1].direction == 0))
						{
							continue;
						}
						break;
					}
					
					blocksSequence[i] = blockId;
				}
			}
			else
			{
				totalNumberOfPeriodsToGenerate = values.length;
			}
    	}
    	catch(NumberFormatException e)
    	{
    		log.error("The format for the variable '" + variable + "' is incorrect (" +
    					 properties.getProperty("sessionExpireTime") + ")", e);
    		System.out.println("The format for the variable '" + variable + "' is incorrect (" +
					 properties.getProperty("sessionExpireTime") + ")");
    		System.exit(-1);
    	}
        mktOpenTime[0] = Integer.parseInt(startTime.substring(0, 2));
        mktOpenTime[1] = Integer.parseInt(startTime.substring(3, 5));
        mktOpenTime[2] = Integer.parseInt(startTime.substring(6, 8));
        
        // optional parameters
        try {
	        variable = "useCurrentBarSizeAsReferenceForShadows";
        	useCurrentBarSizeAsReferenceForShadows = false;
	        if (properties.getProperty(variable) != null)
	        {
		        useCurrentBarSizeAsReferenceForShadows = Boolean.parseBoolean(properties.getProperty(variable).trim());
	        }
        }
        catch(Exception e)
        {
    		log.error("The format for the variable '" + variable + "' is incorrect (" +
					 properties.getProperty("sessionExpireTime") + ")", e);
			System.out.println("The format for the variable '" + variable + "' is incorrect (" +
					 properties.getProperty("sessionExpireTime") + ")");
			System.exit(-1);
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

	public double getMarketTick() {
		return marketTick;
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

	public double[] getBarsSizeNumOfBarsPercentage() {
		return barsSize_numOfBarsPercentage;
	}

	public double[] getBarsSizeAverageBarSizePercentage() {
		return barsSize_averageBarSizePercentage;
	}

	public double getBarsSizeNumOfBarsPercentage(int index) {
		return barsSize_numOfBarsPercentage[index];
	}

	public double getBarsSizeAverageBarSizePercentage(int index) {
		return barsSize_averageBarSizePercentage[index];
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

	public double[] getShadowSizeNumOfBarsPercentage() {
		return shadowSize_numOfBarsPercentage;
	}

	public double[] getShadowSizeAverageBarSizePercentage() {
		return shadowSize_averageBarSizePercentage;
	}

	public double getShadowSizeNumOfBarsPercentage(int index) {
		return shadowSize_numOfBarsPercentage[index];
	}

	public double getShadowSizeAverageBarSizePercentage(int index) {
		return shadowSize_averageBarSizePercentage[index];
	}

	public int getBarsToEndOfTrend() {
		return barsToEndOfTrend;
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

	public String getCSVArchiveFolderPath() {
		return CSVArchiveFolderPath;
	}
	
	public String getExcelArchiveFolderPath() {
		return ExcelArchiveFolderPath;
	}

	public String getOutputFileNamePreamble() {
		return outputFileNamePreamble;
	}

	public String getStartTime() {
		return startTime;
	}
	
	public int getStartMarketHour() {
		return Integer.parseInt(startTime.substring(0,2));
	}
	
	public int getStartMarketMinute() {
		return Integer.parseInt(startTime.substring(3,5));
	}

	public int getStartMarketSecond() {
		return Integer.parseInt(startTime.substring(6,8));
	}

	public double getIntradayVolDistrPerc(int index) {
		return intradayVolDistrPerc[index];
	}

	public double getIntradayVolDistValue(int index) {
		return intradayVolDistValue[index];
	}
	
	public double[] getIntradayVolDistrPerc() {
		return intradayVolDistrPerc;
	}

	public double[] getIntradayVolDistValue() {
		return intradayVolDistValue;
	}

	public boolean getCapIntradayVol() {
		return capIntradayVol;
	}

	public double getShadowSizeToFollowTrendDirectionAt() {
		return shadowSizeToFollowTrendDirectionAt;
	}

	public Double getMaxIntradayVol() {
		return maxIntradayVol;
	}

	public int[] getMktOpenTime() {
		return mktOpenTime;
	}

	public boolean getUseCurrentBarSizeAsReferenceForShadows() {
		return useCurrentBarSizeAsReferenceForShadows;
	}
	
}
