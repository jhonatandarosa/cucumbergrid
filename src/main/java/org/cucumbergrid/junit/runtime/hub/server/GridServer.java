package org.cucumbergrid.junit.runtime.hub.server;

import java.util.concurrent.Executors;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
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
            serverAddress = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        bootstrap = new ServerBootstrap(
                new NioServerSocketChannelFactory(
                        Executors.newCachedThreadPool(),
                        Executors.newCachedThreadPool()));

        // Configure the pipeline factory.
        bootstrap.setPipelineFactory(new GridServerPipelineFactory(handler));

        // Bind and start to accept incoming connections.
        channel = bootstrap.bind(new InetSocketAddress(serverAddress, port));

        System.out.println("Server listening to " + serverAddress.getHostAddress() + ":" + port);
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
