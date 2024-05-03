package barsGenerator;

import java.util.ArrayList;

public class Trend implements Cloneable {
	int direction; // -1 short, 0 lateral, 1 long
	int duration;
	int currentBar;
	int deltaPoints;
	boolean enableMiniTrends;
	int maxBarPerTrend;
	int minBarPerTrend;
	int totalBarsInTred;
	double openPrice;
	double closePrice;
	double targetPrice;
	long timestamp;
	ArrayList<InnerTrend> innerTrends = new ArrayList<>();
	
	@Override
	public Trend clone() throws CloneNotSupportedException {
		 Trend trendCloned = (Trend) super.clone();
		 trendCloned.innerTrends = new ArrayList<InnerTrend>();
		 for(InnerTrend t : innerTrends)
		 {
			 trendCloned.innerTrends.add(t.clone());
		 }
		 return trendCloned;
	}
}