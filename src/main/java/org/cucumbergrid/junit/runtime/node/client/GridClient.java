package org.cucumbergrid.junit.runtime.node.client;

import java.util.concurrent.Executors;

import java.io.Serializable;
import java.net.InetSocketAddress;
import org.cucumbergrid.junit.runner.CucumberGridNode;
import org.cucumbergrid.junit.runtime.node.CucumberGridClientHandler;
import org.cucumbergrid.junit.runtime.node.CucumberGridNodeRuntime;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

public class GridClient {

    private CucumberGridClientHandler handler;
    private String hubAddress;
    private int port;
    private int selectTimeout;
    private int connectTimeout;
    private int maxRetries;
    private int retries;
    private Channel channel;
    private ClientBootstrap bootstrap;

    public GridClient(CucumberGridNode config) {
        this(config.hub(), config.port(), config.selectTimeout(), config.connectTimeout(), config.maxRetries());
    }

    public GridClient(String hubAddress, int port, int selectTimeout, int connectTimeout, int maxRetries) {
        this.hubAddress = hubAddress;
        this.port = port;
        this.selectTimeout = selectTimeout;
        this.connectTimeout = connectTimeout;
        this.maxRetries = maxRetries;
    }

    public void init() {
        // Configure the client.
        bootstrap = new ClientBootstrap(
                new NioClientSocketChannelFactory(
                        Executors.newCachedThreadPool(),
                        Executors.newCachedThreadPool()));


        // Configure the pipeline factory.
        bootstrap.setPipelineFactory(new GridClientPipelineFactory(handler));

        // Start the connection attempt.
        ChannelFuture future = bootstrap.connect(new InetSocketAddress(hubAddress, port));

        // Wait until the connection attempt succeeds or fails.
        channel = future.awaitUninterruptibly().getChannel();

    }

    public void setHandler(CucumberGridNodeRuntime handler) {
        this.handler = handler;
    }

    public void send(Serializable data) {
        channel.write(data);
    }

    public boolean isConnected() {
        return channel.isConnected();
    }

    public void shutdown() {
        channel.disconnect();
    }

    public void releaseExternalResources() {
        bootstrap.releaseExternalResources();
    }
}
