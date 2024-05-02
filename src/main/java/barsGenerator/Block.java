package barsGenerator;

import java.util.ArrayList;
import java.util.List;

public class Block {

	public class InnerTrend {
		int direction;
		int numOfBars;
		
		public InnerTrend(int direction, int numOfBars)
		{
			this.direction = direction;
			this.numOfBars = numOfBars;
		}
	}
	
	public class Trend {
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
		List<InnerTrend> innerTrends = new ArrayList<>();
	}
	
	private int numOfTrends;
	private Trend[] trends;
	private double maxIntrabarVol;
	
	public Block(int numOfTrends, double maxIntrabarVol)
	{
		trends = new Trend[numOfTrends + 1];
		this.maxIntrabarVol = maxIntrabarVol;
		this.numOfTrends = numOfTrends;
	}
	public Block()
	{
		return;
	}
	
	public void pushTrend(Trend trend, int index)
	{
		trends[index] = trend;
	}
	
	public Trend getTrend(int index)
	{
		return trends[index];
	}
	
	public Trend[] getTrends()
	{
		return trends;
	}
	
	public double getMaxIntrabarVol()
	{
		return maxIntrabarVol;
	}

	public int getNumOfTrends() {
		return numOfTrends;
	}
	
}
