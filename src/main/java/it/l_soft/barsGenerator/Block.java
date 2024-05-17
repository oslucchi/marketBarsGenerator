package it.l_soft.barsGenerator;

public class Block implements Cloneable {

	private int id;
	private String name;
	private int numOfTrends;
	private Trend[] trends;
	private double maxBarSize;
	
	public Block(int numOfTrends, double maxBarSize, int id)
	{
		this.id = id;
		trends = new Trend[numOfTrends + 1];
		this.maxBarSize = maxBarSize;
		this.numOfTrends = numOfTrends;
	}
	public Block(int id)
	{
		this.id = id;
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
		return maxBarSize;
	}

	public int getNumOfTrends() {
		return numOfTrends;
	}
	public int getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	 
	@Override
	 public Block clone() throws CloneNotSupportedException {
		 Block blockCloned = (Block) super.clone();
		 blockCloned.trends = new Trend[trends.length];
		 int i = 0;
		 for(Trend t : trends)
		 {
			 blockCloned.trends[i++] = t.clone();
		 }
		 return blockCloned;
	 }
}
