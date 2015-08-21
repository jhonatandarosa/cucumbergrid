package org.cucumbergrid.junit.utils;

import java.util.Properties;

import java.net.InetAddress;
import org.cucumbergrid.junit.sysinfo.SysInfo;

public class MachineUtils {

    public static void main(String[] args) throws Throwable {
        Properties properties = System.getProperties();
        properties.list(System.out);

        String computername= InetAddress.getLocalHost().getHostName();
        System.out.println(computername);
        System.out.println(SysInfo.getInstance().getAddress().getHostAddress());
    }
}
