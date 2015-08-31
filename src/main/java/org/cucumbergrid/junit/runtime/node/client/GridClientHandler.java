package org.cucumbergrid.junit.runtime.node.client;

import java.util.logging.Logger;

import org.cucumbergrid.junit.runtime.common.Message;
import org.cucumbergrid.junit.runtime.node.CucumberGridClientHandler;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

public class GridClientHandler extends SimpleChannelUpstreamHandler {

    private Logger logger = Logger.getLogger(getClass().getName());

    private final CucumberGridClientHandler handler;
    private final GridClient gridClient;

    public GridClientHandler(GridClient gridClient, CucumberGridClientHandler handler) {
        this.gridClient = gridClient;
        this.handler = handler;
    }

    @Override
    public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
        super.handleUpstream(ctx, e);
    }

    @Override
    public void channelInterestChanged(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        gridClient.sendPendingMessages();
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
        Message message = (Message)e.getMessage();
        handler.onDataReceived(e.getChannel(), message);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
        e.getCause().printStackTrace();
        e.getChannel().close();
    }
}

