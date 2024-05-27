package it.l_soft.barsGenerator.comms;

import java.io.Serializable;

import org.apache.log4j.Logger;

public class MarketBarMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    transient Logger log = Logger.getLogger(this.getClass());
    private long timestamp;
    private double open, high, low, close, volume;

    public MarketBarMessage(long timestamp, double open, double high, double low, double close, double volume) {
        this.timestamp = timestamp;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume = volume;
    }

    // Getters
    public long getTimestamp() { return timestamp; }
    public double getOpen() { return open; }
    public double getHigh() { return high; }
    public double getLow() { return low; }
    public double getClose() { return close; }
    public double getVolume() { return volume; }
}
