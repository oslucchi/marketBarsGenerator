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
    
    public void setHighAndLow(int i)
    {
		// Magnitude of the current bar
    	double barBodySize = Math.abs(close - open);

		double[] shadowSize = new double[2];
		
		double shadowSizeBand = props.getRand().nextDouble();
		for(int j = 0; j < 2; j++)
		{
			shadowSizeBand = props.getRand().nextDouble();
			for(int k = 0; k < props.getShadowSizeNumOfBarsPercentage().length; k++)
			{
				if (shadowSizeBand <= props.getShadowSizeNumOfBarsPercentage()[k])
				{
					shadowSize[j] = props.getShadowSizeAverageBarSizePercentage()[k] / 2;
					break;
				}
			}
		}

		if (barBodySize / close < .00015)
		{
			barBodySize *= 6;
		}
		else if (barBodySize / close < .0003)
		{
			barBodySize *= 3;
		}
		else if (barBodySize / close < .0006)
		{
			barBodySize *= 2;
		}

		shadowSize[LOW] = Math.round((barBodySize * shadowSize[LOW]) / props.getMarketTick()) * 
				  props.getMarketTick();
		
		if (props.getSameHighAndLowDepth())
		{
			shadowSize[HIGH] = shadowSize[LOW];
		}
		else
		{
			if (props.getUseRandomOnBothHighAndLow())
			{
				shadowSize[HIGH] = Math.round((barBodySize * shadowSize[HIGH]) / props.getMarketTick()) * 
								   props.getMarketTick();
			}
			else
			{
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
    
    /*
    public void populateObject(double open, double priceChange, double volume)
    {
    	this.open = open;
    	this.close = open + priceChange;
    	this.volume = volume;
    	setHighAndLow();
    	return;
    }
    */
   
    public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	
	public void setTimestampOnCalendar(long lastTimestamp, long msInterval)
	{
		startOfDayBar = false;
		
        Calendar c1 = Calendar.getInstance();
        Calendar c2 = Calendar.getInstance();
        c1.setTime(new Date(lastTimestamp + msInterval));
        if (c1.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY)
        {
            c1.add(Calendar.DAY_OF_YEAR, -1);
        }
        else if (c1.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)
        {
            c1.add(Calendar.DAY_OF_YEAR, -2);
        }
        
        c2.set(Calendar.YEAR, c1.get(Calendar.YEAR));
        c2.set(Calendar.MONTH, c1.get(Calendar.MONTH));
        c2.set(Calendar.DAY_OF_MONTH, c1.get(Calendar.DAY_OF_MONTH));
        c2.set(Calendar.HOUR_OF_DAY, props.getStartMarketHour() + ((int) props.getMarketOpenedHours()));
        c2.set(Calendar.MINUTE, props.getStartMarketMinute());
        c2.set(Calendar.SECOND, props.getStartMarketSecond() + 1);
        
        if (c1.compareTo(c2) >= 0)
        {
        	c1.add(Calendar.DAY_OF_YEAR, 1);
            c1.set(Calendar.HOUR_OF_DAY, props.getStartMarketHour());
            c1.set(Calendar.MINUTE, props.getStartMarketMinute());
            c1.set(Calendar.SECOND, props.getStartMarketSecond());
            startOfDayBar = true;
        }
        
        switch(c1.get(Calendar.DAY_OF_WEEK) )
        {
        case Calendar.SATURDAY:
        	c1.add(Calendar.DAY_OF_YEAR, 2);
            c1.set(Calendar.HOUR_OF_DAY, props.getStartMarketHour());
            c1.set(Calendar.MINUTE, props.getStartMarketMinute());
            c1.set(Calendar.SECOND, props.getStartMarketSecond());
            startOfDayBar = true;
        	break;
        case Calendar.SUNDAY:
        	c1.add(Calendar.DAY_OF_YEAR, 1);
            c1.set(Calendar.HOUR_OF_DAY, props.getStartMarketHour());
            c1.set(Calendar.MINUTE, props.getStartMarketMinute());
            c1.set(Calendar.SECOND, props.getStartMarketSecond());
            startOfDayBar = true;
        	break;
        }
        
		this.timestamp = c1.getTimeInMillis();
		if (startOfDayBar)
		{
			// calculate today's intraday max volatility
			intradayVol = props.getRand().nextDouble();
			for(int i = 0; i < props.getIntradayVolDistrPerc().length; i++)
			{
				if (intradayVol < props.getIntradayVolDistrPerc()[i])
				{
					intradayVol = props.getIntradayVolDistValue()[i];
					break;
				}
			}
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

	public boolean getStartOfDayBar() {
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
