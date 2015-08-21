package org.cucumbergrid.junit.runtime.hub.server;

import org.cucumbergrid.junit.runtime.hub.CucumberGridServerHandler;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.serialization.ObjectDecoder;
import org.jboss.netty.handler.codec.serialization.ObjectEncoder;

public class GridServerPipelineFactory implements ChannelPipelineFactory {

    private CucumberGridServerHandler handler;

    public GridServerPipelineFactory(CucumberGridServerHandler handler) {
        this.handler = handler;
    }

    public ChannelPipeline getPipeline() {
        ChannelPipeline pipeline = Channels.pipeline();

        pipeline.addLast("decoder", new ObjectDecoder());
        pipeline.addLast("encoder", new ObjectEncoder());

        pipeline.addLast("handler", new GridServerHandler(handler));

        return pipeline;
    }
}
