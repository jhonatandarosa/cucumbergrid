package org.cucumbergrid.junit.runtime.hub.server;

import java.util.concurrent.Executors;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import org.cucumbergrid.junit.netty.DiscoveryServer;
import org.cucumbergrid.junit.runner.CucumberGridHub;
import org.cucumbergrid.junit.runtime.common.Message;
import org.cucumbergrid.junit.runtime.hub.CucumberGridServerHandler;
import org.cucumbergrid.junit.sysinfo.SysInfo;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

public class GridServer  {

    private int port;
    private int discoveryServicePort;
    private InetAddress serverAddress;

    private CucumberGridServerHandler handler;
    private ServerBootstrap bootstrap;
    private Channel channel;
    private InetSocketAddress inetSocketAddress;
    private DiscoveryServer discoveryServer;

    public GridServer(int port, int discoveryServicePort) {
        this.port = port;
        this.discoveryServicePort = discoveryServicePort;
    }


    public GridServer(CucumberGridHub config) {
        this(config.port(), config.discoveryServicePort());
    }

    public void setHandler(CucumberGridServerHandler handler) {
        this.handler = handler;
    }

    public void init() {
        serverAddress = SysInfo.getInstance().getAddress();

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

        discoveryServer = new DiscoveryServer(discoveryServicePort);
        discoveryServer.start();
        discoveryServer.setServerAddress(inetSocketAddress);

        System.out.println("Server listening to " + inetSocketAddress);
    }

    public void shutdown() {
        discoveryServer.shutdown();

        channel.close();
        bootstrap.releaseExternalResources();

    }

    public void broadcast(Message message) {
        GridServerHandler.channels.write(message);
    }

    public void send(Channel channel, Serializable data) {
        channel.write(data);
    }
}
