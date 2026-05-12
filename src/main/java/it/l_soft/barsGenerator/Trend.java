package it.l_soft.barsGenerator;

public class Trend {
    int id;
    String name;
    int duration;
    int variationPoints;
    boolean enableMiniTrends;
    int maxBarsPerTrend;
    int minBarsPerTrend;

    int currentBar;
    double openPrice;
    double closePrice;
    double targetPrice;
    long timestampStart;
    long timestampEnd;

    public int getDirection() {
        if (variationPoints > 0) return 1;
        if (variationPoints < 0) return -1;
        return 0;
    }
}
