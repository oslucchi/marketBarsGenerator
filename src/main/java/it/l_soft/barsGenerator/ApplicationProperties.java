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

	private int barsIntervalInMinutes;
	private double marketTick;
	private int initialVolume;
	private boolean sameHighAndLowDepth;
	private boolean useRandomOnBothHighAndLow;
	private boolean forceConvergenceOnLastBar;
	private double[] shadowSize_numOfBarsPercentage;
	private double[] shadowSize_averageBarSizePercentage;
	private double probabilityToEnterTrend;
	private int barsToEndOfTrend;
	private double shadowSizeToFollowTrendDirectionAt;
	private String startDate;
	private String startTime;
	private double marketOpenedHours;
	private double startPrice;
	private double[] barsSize_numOfBarsPercentage;
	private double[] barsSize_averageBarSizePercentage;
	private double[] intradayVolDistrPerc;
	private double[] intradayVolDistValue;
	private Double maxIntradayVol;
	private int[] mktOpenTime = new int[3];
	private boolean useCurrentBarSizeAsReferenceForShadows;

	private int exchangeFromHour;
	private int exchangeFromMinute;
	private int exchangeToHour;
	private int exchangeToMinute;
	private boolean[] openDays;
	private java.util.List<String> holidays;

	private Trend[] trends;
	private Random rand;
	private static String propertiesPath = null;

	private boolean publishData;
	private int port;
	private String host;
	private String polygonApiKey;
	private long intraMessagePause;


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
	
	public static void reset()
	{
		instance = null;
		propertiesPath = null;
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
	String mktHoursVal = properties.getProperty(variable);
	marketOpenedHours = (mktHoursVal != null) ? Double.parseDouble(mktHoursVal.trim()) : 12;
	        variable = "barsToEndOfTrend";
	        barsToEndOfTrend = Integer.parseInt(properties.getProperty(variable).trim()) / 100;
	        variable = "maxIntradayVol";
	        maxIntradayVol = Double.parseDouble(properties.getProperty(variable).trim()) / 100;
	        variable = "probabilityToEnterTrend";
	        probabilityToEnterTrend = Double.parseDouble(properties.getProperty(variable).trim());
	        variable = "startDate";
	        startDate = properties.getProperty(variable).trim();
	        variable = "startTime";
	        startTime = properties.getProperty(variable).trim();

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

			variable = "barsSize.numOfBarsPercentage";
			values = properties.getProperty("barsSize.numOfBarsPercentage").split(",");
			barsSize_numOfBarsPercentage = new double[values.length];
			for(int i = 0; i < values.length; i++)
			{
				barsSize_numOfBarsPercentage[i] = Double.parseDouble(values[i].trim()) / 100;
			}
			
			variable = "barsSize.averageBarSizePercentage";
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
			
			// Read trend definitions
			variable = "numOfTrends";
			int numOfTrends = Integer.parseInt(properties.getProperty(variable).trim());
			trends = new Trend[numOfTrends];
			for (int i = 1; i <= numOfTrends; i++)
			{
				Trend trend = new Trend();
				trend.id = i;
				
				variable = "T" + i + ".duration";
				trend.duration = Integer.parseInt(properties.getProperty(variable).trim());
				
				variable = "T" + i + ".variationPoints";
				trend.variationPoints = Integer.parseInt(properties.getProperty(variable).trim());
				
				variable = "T" + i + ".enableMiniTrends";
				trend.enableMiniTrends = Boolean.parseBoolean(properties.getProperty(variable).trim());
				
				variable = "T" + i + ".maxBarsPerTrend";
				trend.maxBarsPerTrend = Integer.parseInt(properties.getProperty(variable).trim());
				
				variable = "T" + i + ".minBarsPerTrend";
				trend.minBarsPerTrend = Integer.parseInt(properties.getProperty(variable).trim());
				
				trends[i - 1] = trend;
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

			variable = "publishData";
			publishData = false;
	        if (properties.getProperty(variable) != null)
	        {
	        	publishData = Boolean.parseBoolean(properties.getProperty(variable).trim());
	        }		

			variable = "port";
			port = 12345;
	        if (properties.getProperty(variable) != null)
	        {
	        	port = Integer.parseInt(properties.getProperty(variable).trim());
	        }

			variable = "host";
			host = "";
	        if (properties.getProperty(variable) != null)
	        {
	        	host = properties.getProperty(variable).trim();
	        }

			variable = "POLYGON_API_KEY";
			polygonApiKey = "";
	        if (properties.getProperty(variable) != null)
	        {
	        	polygonApiKey = properties.getProperty(variable).trim();
	        }

			variable = "intraMessagePause";
			intraMessagePause = 1000;
	        if (properties.getProperty(variable) != null)
	        {
	        	intraMessagePause = Long.parseLong(properties.getProperty(variable).trim());
	        }

			variable = "exchangeFromTime";
			if (properties.getProperty(variable) != null)
			{
				String[] timeParts = properties.getProperty(variable).trim().split(":");
				exchangeFromHour = Integer.parseInt(timeParts[0].trim());
				exchangeFromMinute = Integer.parseInt(timeParts[1].trim());
			}
			else
			{
				exchangeFromHour = mktOpenTime[0];
				exchangeFromMinute = mktOpenTime[1];
			}

			variable = "exchangeToTime";
			if (properties.getProperty(variable) != null)
			{
				String[] timeParts = properties.getProperty(variable).trim().split(":");
				exchangeToHour = Integer.parseInt(timeParts[0].trim());
				exchangeToMinute = Integer.parseInt(timeParts[1].trim());
			}
			else
			{
				int totalMinutes = (int) (exchangeFromHour * 60 + exchangeFromMinute + marketOpenedHours * 60);
				exchangeToHour = totalMinutes / 60;
				exchangeToMinute = totalMinutes % 60;
			}

			variable = "openDays";
			openDays = new boolean[7];
			if (properties.getProperty(variable) != null)
			{
				String[] days = properties.getProperty(variable).trim().split(",");
				java.util.Map<String, Integer> dayMap = new java.util.HashMap<>();
				dayMap.put("Mo", java.util.Calendar.MONDAY - 1);
				dayMap.put("Tu", java.util.Calendar.TUESDAY - 1);
				dayMap.put("We", java.util.Calendar.WEDNESDAY - 1);
				dayMap.put("Th", java.util.Calendar.THURSDAY - 1);
				dayMap.put("Fr", java.util.Calendar.FRIDAY - 1);
				dayMap.put("Sa", java.util.Calendar.SATURDAY - 1);
				dayMap.put("Su", java.util.Calendar.SUNDAY - 1);
				for (String d : days)
				{
					Integer idx = dayMap.get(d.trim());
					if (idx != null) openDays[idx] = true;
				}
			}
			else
			{
				openDays[java.util.Calendar.MONDAY - 1] = true;
				openDays[java.util.Calendar.TUESDAY - 1] = true;
				openDays[java.util.Calendar.WEDNESDAY - 1] = true;
				openDays[java.util.Calendar.THURSDAY - 1] = true;
				openDays[java.util.Calendar.FRIDAY - 1] = true;
			}

			variable = "holidays";
			holidays = new java.util.ArrayList<>();
			if (properties.getProperty(variable) != null)
			{
				String[] h = properties.getProperty(variable).trim().split(",");
				for (String day : h)
				{
					holidays.add(day.trim());
				}
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

	public Trend[] getTrends() {
		return trends;
	}

	public Trend getTrend(int index) {
		return trends[index];
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

	public boolean getPublishData() {
		return publishData;
	}

	public int getPort() {
		return port;
	}

	public String getHost() {
		return host;
	}

	public String getPolygonApiKey() {
		return polygonApiKey;
	}

	public long getIntraMessagePause() {
		return intraMessagePause;
	}

	public int getExchangeFromHour() {
		return exchangeFromHour;
	}

	public int getExchangeFromMinute() {
		return exchangeFromMinute;
	}

	public int getExchangeToHour() {
		return exchangeToHour;
	}

	public int getExchangeToMinute() {
		return exchangeToMinute;
	}

	public boolean[] getOpenDays() {
		return openDays;
	}

	public java.util.List<String> getHolidays() {
		return holidays;
	}
}
