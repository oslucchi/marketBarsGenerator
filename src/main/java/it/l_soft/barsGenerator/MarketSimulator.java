package it.l_soft.barsGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MarketSimulator {

    private final ApplicationProperties props;
    private final Random rand;
    private final double atrShortBase;
    private final double atrLongBase;
    private final double smoothness;
    private final int tBarsPerB;

    private double atrShortCurrent;
    private double atrLongCurrent;
    private double currentPrice;
    private long currentTimestamp;
    private int bBarIndex;

    public MarketSimulator(double atrShort, double atrLong, double smoothness, int tBarsPerB) {
        this.props = ApplicationProperties.getInstance();
        this.rand = props.getRand();
        this.atrShortBase = isValidAtr(atrShort) ? atrShort : props.getStartPrice() * 0.001;
        this.atrLongBase = isValidAtr(atrLong) ? atrLong : props.getStartPrice() * 0.003;
        this.atrShortCurrent = this.atrShortBase;
        this.atrLongCurrent = this.atrLongBase;
        this.smoothness = smoothness;
        this.tBarsPerB = tBarsPerB;
    }

    private boolean isValidAtr(double v) {
        return !Double.isNaN(v) && !Double.isInfinite(v) && v > 0;
    }

    private void updateAtr() {
        double volatility = Math.max(0.05, 1.0 - smoothness);
        double maxMoveShort = atrShortBase * volatility * 0.2;
        double maxMoveLong = atrLongBase * volatility * 0.2;
        atrShortCurrent += (rand.nextDouble() - 0.5) * 2.0 * maxMoveShort;
        atrLongCurrent += (rand.nextDouble() - 0.5) * 2.0 * maxMoveLong;
        atrShortCurrent = Math.max(atrShortBase * 0.3, Math.min(atrShortBase * 2.5, atrShortCurrent));
        atrLongCurrent = Math.max(atrLongBase * 0.3, Math.min(atrLongBase * 2.5, atrLongCurrent));
    }

    public List<Bar> generateBars(List<Trend> trends, double startPrice, long startTimestamp, int barsIntervalMs) {
        List<Bar> allBars = new ArrayList<>();
        currentPrice = startPrice;
        currentTimestamp = startTimestamp;
        bBarIndex = 0;

        for (Trend trend : trends) {
            trend.openPrice = currentPrice;
            double trendCloseTarget = currentPrice + trend.variationPoints;

            for (int b = 0; b < trend.duration; b++) {
                ++bBarIndex;
                updateAtr();

                double bProgress = (double) (b + 1) / trend.duration;
                double bCloseTarget = trend.openPrice + trend.variationPoints * bProgress;

                double bCloseNoise = (rand.nextDouble() - 0.5) * 2.0 * atrLongCurrent * 0.3;
                double bClose = bCloseTarget + bCloseNoise;

                // For the last B bar of a trend, converge exactly to the trend target
                if (b == trend.duration - 1) {
                    bClose = trendCloseTarget;
                }

                double bHigh = Math.max(currentPrice, bClose) + atrLongCurrent * 0.2 * rand.nextDouble();
                double bLow = Math.min(currentPrice, bClose) - atrLongCurrent * 0.2 * rand.nextDouble();

                long lastTs = currentTimestamp;

                // Generate T bars for this B bar period
                for (int t = 0; t < tBarsPerB; t++) {
                    double tProgress = (double) (t + 1) / tBarsPerB;

                    double tOpen = (t == 0) ? currentPrice : allBars.get(allBars.size() - 1).getClose();
                    double tCloseTarget = currentPrice + (bClose - currentPrice) * tProgress;
                    double tCloseNoise = (rand.nextDouble() - 0.5) * 2.0 * atrShortCurrent * 0.3;
                    double tClose = tCloseTarget + tCloseNoise;

                    // Keep T close within B bar range
                    double bBarLow = Math.min(currentPrice, bClose);
                    double bBarHigh = Math.max(currentPrice, bClose);
                    tClose = Math.max(bBarLow, Math.min(bBarHigh, tClose));

                    // H/L based on ATR smoothness
                    double bodySize = Math.abs(tClose - tOpen);
                    double shadowFraction = Math.max(0.05, 1.0 - smoothness) * 0.5;
                    double shadow = bodySize * shadowFraction * (0.5 + rand.nextDouble() * 0.5);
                    shadow = Math.min(shadow, atrShortCurrent * 0.5);

                    // Distribute shadow based on direction
                    double tHigh, tLow;
                    if (tClose >= tOpen) {
                        tHigh = tClose + shadow;
                        tLow = tOpen - shadow * 0.4;
                    } else {
                        tHigh = tOpen + shadow * 0.4;
                        tLow = tClose - shadow;
                    }

                    // Keep within B bar bounds
                    tHigh = Math.max(tHigh, Math.max(tOpen, tClose));
                    tLow = Math.min(tLow, Math.min(tOpen, tClose));
                    tHigh = Math.min(tHigh, bHigh);
                    tLow = Math.max(tLow, bLow);

                    // Volume: proportional to ATR/price ratio plus randomness
                    long volume = calculateVolume(bodySize);

                    Bar tBar = new Bar(lastTs, barsIntervalMs, 0, 0);
                    tBar.setOpen(tOpen);
                    tBar.setClose(tClose);
                    tBar.setHigh(tHigh);
                    tBar.setLow(tLow);
                    tBar.setVolume(volume);

                    allBars.add(tBar);
                    lastTs = tBar.getTimestamp();
                }

                currentPrice = bClose;
                currentTimestamp = lastTs;
            }

            trend.closePrice = currentPrice;
        }

        return allBars;
    }

    private long calculateVolume(double bodySize) {
        double baseVolume = props.getInitialVolume();
        double atrRatio = atrShortCurrent / Math.max(props.getStartPrice(), 1.0);
        double volumeMultiplier = 1.0 + atrRatio * 100.0;
        double noise = 0.5 + rand.nextDouble();
        return Math.max(1, (long) (baseVolume * volumeMultiplier * noise));
    }
}
