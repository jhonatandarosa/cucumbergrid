package org.cucumbergrid.junit.runtime.common;

import java.io.*;

public class Message implements Serializable {

    private MessageID id;
    private Serializable data;

    public Message(MessageID id) {
        this(id, null);
    }

    public Message(MessageID id, Serializable data) {
        this.id = id;
        this.data = data;
    }

    public MessageID getID() {
        return id;
    }

    public <T extends Serializable> T getData() {
        return (T)data;
    }

    @Override
    public String toString() {
        return "Message["+id+"]{"+data+"}";
    }
}
