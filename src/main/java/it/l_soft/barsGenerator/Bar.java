package it.l_soft.barsGenerator;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Bar {
	final int LOW = 0;
	final int HIGH = 1;

	private ApplicationProperties props = ApplicationProperties.getInstance();
	private long timestamp;
	private double open;
	private double close;
	private double high;
	private double low;
	private long volume;
	private double interest;
	private double intrabarVol;
	private double intradayVol;
	private int trendFollowing;
	private long marketOpenTimestamp;
	private long marketOpenHours;
	private boolean startOfDayBar;

    public Bar(long lastTimestamp, long msInterval, double intrabarVol, int trendFollowing) 
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
    
    public void setHighAndLow(double shadowSizeHigh, double shadowSizeLow)
    {
		high = Math.max(open, close) + shadowSizeHigh;
		low = Math.min(open, close) - shadowSizeLow;
    }
    
    public Bar(long lastTimestamp, long msInterval) 
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

		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date(lastTimestamp));
		cal.add(Calendar.MILLISECOND, (int) msInterval);

		// Define today's exchange open and close
		Calendar exchangeOpen = Calendar.getInstance();
		exchangeOpen.setTime(cal.getTime());
		exchangeOpen.set(Calendar.HOUR_OF_DAY, props.getExchangeFromHour());
		exchangeOpen.set(Calendar.MINUTE, props.getExchangeFromMinute());
		exchangeOpen.set(Calendar.SECOND, 0);
		exchangeOpen.set(Calendar.MILLISECOND, 0);

		Calendar exchangeClose = Calendar.getInstance();
		exchangeClose.setTime(cal.getTime());
		exchangeClose.set(Calendar.HOUR_OF_DAY, props.getExchangeToHour());
		exchangeClose.set(Calendar.MINUTE, props.getExchangeToMinute());
		exchangeClose.set(Calendar.SECOND, 0);
		exchangeClose.set(Calendar.MILLISECOND, 0);

		// If outside exchange hours, move to next exchange open
		if (cal.before(exchangeOpen))
		{
			startOfDayBar = true;
			cal.setTime(exchangeOpen.getTime());
		}
		else if (cal.after(exchangeClose))
		{
			startOfDayBar = true;
			cal.add(Calendar.DAY_OF_YEAR, 1);
			cal.set(Calendar.HOUR_OF_DAY, props.getExchangeFromHour());
			cal.set(Calendar.MINUTE, props.getExchangeFromMinute());
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
		}

		// Advance past closed days (weekends via openDays) and holidays
		boolean[] openDays = props.getOpenDays();
		java.util.List<String> holidays = props.getHolidays();
		int maxLoops = 30;
		while (maxLoops-- > 0)
		{
			int dow = cal.get(Calendar.DAY_OF_WEEK) - 1; // Calendar.MONDAY=2 -> index 1
			if (dow >= 0 && dow < openDays.length && !openDays[dow])
			{
				cal.add(Calendar.DAY_OF_YEAR, 1);
				cal.set(Calendar.HOUR_OF_DAY, props.getExchangeFromHour());
				cal.set(Calendar.MINUTE, props.getExchangeFromMinute());
				cal.set(Calendar.SECOND, 0);
				cal.set(Calendar.MILLISECOND, 0);
				startOfDayBar = true;
				continue;
			}

			String dateStr = String.format("%02d/%02d",
				cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH) + 1);
			if (holidays.contains(dateStr))
			{
				cal.add(Calendar.DAY_OF_YEAR, 1);
				cal.set(Calendar.HOUR_OF_DAY, props.getExchangeFromHour());
				cal.set(Calendar.MINUTE, props.getExchangeFromMinute());
				cal.set(Calendar.SECOND, 0);
				cal.set(Calendar.MILLISECOND, 0);
				startOfDayBar = true;
				continue;
			}

			break;
		}

		this.timestamp = cal.getTimeInMillis();
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

	public long getVolume() {
		return volume;
	}

	public void setVolume(long volume) {
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
    	String stringVal = String.format(Locale.US, 
    									 "%s %10.2f %10.2f %10.2f %10.2f %10d %12.6f %6d",
    									 df.format(new Date(timestamp)), open, high, low, close,
    									 volume, interest, trendFollowing);
        return stringVal;
	}
	
    public String csvOutput() {
    	SimpleDateFormat date = new SimpleDateFormat("dd/MM/YYYY");
    	SimpleDateFormat time = new SimpleDateFormat("HH:mm:ss");
    	Date ts = new Date(timestamp);
        return String.format(Locale.US, "%s %s,%.2f,%.2f,%.2f,%.2f,%d,%.5f,%d", 
        					 date.format(ts), time.format(ts), open, high, low, close, volume, intrabarVol, trendFollowing)
        					.replace(",", (props.getFieldSeparator() == null ? "," : props.getFieldSeparator()))
        					.replace(".", (props.getDecimalSeparator() == null ? "." : props.getDecimalSeparator()));
    }

    public String tradiaOutput() {
    	SimpleDateFormat date = new SimpleDateFormat("dd/MM/YYYY");
    	SimpleDateFormat time = new SimpleDateFormat("HH:mm:ss");
    	Date ts = new Date(timestamp);
        return String.format(Locale.US, "%s,%s,%.2f,%.2f,%.2f,%.2f,%d", 
        					 date.format(ts), time.format(ts), open, high, low, close, (volume == 0 ? 1000 : volume))
        					.replace(",", (props.getFieldSeparator() == null ? "," : props.getFieldSeparator()))
        					.replace(".", (props.getDecimalSeparator() == null ? "." : props.getDecimalSeparator()));
    }

    public String bOutput(int barNumber) {
    	SimpleDateFormat date = new SimpleDateFormat("dd/MM/YYYY");
    	SimpleDateFormat time = new SimpleDateFormat("HH:mm:ss");
    	Date ts = new Date(timestamp);
        return String.format(Locale.US, "B,%s,%s,%.2f,%.2f,%.2f,%.2f,%d,%d", 
        					 date.format(ts), time.format(ts), open, high, low, close, (volume == 0 ? 1000 : volume), barNumber);
    }

    public String tOutput(long subTimestamp, double subOpen, double subHigh, double subLow, double subClose, long subVolume, int barNumber) {
    	SimpleDateFormat date = new SimpleDateFormat("dd/MM/YYYY");
    	SimpleDateFormat time = new SimpleDateFormat("HH:mm:ss");
    	Date ts = new Date(subTimestamp);
        return String.format(Locale.US, "T,%s,%s,%.2f,%.2f,%.2f,%.2f,%d,%d",
                             date.format(ts), time.format(ts), subOpen, subHigh, subLow, subClose, subVolume, barNumber);
    }

}
