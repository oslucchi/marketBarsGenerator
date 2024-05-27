package it.l_soft.barsGenerator.comms;

import java.io.Serializable;

import org.apache.log4j.Logger;

public class TradeMessage implements Serializable {
	transient Logger log = Logger.getLogger(this.getClass());
    private static final long serialVersionUID = 1L;
    private long timestamp;
    private String side;
    private double value, quantity;

    public TradeMessage(long timestamp, String side, double value, double quantity) {
        this.side = side;
        this.value = value;
        this.quantity = quantity;
    }

    // Getters
    public String getSide() { return side; }
    public double getValue() { return value; }
    public double getQuantity() { return quantity; }
    public long getTimestamp() { return timestamp; }
}