package it.l_soft.barsGenerator;

public class InnerTrend implements Cloneable {
	double openPrice;
	double closePrice;
	boolean rebound = false;
	int direction;
	int numOfBars;
	int barStart;
	int barEnd;
	
	public InnerTrend(int direction, int numOfBars)
	{
		this.direction = direction;
		this.numOfBars = numOfBars;
	}

	@Override
	public InnerTrend clone() throws CloneNotSupportedException {
		 return (InnerTrend) super.clone();
	}
}