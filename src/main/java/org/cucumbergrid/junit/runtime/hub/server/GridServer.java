package org.cucumbergrid.junit.runtime.hub.server;

import java.util.Enumeration;
import java.util.concurrent.Executors;

import java.io.Serializable;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import org.cucumbergrid.junit.runner.CucumberGridHub;
import org.cucumbergrid.junit.runtime.common.Message;
import org.cucumbergrid.junit.runtime.hub.CucumberGridServerHandler;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

public class GridServer  {

    private int port;
    private InetAddress serverAddress;

    private CucumberGridServerHandler handler;
    private ServerBootstrap bootstrap;
    private Channel channel;
    private InetSocketAddress inetSocketAddress;

    public GridServer(int port) {
        this.port = port;
    }


    public GridServer(CucumberGridHub config) {
        this(config.port());
    }

    public void setHandler(CucumberGridServerHandler handler) {
        this.handler = handler;
    }

    public void init() {
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
                    serverAddress = interfaceAddress.getAddress();
                    if (serverAddress != null && serverAddress instanceof Inet4Address) {
                        break netInterfaceLoop;
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
            serverAddress = null;
        }


        bootstrap = new ServerBootstrap(
                new NioServerSocketChannelFactory(
                        Executors.newCachedThreadPool(),
                        Executors.newCachedThreadPool()));

        // Configure the pipeline factory.
        bootstrap.setPipelineFactory(new GridServerPipelineFactory(handler));

        // Bind and start to accept incoming connections.
        if (serverAddress == null) {
            inetSocketAddress = new InetSocketAddress("0.0.0.0", port);
            channel = bootstrap.bind(inetSocketAddress);
        } else {
            inetSocketAddress = new InetSocketAddress(serverAddress, port);
            channel = bootstrap.bind(inetSocketAddress);
        }

        System.out.println("Server listening to " + inetSocketAddress);
    }

    public void shutdown() {
        channel.disconnect();
        bootstrap.releaseExternalResources();
    }

    public void broadcast(Message message) {
        GridServerHandler.channels.write(message);
    }

    public void send(Channel channel, Serializable data) {
        channel.write(data);
    }
}
