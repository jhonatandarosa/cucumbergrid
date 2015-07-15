package org.cucumbergrid.junit.sysinfo;

public enum OSType {
    WIN,
    LINUX,;

    public static OSType getByOSName() {
        String name = System.getProperty("os.name");
        name = name.toLowerCase();
        if (name.contains("win")) {
            return WIN;
        } else if (name.contains("linux")) {
            return LINUX;
        }
        return null;
    }
}
