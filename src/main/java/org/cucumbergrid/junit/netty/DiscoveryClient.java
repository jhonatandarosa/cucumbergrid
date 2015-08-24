package org.cucumbergrid.junit.netty;

import java.util.concurrent.Executors;
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
                    address = msg.getData();
                    logger.info("Address discovered: " + address.toString());
                    e.getChannel().close();
                }
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
            e.getCause().printStackTrace();
            e.getChannel().close();
        }
    };

    private int port;
    private int discoveryTimeout;
    private InetSocketAddress address;

    public DiscoveryClient(int port, int discoveryTimeout) {
        this.port = port;
        this.discoveryTimeout = discoveryTimeout;
    }

    public InetSocketAddress discover() {
        logger.info("starting discovery service");
        address = null;
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

            // handler will close the DatagramChannel when a
            // response is received.  If the channel is not closed within 5 seconds,
            // print an error message and quit.
            if (!c.getCloseFuture().await(discoveryTimeout)) {
                logger.warning("Discover request timed out.");
                c.close().awaitUninterruptibly();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            bootstrap.releaseExternalResources();
        }

        return address;
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println(new DiscoveryClient(3299, 5000).discover());
    }
}
