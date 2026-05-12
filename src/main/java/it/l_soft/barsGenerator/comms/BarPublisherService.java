package it.l_soft.barsGenerator.comms;

import java.util.List;

import org.apache.log4j.Logger;

import it.l_soft.barsGenerator.ApplicationProperties;
import it.l_soft.barsGenerator.Bar;

public class BarPublisherService {
	final Logger log = Logger.getLogger(this.getClass());
	private Publisher publisher;
	private ApplicationProperties props;

	private double cumOpen;
	private double cumHigh;
	private double cumLow;
	private double cumClose;
	private long cumVolume;
	private long cumTimestamp;
	private int barsInCumulative;
	private boolean cumulativeStarted;
	private int barsPerCumulative;

	public BarPublisherService(Publisher publisher, int tBarsPerB) {
		this.publisher = publisher;
		this.props = ApplicationProperties.getInstance();
		this.barsPerCumulative = tBarsPerB;
		if (this.barsPerCumulative < 1) this.barsPerCumulative = 1;
		resetCumulative();
	}

	private void resetCumulative() {
		cumOpen = 0;
		cumHigh = 0;
		cumLow = Double.MAX_VALUE;
		cumClose = 0;
		cumVolume = 0;
		cumTimestamp = 0;
		barsInCumulative = 0;
		cumulativeStarted = false;
	}

	private void accumulate(Bar bar) {
		if (!cumulativeStarted) {
			cumOpen = bar.getOpen();
			cumulativeStarted = true;
		}
		if (bar.getHigh() > cumHigh) cumHigh = bar.getHigh();
		if (bar.getLow() < cumLow) cumLow = bar.getLow();
		cumClose = bar.getClose();
		cumVolume += bar.getVolume();
		cumTimestamp = bar.getTimestamp();
		barsInCumulative++;
	}

	private void sendBCumulative() 
		throws Exception
	{
		if (barsInCumulative == 0) return;
		MarketBar bMsg = new MarketBar(cumTimestamp, cumOpen, cumHigh, cumLow, cumClose, cumVolume);
		bMsg.setTopic("B");
		publisher.sendMessageObject(bMsg);
		log.debug("Published B bar: O=" + cumOpen + " H=" + cumHigh + " L=" + cumLow + " C=" + cumClose + " V=" + cumVolume + " bars=" + barsInCumulative);
		resetCumulative();
	}

	public void publishBar(Bar bar) {
		MarketBar tMsg = new MarketBar(bar.getTimestamp(), bar.getOpen(), bar.getHigh(), bar.getLow(), bar.getClose(), bar.getVolume());
		tMsg.setTopic("T");
		try {
			publisher.sendMessageObject(tMsg);
			accumulate(bar);
			if (barsInCumulative >= barsPerCumulative) {
				sendBCumulative();
			}
		}
		catch(Exception e)
		{
			log.warn("No more clients connected");
		}
	}

	public void publishList(List<Bar> bars) {
		for (Bar bar : bars) {
			publishBar(bar);
			try {
				Thread.sleep(props.getIntraMessagePause());
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				break;
			}
		}
		try {
			if (barsInCumulative > 0) {
				sendBCumulative();
			}
		}
		catch(Exception e)
		{
			log.warn("No more clients connected");
		}
	}
}
