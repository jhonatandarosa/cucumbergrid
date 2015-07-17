package org.cucumbergrid.junit.netty;

import java.util.concurrent.Executors;

import java.net.InetSocketAddress;
import org.cucumbergrid.junit.sysinfo.SysInfo;
import org.jboss.netty.bootstrap.ConnectionlessBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.FixedReceiveBufferSizePredictorFactory;
import org.jboss.netty.channel.socket.nio.NioDatagramChannelFactory;
import org.jboss.netty.handler.codec.serialization.ObjectDecoder;
import org.jboss.netty.handler.codec.serialization.ObjectEncoder;

public class DiscoveryServer {

    private int port;
    private Channel channel;
    private ConnectionlessBootstrap bootstrap;
    private InetSocketAddress serverAddress;

    public DiscoveryServer(int port) {
        this.port = port;
    }

    public void start() {
        bootstrap = new ConnectionlessBootstrap(new NioDatagramChannelFactory(Executors.newSingleThreadExecutor()));
        // Configure the pipeline factory.
        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
            public ChannelPipeline getPipeline() {
                return Channels.pipeline(
                        new ObjectEncoder(),
                        new ObjectDecoder(),
                        new DiscoveryServerHandler(DiscoveryServer.this));
            }
        });
        // Enable broadcast
        bootstrap.setOption("broadcast", "false");
        // Allow packets as large as up to 1024 bytes (default is 768).
        // You could increase or decrease this value to avoid truncated packets
        // or to improve memory footprint respectively.
        //
        // Please also note that a large UDP packet might be truncated or
        // dropped by your router no matter how you configured this option.
        // In UDP, a packet is truncated or dropped if it is larger than a
        // certain size, depending on router configuration.  IPv4 routers
        // truncate and IPv6 routers drop a large packet.  That's why it is
        // safe to send small packets in UDP.
        bootstrap.setOption("receiveBufferSizePredictorFactory", new FixedReceiveBufferSizePredictorFactory(1024));
        // Bind to the port and start the service.
        InetSocketAddress localAddress = new InetSocketAddress(port);
        channel = bootstrap.bind(localAddress);

        System.out.println("Discovery server listening to " + localAddress);
    }

    public void shutdown() {
        channel.close();
        bootstrap.releaseExternalResources();
    }


    public void setServerAddress(InetSocketAddress inetSocketAddress) {
        serverAddress = inetSocketAddress;
    }

    public InetSocketAddress getServerAddress() {
        return serverAddress;
    }

    public static void main(String[] args) {
        DiscoveryServer server = new DiscoveryServer(3299);
        InetSocketAddress inetSocketAddress = new InetSocketAddress(SysInfo.getInstance().getAddress(), 3299);
        server.setServerAddress(inetSocketAddress);
        server.start();
    }
}
