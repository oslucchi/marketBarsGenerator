package it.l_soft.barsGenerator.comms;

import org.apache.log4j.Logger;

public class TradeMessage extends Message {
	transient Logger log = Logger.getLogger(this.getClass());
    private String side;
    private double value, quantity;

    public TradeMessage() {
    	super("A");
    }
    
    public TradeMessage(long timestamp, String side, double value, double quantity) {
    	super("A");
        this.side = side;
        this.value = value;
        this.quantity = quantity;
        this.timestamp = timestamp;
    }

    // Getters
    public String getSide() { return side; }
    public double getValue() { return value; }
    public double getQuantity() { return quantity; }
}