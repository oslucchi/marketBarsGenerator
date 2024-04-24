package barsGenerator;

import java.text.ParseException;

public class MarketTrend {
    private int duration; // how many bars in the period
    private double startPrice;
    private double volatility; // where will the price go
	private double maxIntrabarVol; // max change in a single bar
	private double maxVolHighLow; // max distance for High and Low vs lastClose
	private boolean enableTrends; // is the period subject to trends
	private int maxTrendsInPeriod; // how many trends should we see at max in the period
	private int maxBarsInTrend;  // how many bars max when a trend starts
	private long timestampStart; // start time for the period
	
	public MarketTrend(int run) throws ParseException
	{
		ApplicationProperties props = ApplicationProperties.getInstance();
	    this.volatility = props.getVolatility()[run] / 100;
	    this.duration = props.getDuration()[run];
		this.maxIntrabarVol= props.getMaxIntrabarVol()[run] / 100;
		this.maxVolHighLow = props.getMaxVolHighLow()[run] / 100;
	    this.enableTrends = props.getEnableTrends()[run];
	    this.maxTrendsInPeriod = props.getMaxTrendsInPeriod()[run];
	    this.maxBarsInTrend = props.getMaxBarsInTrend()[run];
	}

	public int getDuration() {
		return duration;
	}

	public double getStartPrice() {
		return startPrice;
	}
	
	public void setStartPrice(double startPrice) {
		this.startPrice = startPrice;
	}

	public double getVolatility() {
		return volatility;
	}

	public double getMaxIntrabarVol() {
		return maxIntrabarVol;
	}

	public double getMaxVolHighLow() {
		return maxVolHighLow;
	}

	public boolean isEnableTrends() {
		return enableTrends;
	}

	public int getMaxTrendsInPeriod() {
		return maxTrendsInPeriod;
	}

	public int getMaxBarsInTrend() {
		return maxBarsInTrend;
	}

	public long getTimestampStart() {
		return timestampStart;
	}
	
	public void setTimestampStart(long timestampStart) {
		this.timestampStart = timestampStart;
	}
}
