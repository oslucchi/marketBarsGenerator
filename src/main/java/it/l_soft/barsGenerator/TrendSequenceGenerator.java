package it.l_soft.barsGenerator;

import java.util.ArrayList;
import java.util.List;

public class TrendSequenceGenerator {

    private final ApplicationProperties props;

    public TrendSequenceGenerator(ApplicationProperties props) {
        this.props = props;
    }

    public List<Trend> generateSequence() {
        Trend[] trendArray = props.getTrends();
        if (trendArray == null || trendArray.length == 0) {
            throw new IllegalStateException("No trends configured");
        }
        List<Trend> list = new ArrayList<>(trendArray.length);
        for (Trend t : trendArray) {
            if (t.duration <= 0) {
                throw new IllegalStateException("Trend " + t.id + " has invalid duration: " + t.duration);
            }
            list.add(t);
        }
        return list;
    }
}
