package org.cucumbergrid.junit.netty;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.net.InetSocketAddress;
import org.cucumbergrid.junit.runtime.common.Message;
import org.cucumbergrid.junit.runtime.common.MessageID;
import org.jboss.netty.bootstrap.ConnectionlessBootstrap;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.FixedReceiveBufferSizePredictorFactory;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.socket.DatagramChannel;
import org.jboss.netty.channel.socket.nio.NioDatagramChannelFactory;
import org.jboss.netty.handler.codec.serialization.ObjectDecoder;
import org.jboss.netty.handler.codec.serialization.ObjectEncoder;

public class DiscoveryClient {

    private Logger logger = Logger.getLogger(DiscoveryClient.class.getName());

    SimpleChannelUpstreamHandler handler = new SimpleChannelUpstreamHandler() {

        @Override
        public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
            Object obj = e.getMessage();
            if (obj instanceof Message) {
                Message msg = (Message) obj;
                if (msg.getID() == MessageID.DISCOVERY) {
                    DiscoveryData discoveryData = msg.getData();
                    logger.info("Address discovered: " + discoveryData);
                    discoveryDataList.add(discoveryData);
                    if ((gridId != null && gridId.equals(discoveryData.getGridId())) ||
                        gridId == discoveryData.getGridId()) {
                        found = discoveryData;
                        logger.info("Grid id matches, closing discovery client...");
                        e.getChannel().close();
                    } else {
                        logger.info("Discovered address does not match the grid id: " + gridId);
                    }
                }
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
            logger.log(Level.SEVERE, "Exception caught at DiscoveryClient", e.getCause());
            e.getChannel().close();
        }
    };

    private int port;
    private int discoveryTimeout;
    private List<DiscoveryData> discoveryDataList = new ArrayList<>();
    private DiscoveryData found;
    private String gridId;

    public DiscoveryClient(int port, int discoveryTimeout) {
        this.port = port;
        this.discoveryTimeout = discoveryTimeout;
    }

    public InetSocketAddress discover(String gridId) {
        this.gridId = gridId;
        logger.info("starting discovery service with grid id " + gridId);
        found = null;
        discoveryDataList.clear();
        ConnectionlessBootstrap bootstrap = new ConnectionlessBootstrap(
                new NioDatagramChannelFactory(Executors.newCachedThreadPool()));
        try {
            // Configure the pipeline factory.
            bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
                public ChannelPipeline getPipeline() {
                    return Channels.pipeline(
                            new ObjectEncoder(),
                            new ObjectDecoder(),
                            handler);
                }
            });
            // Enable broadcast
            bootstrap.setOption("broadcast", "true");
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
            DatagramChannel c = (DatagramChannel) bootstrap.bind(new InetSocketAddress(0));

            Message msg = new Message(MessageID.DISCOVERY);
            c.write(msg, new InetSocketAddress("255.255.255.255", port));

            if (!c.getCloseFuture().await(discoveryTimeout)) {
                logger.warning("Discover request timed out.");
                c.close().awaitUninterruptibly();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            bootstrap.releaseExternalResources();
        }

        return found != null ? found.getAddress() : null;
    }
}
