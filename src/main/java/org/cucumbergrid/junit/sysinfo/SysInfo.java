package org.cucumbergrid.junit.sysinfo;


import java.util.Enumeration;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import org.cucumbergrid.junit.utils.IOUtils;

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

        String hostname = null;
        try {
            hostname = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            //ignore
            hostname = NativeHostnameFinder.find();
        }
        operatingSystem.setHostname(hostname);



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

    private static class NativeHostnameFinder {

        static String find() {
            Runtime run = Runtime.getRuntime();
            Process proc = null;
            try {
                proc = run.exec("hostname");
                return IOUtils.readFully(proc.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
