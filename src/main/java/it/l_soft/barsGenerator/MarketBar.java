package it.l_soft.barsGenerator;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MarketBar {
	final int LOW = 0;
	final int HIGH = 1;

	private ApplicationProperties props = ApplicationProperties.getInstance();
	private long timestamp;
	private double open;
	private double close;
	private double high;
	private double low;
	private double volume;
	private double interest;
	private double intrabarVol;
	private double intradayVol;
	private int trendFollowing;
	private long marketOpenTimestamp;
	private long marketOpenHours;
	private boolean startOfDayBar;

    public MarketBar(long lastTimestamp, long msInterval, double intrabarVol, int trendFollowing) 
    {
    	setTimestampOnCalendar(lastTimestamp, msInterval);
        this.interest = intrabarVol;
        this.intrabarVol = intrabarVol;
        this.trendFollowing= trendFollowing;
        if (intradayVol == 0)
        {
        	intradayVol = props.getIntradayVolDistValue(0);
        }
    }
    
    public void setHighAndLow(int i, Trend trendCur)
    {
		// Magnitude of the current bar
    	double barBodySize;
    	if (props.getUseCurrentBarSizeAsReferenceForShadows())
    	{
    		barBodySize = Math.abs(close - open);
    	}
    	else
    	{
    		barBodySize = trendCur.maxBarSize;
    	}
    	
		double[] shadowSize = new double[2];
		
		// get the percentage of the barBodySize to use randomly from the distribution
		// in configuration file.
		// both LOW and HIGH are taken randomly and eventually used below (see comments down)
		double shadowSizeBand = props.getRand().nextDouble();
		for(int j = 0; j < 2; j++)
		{
			shadowSizeBand = props.getRand().nextDouble();
			for(int k = 0; k < props.getShadowSizeNumOfBarsPercentage().length; k++)
			{
				if (shadowSizeBand <= props.getShadowSizeNumOfBarsPercentage()[k])
				{
					shadowSize[j] = props.getShadowSizeAverageBarSizePercentage()[k] * 
									trendCur.shadowSizeAmplifier;
					break;
				}
			}
		}

		// Calculate the effective vale of the LOW shadow and round it to the tick
		shadowSize[LOW] = Math.round((barBodySize * shadowSize[LOW]) / props.getMarketTick()) * 
				  props.getMarketTick();
		
		// Calculation of the HIGH
		if (props.getSameHighAndLowDepth())
		{
			// It is required to generate simmetric shadow
			shadowSize[HIGH] = shadowSize[LOW];
		}
		else
		{
			if (props.getUseRandomOnBothHighAndLow())
			{
				// Both shadows should be random. Use the previously random number
				// we got from distribution above as we did for the LOW shadow
				shadowSize[HIGH] = Math.round((barBodySize * shadowSize[HIGH]) / props.getMarketTick()) * 
								   props.getMarketTick();
			}
			else
			{
				// Take the size of the LOW shadow as the total size of the shadows, randomly generate
				// a 'split' point within it and assign one part of it to the LOW and one to the HIGH
				// following the direction the current bar goes, unless (randomly) we enter the case 
				// to revert this direction. The probability is ruled by the parameter 
				// shadowSizeToFollowTrendDirectionAt expressing the probability the shadow are oriented
				// oppositely from the trade direction
				
				double partOfShadowAllocated = props.getRand().nextDouble();
				double shadowsGreater = (partOfShadowAllocated > 0.5 ? partOfShadowAllocated : 1 - partOfShadowAllocated);
				int highIdx, lowIdx;
				if ((close > open) && 
					(props.getRand().nextDouble() < props.getShadowSizeToFollowTrendDirectionAt()))
				{
					highIdx = 1;
					lowIdx = 0;
				}
				else
				{
					highIdx = 0;
					lowIdx = 1;
				}
				shadowSize[highIdx] = Math.round((shadowSize[LOW] * shadowsGreater) / props.getMarketTick()) * 
						  props.getMarketTick();
				shadowSize[lowIdx] = Math.round(shadowSize[LOW] * (1 - shadowsGreater) / props.getMarketTick()) * 
						   props.getMarketTick();
			}
		}
		
		double reference = Math.max(open, close);
		high = reference + shadowSize[HIGH];
		reference = Math.min(open, close);
		low = reference - shadowSize[LOW];
    }
    
    public MarketBar(long lastTimestamp, long msInterval) 
    {
    	setTimestampOnCalendar(lastTimestamp, msInterval);
    	return;
    }
    
   
    public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	
	private double calculateIntradayVol()
	{
		double intradayVol = 1;
		double intradayVolBand = props.getRand().nextDouble();
		for(int k = 0; k < props.getIntradayVolDistrPerc().length; k++)
		{
			if (intradayVolBand <= props.getIntradayVolDistrPerc()[k])
			{
				intradayVol = props.getIntradayVolDistValue(k);
				break;
			}
		}
		return intradayVol;
	}

	public void setTimestampOnCalendar(long lastTimestamp, long msInterval)
	{
		startOfDayBar = false;
		
		Calendar today = Calendar.getInstance();
		today.setTime(new Date(lastTimestamp));

		Calendar nextOpen = (Calendar) today.clone();
		nextOpen.set(Calendar.HOUR_OF_DAY, props.getMktOpenTime()[0]);
		nextOpen.set(Calendar.MINUTE, props.getMktOpenTime()[1]);
		nextOpen.set(Calendar.SECOND, props.getMktOpenTime()[2]);
		
		Calendar todayClose = (Calendar) nextOpen.clone();
		nextOpen.add(Calendar.DAY_OF_YEAR, 1);

		todayClose.add(Calendar.HOUR, (int) props.getMarketOpenedHours());
		todayClose.set(Calendar.MINUTE, (int) (60 * (props.getMarketOpenedHours() - 
											   (int)props.getMarketOpenedHours())));
		todayClose.add(Calendar.MINUTE, - props.getBarsIntervalInMinutes()); 
		
		today.add(Calendar.MILLISECOND, (int) msInterval);
		if (today.compareTo(todayClose) > 0)
		{
			// after day close, mark it as the next day
			startOfDayBar = true;
			// move the timestamp to the open time of the next day
			today = nextOpen;
		}
		
        switch(today.get(Calendar.DAY_OF_WEEK) )
        {
        case Calendar.SATURDAY:
        	today.add(Calendar.DAY_OF_YEAR, 2);
         	break;
        case Calendar.SUNDAY:
        	today.add(Calendar.DAY_OF_YEAR, 1);
        	break;
        }
        
		this.timestamp = today.getTimeInMillis();
		if (startOfDayBar)
		{
			intradayVol = calculateIntradayVol();
		}
	}

	public double getOpen() {
		return open;
	}

	public void setOpen(double open) {
		this.open = open;
	}

	public double getClose() {
		return close;
	}

	public void setClose(double close) {
		this.close = close;
	}

	public double getHigh() {
		return high;
	}

	public void setHigh(double high) {
		this.high = high;
	}

	public double getLow() {
		return low;
	}

	public void setLow(double low) {
		this.low = low;
	}

	public double getVolume() {
		return volume;
	}

	public void setVolume(double volume) {
		this.volume = volume;
	}

	public double getInterest() {
		return interest;
	}

	public void setInterest(double interest) {
		this.interest = interest;
	}

	public int getTrendFollowing() {
		return trendFollowing;
	}

	public void setTrendFollowing(int trendFollowing) {
		this.trendFollowing = trendFollowing;
	}

	public double getIntrabarVol() {
		return intrabarVol;
	}

	public void setIntrabarVol(double intrabarVol) {
		this.intrabarVol = intrabarVol;
	}

	public long getMarketOpenTimestamp() {
		return marketOpenTimestamp;
	}

	public void setMarketOpenTimestamp(long marketOpenTimestamp) {
		this.marketOpenTimestamp = marketOpenTimestamp;
	}

	public long getMarketOpenHours() {
		return marketOpenHours;
	}

	public void setMarketOpenHours(long marketOpenHours) {
		this.marketOpenHours = marketOpenHours;
	}

	public double getIntradayVol() {
		return intradayVol;
	}

	public void setIntradayVol(double intradayVol) {
		this.intradayVol = intradayVol;
	}

	public boolean isStartOfDayBar() {
		return startOfDayBar;
	}

	@Override
    public String toString() {
    	SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        return String.format(Locale.US, "%s %10.2f %10.2f %10.2f %10.2f %10.0f %12.6f %6.6s", 
        					 df.format(new Date(timestamp)), open, high, low, close, 
        					 volume, interest, trendFollowing);
    }
	
    public String csvOutput() {
    	SimpleDateFormat date = new SimpleDateFormat("dd/MM/YYYY");
    	SimpleDateFormat time = new SimpleDateFormat("HH:mm:ss");
    	Date ts = new Date(timestamp);
        return String.format(Locale.US, "%s %s,%.2f,%.2f,%.2f,%.2f,%.0f,%.5f,%d", 
        					 date.format(ts), time.format(ts), open, high, low, close, volume, intrabarVol, trendFollowing)
        					.replace(",", (props.getFieldSeparator() == null ? "," : props.getFieldSeparator()))
        					.replace(".", (props.getDecimalSeparator() == null ? "." : props.getDecimalSeparator()));
    }

    public String tradiaOutput() {
    	SimpleDateFormat date = new SimpleDateFormat("dd/MM/YYYY");
    	SimpleDateFormat time = new SimpleDateFormat("HH:mm:ss");
    	Date ts = new Date(timestamp);
        return String.format(Locale.US, "%s,%s,%.2f,%.2f,%.2f,%.2f,%.0f", 
        					 date.format(ts), time.format(ts), open, high, low, close, (volume == 0 ? 1000 : volume))
							.replace(",", (props.getFieldSeparator() == null ? "," : props.getFieldSeparator()))
							.replace(".", (props.getDecimalSeparator() == null ? "." : props.getDecimalSeparator()));
    }
    
    
}
