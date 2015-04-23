package org.cucumbergrid.junit.runtime.node.client;

import org.cucumbergrid.junit.runtime.node.CucumberGridClientHandler;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.serialization.ObjectDecoder;
import org.jboss.netty.handler.codec.serialization.ObjectEncoder;

public class GridClientPipelineFactory implements ChannelPipelineFactory {

    private GridClient gridClient;
    private CucumberGridClientHandler handler;

    public GridClientPipelineFactory(GridClient gridClient, CucumberGridClientHandler handler) {
        this.gridClient = gridClient;
        this.handler = handler;
    }

    public ChannelPipeline getPipeline() {
        ChannelPipeline pipeline = Channels.pipeline();

        pipeline.addLast("decoder", new ObjectDecoder());
        pipeline.addLast("encoder", new ObjectEncoder());

        pipeline.addLast("handler", new GridClientHandler(gridClient, handler));

        return pipeline;
    }
}
