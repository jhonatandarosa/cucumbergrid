package org.cucumbergrid.junit.runtime.node.client;

import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import org.cucumbergrid.junit.netty.DiscoveryClient;
import org.cucumbergrid.junit.runner.CucumberGridNode;
import org.cucumbergrid.junit.runtime.common.GridProperties;
import org.cucumbergrid.junit.runtime.node.CucumberGridClientHandler;
import org.cucumbergrid.junit.utils.StringUtils;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

public class GridClient {

    private Logger logger = Logger.getLogger(getClass().getName());

    private int discoveryServicePort;
    private int discoveryServiceTimeout;
    private CucumberGridClientHandler handler;
    private String hubAddress;
    private int port;
    private Channel channel;
    private ClientBootstrap bootstrap;
    private ConcurrentLinkedDeque<Serializable> pendingMessages = new ConcurrentLinkedDeque<>();
    private boolean shutdownScheduled;

    public GridClient(CucumberGridNode config) {
        this(config.hub(), config.port(), config.discoveryServicePort(), config.discoveryServiceTimeout());
    }

    public GridClient(String hubAddress, int port, int discoveryServicePort, int discoveryServiceTimeout) {
        this.hubAddress = hubAddress;
        this.port = port;
        this.discoveryServicePort = discoveryServicePort;
        this.discoveryServiceTimeout = discoveryServiceTimeout;
    }

    public void init() {
        if (StringUtils.isNullOrEmpty(hubAddress)) {
            DiscoveryClient discoveryClient = new DiscoveryClient(discoveryServicePort, discoveryServiceTimeout);
            InetSocketAddress address = discoveryClient.discover(GridProperties.getGridId());
            if (address == null) {
                throw new IllegalStateException("Hub address not specified and there's no result in server discovery");
            }
            hubAddress = address.getHostString();
            port = address.getPort();
        }
        // Configure the client.
        bootstrap = new ClientBootstrap(
                new NioClientSocketChannelFactory(
                        Executors.newCachedThreadPool(),
                        Executors.newCachedThreadPool()));


        // Configure the pipeline factory.
        bootstrap.setPipelineFactory(new GridClientPipelineFactory(this, handler));

        // Start the connection attempt.
        ChannelFuture future = bootstrap.connect(new InetSocketAddress(hubAddress, port));

        // Wait until the connection attempt succeeds or fails.
        channel = future.awaitUninterruptibly().getChannel();
    }

    public void setHandler(CucumberGridClientHandler handler) {
        this.handler = handler;
    }

    public void send(Serializable data) {
        sendPendingMessages();
        if (channel.isWritable()) {
            channel.write(data);
        } else {
            pendingMessages.add(data);
        }
    }

    void sendPendingMessages() {
        ChannelFuture lastFuture = null;
        while (channel.isWritable() && !pendingMessages.isEmpty()) {
            lastFuture = channel.write(pendingMessages.poll());
        }
        if (shutdownScheduled && pendingMessages.isEmpty()) {
            logger.info("Shutting down...");
            if (lastFuture != null) {
                lastFuture.addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        channel.close();
                    }
                });
            } else {
                logger.info("closing...");
                channel.close();
            }
        }
    }

    public boolean isConnected() {
        return channel != null && channel.isConnected();
    }

    public void shutdown() {
        shutdown(true);
    }

    public void shutdown(boolean gracefully) {
        if (gracefully && !pendingMessages.isEmpty()) {
            shutdownScheduled = true;
        } else {
            channel.close();
            pendingMessages.clear();
        }
    }

    public SocketAddress getAddress() {
        return channel.getLocalAddress();
    }

    public void releaseExternalResources() {
        bootstrap.releaseExternalResources();
    }
}
