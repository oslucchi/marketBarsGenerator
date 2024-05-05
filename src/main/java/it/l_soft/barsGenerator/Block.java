package it.l_soft.barsGenerator;

public class Block implements Cloneable {

	private int id;
	private String name;
	private int numOfTrends;
	private Trend[] trends;
	private double maxIntrabarVol;
	
	public Block(int numOfTrends, double maxIntrabarVol, int id)
	{
		this.id = id;
		trends = new Trend[numOfTrends + 1];
		this.maxIntrabarVol = maxIntrabarVol;
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
		return maxIntrabarVol;
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
