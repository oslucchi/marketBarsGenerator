package barsGenerator;

public class MarketTrend {
    double volatility;
    int duration;
    double targetEndPrice;
    double initialVolume;
    double volumeTrend; // Factor to increase or decrease volume
	double maxIntrabarVol;
	boolean enableTrends;
	int maxTrendsInPeriod;
	int maxBarsInTrend;
    double startPrice;
    long timestampStart;
    
    public MarketTrend(ApplicationProperties props, int run, MarketBar lastBar) {
        this.volatility = props.getVolatility()[run];
        this.duration = props.getDuration()[run];
        this.targetEndPrice = lastBar.close * (1 + volatility * duration / 10);
        this.initialVolume = lastBar.volume;
        this.volumeTrend = props.getVolumeTrend()[run];
    	this.maxIntrabarVol= props.getMaxIntrabarVol()[run] ;
        this.enableTrends = props.getEnableTrends()[run];
        this.maxTrendsInPeriod = props.getMaxTrendsInPeriod()[run];
        this.maxBarsInTrend = props.getMaxBarsInTrend()[run];
        this.startPrice = lastBar.close;
        this.timestampStart = lastBar.timestamp;
    }
}
