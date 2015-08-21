package org.cucumbergrid.junit.runtime.hub;

import org.cucumbergrid.junit.runtime.common.Message;
import org.jboss.netty.channel.Channel;

public interface CucumberGridServerHandler {

    void onDataReceived(Channel channel, Message data);

    void onNodeDisconnected(Channel channel);
}
