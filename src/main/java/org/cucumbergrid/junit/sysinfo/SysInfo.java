package org.cucumbergrid.junit.sysinfo;


import java.util.Enumeration;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;

public class SysInfo {

    private static SysInfo instance;

    public static SysInfo getInstance() {
        if (instance == null) {
            instance = new SysInfo();
            instance.discover();
        }
        return instance;
    }

    private OperatingSystem operatingSystem;
    private InetAddress address;

    private void discover() {
        operatingSystem = new OperatingSystem();
        operatingSystem.setName(System.getProperty("os.name"));
        operatingSystem.setVersion(System.getProperty("os.version"));
        operatingSystem.setType(OSType.getByOSName());
        operatingSystem.setArch(OSArch.getByOSArch());
        operatingSystem.setUsername(System.getProperty("user.name"));

        try {
            String hostname = InetAddress.getLocalHost().getHostName();
            operatingSystem.setHostname(hostname);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }



        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

            netInterfaceLoop:
            while (interfaces.hasMoreElements()) {
                NetworkInterface netInterface = interfaces.nextElement();
                if (netInterface.isLoopback()
                        || netInterface.isVirtual()
                        || !netInterface.isUp())
                    continue;

                for (InterfaceAddress interfaceAddress : netInterface.getInterfaceAddresses()) {
                    address = interfaceAddress.getAddress();
                    if (address != null && address instanceof Inet4Address) {
                        break netInterfaceLoop;
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
            address = null;
        }

    }

    public OperatingSystem getOperatingSystem() {
        return operatingSystem;
    }

    public InetAddress getAddress() {
        return address;
    }
}
