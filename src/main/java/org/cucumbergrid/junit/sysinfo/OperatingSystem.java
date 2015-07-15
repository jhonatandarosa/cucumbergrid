package org.cucumbergrid.junit.sysinfo;

import java.io.Serializable;

public class OperatingSystem implements Serializable {

    private OSType type;
    private String name;
    private String version;
    private OSArch arch;
    private String username;
    private String hostname;

    public OSType getType() {
        return type;
    }

    public void setType(OSType type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public OSArch getArch() {
        return arch;
    }

    public void setArch(OSArch arch) {
        this.arch = arch;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }
}
