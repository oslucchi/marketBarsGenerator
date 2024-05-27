package it.l_soft.barsGenerator.comms;

import java.io.Serializable;

class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    private String topic;
    private Object data;

    public Message(String topic, Object data) {
        this.topic = topic;
        this.data = data;
    }

    public String getTopic() {
        return topic;
    }

    public Object getData() {
        return data;
    }
}