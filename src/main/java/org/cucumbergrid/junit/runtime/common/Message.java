package org.cucumbergrid.junit.runtime.common;

import java.io.*;

public class Message implements Serializable {

    private MessageID id;
    private byte[] data;

    public Message(MessageID id) {
        this(id, null);
    }

    public Message(MessageID id, byte[] data) {
        this.id = id;
        this.data = data;
    }

    public MessageID getID() {
        return id;
    }

    public byte[] getData() {
        return data;
    }
}
