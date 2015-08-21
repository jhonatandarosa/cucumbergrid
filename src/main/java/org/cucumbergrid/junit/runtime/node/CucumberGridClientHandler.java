package org.cucumbergrid.junit.runtime.node;

import org.cucumbergrid.junit.runtime.common.Message;
import org.jboss.netty.channel.Channel;

public interface CucumberGridClientHandler {
    void onDataReceived(Channel key, Message data);

}
