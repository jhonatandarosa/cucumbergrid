package org.cucumbergrid.junit.runtime.hub.server;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.cucumbergrid.junit.runtime.common.Message;
import org.cucumbergrid.junit.runtime.hub.CucumberGridServerHandler;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;

public class GridServerHandler extends SimpleChannelUpstreamHandler {

    private Logger logger = Logger.getLogger(getClass().getName());
    static final ChannelGroup channels = new DefaultChannelGroup();
    private CucumberGridServerHandler handler;

    public GridServerHandler(CucumberGridServerHandler handler) {
        this.handler = handler;
    }

    @Override
    public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
        if (e instanceof ChannelStateEvent) {
            logger.log(Level.SEVERE, "Error handling upstream", e);
        }
        super.handleUpstream(ctx, e);
    }

    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) {
        Channel channel = ctx.getChannel();

        // Register the channel to the global channel list
        // so the channel received the messages from others.
        channels.add(channel);
    }

    @Override
    public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) {
        // Unregister the channel from the global channel list
        // so the channel does not receive messages anymore.
        channels.remove(e.getChannel());
        handler.onNodeDisconnected(e.getChannel());
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
        Message message = (Message) e.getMessage();
        handler.onDataReceived(e.getChannel(), message);

        // Send the received message to all channels but the current one.
/*        for (Channel c: channels) {
            if (c != e.getChannel()) {
                response.setAlias(e.getChannel().getRemoteAddress().toString());
                response.setMessage(request.getMessage());
//                c.write("[" + e.getChannel().getRemoteAddress() + "] " + request + '\n');
            } else {
                //c.write("[you] " + request + '\n');
                response.setAlias("you");
                response.setMessage(request.getMessage());
            }
            c.write(response);
        }
        */
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
        e.getCause().printStackTrace();
        e.getChannel().close();
    }
}
