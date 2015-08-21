package org.cucumbergrid.junit.runtime.hub;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cucumbergrid.junit.runtime.common.Message;
import org.cucumbergrid.junit.runtime.common.MessageID;
import org.cucumbergrid.junit.runtime.common.NodeInfo;
import org.cucumbergrid.junit.runtime.common.admin.AdminMessage;
import org.cucumbergrid.junit.runtime.common.admin.AdminMessageID;
import org.cucumbergrid.junit.runtime.common.admin.GridStats;
import org.cucumbergrid.junit.utils.CollectionUtils;
import org.jboss.netty.channel.Channel;

public class CucumberGridServerAdminHandler {

    private CucumberGridHubRuntime runtime;

    public CucumberGridServerAdminHandler(CucumberGridHubRuntime runtime) {
        this.runtime = runtime;
    }

    public void onAdminMessage(Channel channel, AdminMessage message) {
        switch (message.getID()) {
            case FINISH:
                onFinish((Boolean)message.getData()[0]);
                break;
            case REFRESH:
                onRefresh(channel);
                break;
        }
    }

    private void send(Channel channel, AdminMessage message) {
        channel.write(new Message(MessageID.ADMIN, message));
    }

    private void onRefresh(Channel channel) {
        Map<Integer, NodeInfo> nodeInfos = runtime.getNodeInfos();
        List<Integer> connectedNodes = runtime.getConnectedNodes();

        HashMap<Integer, NodeInfo> connected = CollectionUtils.filter(nodeInfos, connectedNodes);

        GridStats stats = runtime.getGridStats();

        send(channel, new AdminMessage(AdminMessageID.REFRESH, connected, stats));
    }

    private void onFinish(boolean gracefully) {
        System.out.println("Finish requested by admin [Gracefully: ] " + gracefully);
        runtime.finish(gracefully);
    }
}
