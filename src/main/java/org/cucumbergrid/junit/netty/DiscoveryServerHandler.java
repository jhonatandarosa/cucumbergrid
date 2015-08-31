package org.cucumbergrid.junit.netty;

import java.util.logging.Level;
import java.util.logging.Logger;

import java.net.InetSocketAddress;
import org.cucumbergrid.junit.runtime.common.GridProperties;
import org.cucumbergrid.junit.runtime.common.Message;
import org.cucumbergrid.junit.runtime.common.MessageID;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

public class DiscoveryServerHandler extends SimpleChannelUpstreamHandler {
    private DiscoveryServer server;
    private Logger logger = Logger.getLogger(DiscoveryServer.class.getName());

    public DiscoveryServerHandler(DiscoveryServer server) {
        this.server = server;
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
        Object obj = e.getMessage();
        if (obj instanceof Message) {
            Message msg = (Message)obj;
            if (msg.getID() == MessageID.DISCOVERY) {
                InetSocketAddress address = server.getServerAddress();
                DiscoveryData data = new DiscoveryData(address, GridProperties.getGridId());
                Message response = new Message(MessageID.DISCOVERY, data);
                e.getChannel().write(response, e.getRemoteAddress());
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
        logger.log(Level.SEVERE, "Exception caught at discovery server", e.getCause());
        // We don't close the channel because we can keep serving requests.
    }
}
