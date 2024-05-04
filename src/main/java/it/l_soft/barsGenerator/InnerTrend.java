package it.l_soft.barsGenerator;

public class InnerTrend implements Cloneable {
	int direction;
	int numOfBars;
	
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