package org.cucumbergrid.junit.runtime.node.client;

import org.cucumbergrid.junit.runtime.common.Message;
import org.cucumbergrid.junit.runtime.node.CucumberGridClientHandler;
import org.jboss.netty.channel.*;

public class GridClientHandler extends SimpleChannelUpstreamHandler {

    private final CucumberGridClientHandler handler;
    private final GridClient gridClient;

    public GridClientHandler(GridClient gridClient, CucumberGridClientHandler handler) {
        this.gridClient = gridClient;
        this.handler = handler;
    }

    @Override
    public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
        if (e instanceof ChannelStateEvent) {
            if (((ChannelStateEvent) e).getState() != ChannelState.INTEREST_OPS) {
                System.err.println(e);
            }
        }
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

