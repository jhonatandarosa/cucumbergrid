package org.cucumbergrid.junit.runtime.common;

import java.io.Serializable;

public class FormatMessage implements Serializable {

    private FormatMessageID id;
    private Serializable[] data;

    public FormatMessage(FormatMessageID id, Serializable ... data) {
        this.id = id;
        this.data = data;
    }

    public FormatMessageID getID() {
        return id;
    }

    public Serializable[] getData() {
        return data;
    }

    public <T extends Serializable> T getData(int index) {
        return (T)data[index];
    }
}
