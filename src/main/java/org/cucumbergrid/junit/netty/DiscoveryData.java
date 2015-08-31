package org.cucumbergrid.junit.netty;

import java.io.Serializable;
import java.net.InetSocketAddress;

public class DiscoveryData implements Serializable {

    private InetSocketAddress address;
    private String gridId;

    public DiscoveryData() {
    }

    public DiscoveryData(InetSocketAddress address, String gridId) {
        this.address = address;
        this.gridId = gridId;
    }

    public InetSocketAddress getAddress() {
        return address;
    }

    public void setAddress(InetSocketAddress address) {
        this.address = address;
    }

    public String getGridId() {
        return gridId;
    }

    public void setGridId(String gridId) {
        this.gridId = gridId;
    }

    @Override
    public String toString() {
        return "DiscoveryData["+address+"/"+gridId+"]";
    }
}
