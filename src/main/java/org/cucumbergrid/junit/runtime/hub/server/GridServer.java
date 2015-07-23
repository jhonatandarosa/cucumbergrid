package org.cucumbergrid.junit.runtime.hub.server;

import java.util.ArrayList;
import java.util.List;
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
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.group.ChannelGroupFuture;
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
        GridServerHandler.channels.close();
        channel.close();
        bootstrap.releaseExternalResources();

    }

    public ChannelGroupFuture broadcast(Message message) {
        return GridServerHandler.channels.write(message);
    }

    public ChannelFuture send(Channel channel, Serializable data) {
        return channel.write(data);
    }

    public List<Integer> getConnectedNodes() {
        List<Integer> ids = new ArrayList<>();
        for (Channel channel : GridServerHandler.channels) {
            ids.add(channel.getId());
        }
        return ids;
    }
}
