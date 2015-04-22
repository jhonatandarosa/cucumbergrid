package org.cucumbergrid.junit.runtime.node.client;

import org.cucumbergrid.junit.runtime.common.Message;
import org.cucumbergrid.junit.runtime.node.CucumberGridClientHandler;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

public class GridClientHandler extends SimpleChannelUpstreamHandler {

    private final CucumberGridClientHandler handler;

    public GridClientHandler(CucumberGridClientHandler handler) {
        this.handler = handler;
    }

    @Override
    public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
        if (e instanceof ChannelStateEvent) {
            System.err.println(e);
        }
        super.handleUpstream(ctx, e);
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

