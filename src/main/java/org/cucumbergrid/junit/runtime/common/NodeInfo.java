package org.cucumbergrid.junit.runtime.common;

import java.util.Map;

import java.io.Serializable;
import org.cucumbergrid.junit.sysinfo.OperatingSystem;

public class NodeInfo implements Serializable {

    private OperatingSystem os;

    private String address;

    private Map<String, String> properties;

    public OperatingSystem getOs() {
        return os;
    }

    public void setOs(OperatingSystem os) {
        this.os = os;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public Map<String, String> getProperties() {
        return properties;
    }
}
