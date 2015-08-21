package org.cucumbergrid.junit.runtime.common.admin;


import java.io.Serializable;

public class AdminMessage implements Serializable {

    private AdminMessageID id;
    private Serializable[] data;

    public AdminMessage(AdminMessageID id, Serializable... data) {
        this.id = id;
        this.data = data;
    }

    public AdminMessageID getID() {
        return id;
    }

    public Serializable[] getData() {
        return data;
    }

    public <T extends Serializable> T getData(int index) {
        return (T) data[index];
    }
}