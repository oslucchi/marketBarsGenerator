package it.l_soft.barsGenerator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;

public class MarketAtrFetcher {

    private static final int ATR_PERIOD = 14;

    private final String symbol;
    private final int[] durationsSeconds;
    private final String apiKey;
    private final HttpClient httpClient;
    private final ObjectMapper mapper;

    public static class AtrResult {
        public final double[] atrValues;
        public final double[] smoothnessValues;

        public AtrResult(double[] atrValues, double[] smoothnessValues) {
            this.atrValues = atrValues;
            this.smoothnessValues = smoothnessValues;
        }

        public double atr(int index) { return atrValues[index]; }
        public double smoothness(int index) { return smoothnessValues[index]; }
        public int size() { return atrValues.length; }
    }

    public MarketAtrFetcher(String symbol, int[] durationsSeconds) {
        this.symbol = symbol;
        this.durationsSeconds = durationsSeconds;
        String key = System.getenv("POLYGON_API_KEY");
        if (key == null || key.isBlank()) {
            key = System.getProperty("POLYGON_API_KEY");
        }
        if (key == null || key.isBlank()) {
            throw new IllegalStateException("Missing POLYGON_API_KEY environment variable");
        }
        this.apiKey = key;

        this.httpClient = HttpClient.newHttpClient();
        this.mapper = new ObjectMapper();
    }

    public AtrResult fetchAtrValues() throws IOException, InterruptedException {
        double[] atrResult = new double[durationsSeconds.length];
        double[] smoothnessResult = new double[durationsSeconds.length];

        for (int i = 0; i < durationsSeconds.length; i++) {
            int duration = durationsSeconds[i];

            List<Bar> bars = fetchBars(duration);

            if (bars.size() < ATR_PERIOD + 1) {
                atrResult[i] = Double.NaN;
                smoothnessResult[i] = 1.0;
            } else {
                AtrResultSingle r = calculateLatestAtr(bars, ATR_PERIOD);
                atrResult[i] = r.atr;
                smoothnessResult[i] = r.smoothness;
            }
        }

        return new AtrResult(atrResult, smoothnessResult);
    }

    private List<Bar> fetchBars(int durationSeconds) throws IOException, InterruptedException {
        TimeFrame tf = toTimeFrame(durationSeconds);

        LocalDate to = LocalDate.now();
        LocalDate from = to.minusDays(10);

        String encodedSymbol = URLEncoder.encode(symbol, StandardCharsets.UTF_8);

        String url = "https://api.polygon.io/v2/aggs/ticker/"
                + encodedSymbol
                + "/range/"
                + tf.multiplier
                + "/"
                + tf.timespan
                + "/"
                + from
                + "/"
                + to
                + "?adjusted=true"
                + "&sort=asc"
                + "&limit=50000"
                + "&apiKey="
                + apiKey;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("HTTP error " + response.statusCode() + ": " + response.body());
        }

        JsonNode root = mapper.readTree(response.body());
        JsonNode results = root.get("results");

        if (results == null || !results.isArray()) {
            return Collections.emptyList();
        }

        List<Bar> bars = new ArrayList<>();

        for (JsonNode node : results) {
            bars.add(new Bar(
                    node.get("o").asDouble(),
                    node.get("h").asDouble(),
                    node.get("l").asDouble(),
                    node.get("c").asDouble(),
                    node.has("v") ? node.get("v").asDouble() : 0.0
            ));
        }

        return bars;
    }

    private static class AtrResultSingle {
        final double atr;
        final double smoothness;

        AtrResultSingle(double atr, double smoothness) {
            this.atr = atr;
            this.smoothness = smoothness;
        }
    }

    private AtrResultSingle calculateLatestAtr(List<Bar> bars, int period) {
        List<Double> trueRanges = new ArrayList<>();

        for (int i = 1; i < bars.size(); i++) {
            Bar current = bars.get(i);
            Bar previous = bars.get(i - 1);

            double highLow = current.high - current.low;
            double highPrevClose = Math.abs(current.high - previous.close);
            double lowPrevClose = Math.abs(current.low - previous.close);

            double tr = Math.max(highLow, Math.max(highPrevClose, lowPrevClose));
            trueRanges.add(tr);
        }

        double atr = 0.0;
        for (int i = 0; i < period; i++) {
            atr += trueRanges.get(i);
        }
        atr /= period;

        for (int i = period; i < trueRanges.size(); i++) {
            atr = ((atr * (period - 1)) + trueRanges.get(i)) / period;
        }

        // Calculate smoothness as 1 / (1 + CV of true ranges)
        double mean = 0;
        int n = Math.min(period * 3, trueRanges.size());
        for (int i = trueRanges.size() - n; i < trueRanges.size(); i++) {
            mean += trueRanges.get(i);
        }
        mean /= n;

        double variance = 0;
        for (int i = trueRanges.size() - n; i < trueRanges.size(); i++) {
            double diff = trueRanges.get(i) - mean;
            variance += diff * diff;
        }
        variance /= n;
        double stdDev = Math.sqrt(variance);

        double cv = (mean > 0) ? stdDev / mean : 1.0;
        double smoothness = 1.0 / (1.0 + cv);

        return new AtrResultSingle(atr, smoothness);
    }

    private TimeFrame toTimeFrame(int seconds) {
        if (seconds < 60) {
            return new TimeFrame(seconds, "second");
        }

        if (seconds % 86400 == 0) {
            return new TimeFrame(seconds / 86400, "day");
        }

        if (seconds % 3600 == 0) {
            return new TimeFrame(seconds / 3600, "hour");
        }

        if (seconds % 60 == 0) {
            return new TimeFrame(seconds / 60, "minute");
        }

        throw new IllegalArgumentException(
                "Unsupported duration: " + seconds + " seconds"
        );
    }

    private static class TimeFrame {
        final int multiplier;
        final String timespan;

        TimeFrame(int multiplier, String timespan) {
            this.multiplier = multiplier;
            this.timespan = timespan;
        }
    }

    private static class Bar {
        final double open;
        final double high;
        final double low;
        final double close;
        final double volume;

        Bar(double open, double high, double low, double close, double volume) {
            this.open = open;
            this.high = high;
            this.low = low;
            this.close = close;
            this.volume = volume;
        }
    }

    public static void main(String[] args) throws Exception {
        MarketAtrFetcher fetcher = new MarketAtrFetcher(
                "AAPL",
                new int[]{60, 300}
        );

        AtrResult result = fetcher.fetchAtrValues();
        for (int i = 0; i < result.size(); i++) {
            System.out.println("Duration index " + i +
                    ": ATR=" + result.atr(i) +
                    " smoothness=" + result.smoothness(i));
        }
    }
}
