package barsGenerator;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MarketBar {
	long timestamp;
    double open;
    double close;
    double high;
    double low;
    double volume;
    double interest;
    boolean trendFollowing;

    public MarketBar(double open, double close, double high, double low, 
    				 double volume, long lastTimestamp, long msInterval, double interest,
    				 boolean trendFollowing) {
        this.open = open;
        this.close = close;
        this.high = high;
        this.low = low;
        this.volume = volume;
        this.timestamp = lastTimestamp + msInterval;
        this.interest = interest;
        this.trendFollowing= trendFollowing ;
    }

    @Override
    public String toString() {
    	SimpleDateFormat df = new SimpleDateFormat("yy/MM/dd HH:mm");
        return String.format("%s %10.2f %10.2f %10.2f %10.2f %10.0f %12.6f %6.6s", 
        					 df.format(new Date(timestamp)), open, high, low, close, 
        					 volume, interest, trendFollowing);
    }
}
