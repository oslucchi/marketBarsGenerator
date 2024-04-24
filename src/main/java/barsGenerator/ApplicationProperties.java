package barsGenerator;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

public class ApplicationProperties {
	private static ApplicationProperties instance = null;
	final Logger log = Logger.getLogger(this.getClass());
	
	private int totalPeriods = 1;
	private int[] duration;
	private double[] volatility;
	private double[] volumeTrend; 
	private double[] maxIntrabarVol;
	private double[] maxVolHighLow;
	private boolean[] enableTrends;
	private int[] maxBarsInTrend;
	private int[] maxTrendsInPeriod;
	private double initialVolume;
	private double startPrice;
	private long interval;
	private double probabilityToEnterTrend;
	private String startDate;
	
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
			variable = "duration";
			values = properties.getProperty("duration").split(",");
			duration = new int[values.length];
			for(int i = 0; i < values.length; i++)
			{
				duration[i] = Integer.parseInt(values[i].trim());
			}
			
			variable = "volatility";
			values = properties.getProperty("volatility").split(",");
			volatility = new double[values.length];
			for(int i = 0; i < values.length; i++)
			{
				volatility[i] = Double.parseDouble(values[i].trim());
			}
			
			variable = "volumeTrend";
			values = properties.getProperty("volumeTrend").split(",");
			volumeTrend = new double[values.length];
			for(int i = 0; i < values.length; i++)
			{
				volumeTrend[i] = Double.parseDouble(values[i].trim());
			}
			
			variable = "maxIntrabarVol";
			values = properties.getProperty("maxIntrabarVol").split(",");
			maxIntrabarVol = new double[values.length];
			for(int i = 0; i < values.length; i++)
			{
				maxIntrabarVol[i] = Double.parseDouble(values[i].trim());
			}
			
			variable = "maxVolHighLow";
			values = properties.getProperty("maxVolHighLow").split(",");
			maxVolHighLow = new double[values.length];
			for(int i = 0; i < values.length; i++)
			{
				maxVolHighLow[i] = Double.parseDouble(values[i].trim());
			}
			
			variable = "enbleTrends";
			values = properties.getProperty("enbleTrends").split(",");
			enableTrends = new boolean[values.length];
			for(int i = 0; i < values.length; i++)
			{
				enableTrends[i] = Boolean.parseBoolean(values[i].trim());
			}

			variable = "maxTrendsInPeriod";
			values = properties.getProperty("maxTrendsInPeriod").split(",");
			maxTrendsInPeriod = new int[values.length];
			for(int i = 0; i < values.length; i++)
			{
				maxTrendsInPeriod[i] = Integer.parseInt(values[i].trim());
			}
			
			variable = "maxBarsInTrend";
			values = properties.getProperty("maxBarsInTrend").split(",");
			maxBarsInTrend = new int[values.length];
			for(int i = 0; i < values.length; i++)
			{
				maxBarsInTrend[i] = Integer.parseInt(values[i].trim());
			}
			
			variable = "totalPeriods";
			totalPeriods = Integer.parseInt(properties.getProperty("totalPeriods").trim());
			variable = "initialVolume";
			initialVolume = Integer.parseInt(properties.getProperty("initialVolume").trim());
	        variable = "startPrice";
	        startPrice = Double.parseDouble(properties.getProperty("startPrice").trim());
	        variable = "interval";
	        interval = Long.parseLong(properties.getProperty("interval").trim()) * 60000;
	        variable="probabilityToEnterTrend";
	        probabilityToEnterTrend = Double.parseDouble(properties.getProperty("probabilityToEnterTrend").trim());
	        variable="startDate";
	        startDate = properties.getProperty("startDate").trim();
    	}
    	catch(NumberFormatException e)
    	{
    		log.error("The format for the variable '" + variable + "' is incorrect (" +
    					 properties.getProperty("sessionExpireTime") + ")", e);
    	}		
	}

	public int[] getDuration() {
		return duration;
	}

	public double[] getVolatility() {
		return volatility;
	}

	public double[] getVolumeTrend() {
		return volumeTrend;
	}

	public double[] getMaxIntrabarVol() {
		return maxIntrabarVol;
	}

	public boolean[] getEnableTrends() {
		return enableTrends;
	}

	public int[] getMaxTrendsInPeriod() {
		return maxTrendsInPeriod;
	}

	public double getInitialVolume() {
		return initialVolume;
	}

	public double getStartPrice() {
		return startPrice;
	}

	public long getInterval() {
		return interval;
	}

	public int getTotalPeriods() {
		return totalPeriods;
	}

	public int[] getMaxBarsInTrend() {
		return maxBarsInTrend;
	}

	public double[] getMaxVolHighLow() {
		return maxVolHighLow;
	}

	public double getProbabilityToEnterTrend() {
		return probabilityToEnterTrend;
	}

	public String getStartDate() {
		return startDate;
	}
}