package it.l_soft.barsGenerator.comms;

public class Message {
    protected String topic;
    protected long timestamp;
    
    public Message(String topic) {
        this.topic = topic;
    }

    public String getTopic() {
        return topic;
    }

    public long getTimestamp() {
    	return timestamp;
    }
    
	public void setTopic(String topic) {
		this.topic = topic;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

}