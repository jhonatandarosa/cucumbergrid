package org.cucumbergrid.junit.sysinfo;

public enum OSArch {
    X86,
    X64;

    public static OSArch getByOSArch() {
        String arch = System.getProperty("os.arch");
        if (arch.contains("64")) {
            return X64;
        } else {
            return X86;
        }
    }
}
