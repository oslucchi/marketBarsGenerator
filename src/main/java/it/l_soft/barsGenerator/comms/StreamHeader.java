package it.l_soft.barsGenerator.comms;

public class StreamHeader extends Message {
	String dataFileName;
	
	public StreamHeader()
	{
		super("H");
	}
	public StreamHeader(long timestamp, String dataFileName)
	{
		super("H");
		this.dataFileName = dataFileName;
		this.timestamp = timestamp;
	}
	
	public void setDataFileName(String dataFileName)
	{
		this.dataFileName = dataFileName;
	}
	public String getDataFileName()
	{
		return dataFileName;
	}

}
