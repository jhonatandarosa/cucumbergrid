package org.cucumbergrid.junit.admin;

import java.util.HashMap;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import org.cucumbergrid.junit.admin.events.AdminConnectedEvent;
import org.cucumbergrid.junit.admin.events.AdminDisconnectedEvent;
import org.cucumbergrid.junit.admin.events.RefreshActiveNodesEvent;
import org.cucumbergrid.junit.admin.events.RefreshStatsEvent;
import org.cucumbergrid.junit.eventbus.EventBus;
import org.cucumbergrid.junit.runtime.common.Message;
import org.cucumbergrid.junit.runtime.common.MessageID;
import org.cucumbergrid.junit.runtime.common.NodeInfo;
import org.cucumbergrid.junit.runtime.common.admin.AdminMessage;
import org.cucumbergrid.junit.runtime.common.admin.AdminMessageID;
import org.cucumbergrid.junit.runtime.common.admin.GridStats;
import org.cucumbergrid.junit.runtime.node.CucumberGridClientHandler;
import org.cucumbergrid.junit.runtime.node.client.GridClient;
import org.jboss.netty.channel.Channel;

public class AdminApp implements CucumberGridClientHandler {

    private GridClient client;
    private AdminFrame frame;

    public void start() {
        client = new GridClient(null, 26000, 26001, 60000);
        client.setHandler(this);
        frame = new AdminFrame(this);

        frame.setVisible(true);
    }

    public void send(AdminMessage msg) {
        client.send(new Message(MessageID.ADMIN, msg));
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
        new AdminApp().start();
    }

    @Override
    public void onDataReceived(Channel key, Message data) {
        MessageID id = data.getID();
        if (id == MessageID.ADMIN) {
            processMessage(data.<AdminMessage>getData());
        } else if (id == MessageID.SHUTDOWN) {
            client.shutdown(false);
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    client.releaseExternalResources();
                    System.exit(0);
                }
            });
        }
    }

    private void processMessage(AdminMessage message) {
        switch (message.getID()) {
            case REFRESH:
                onRefresh(message);
                break;
        }
    }

    private void onRefresh(AdminMessage message) {
        HashMap<Integer, NodeInfo> nodes = message.getData(0);
        GridStats stats = message.getData(1);
        EventBus.getInstance().fire(new RefreshActiveNodesEvent(nodes));
        EventBus.getInstance().fire(new RefreshStatsEvent(stats));
    }

    public void connect() {
        client.init();
        EventBus.getInstance().fire(new AdminConnectedEvent());
        send(new AdminMessage(AdminMessageID.REFRESH));
        frame.mask();
    }

    public boolean isConnected() {
        return client.isConnected();
    }

    public void disconnect() {
        client.shutdown();
        client.releaseExternalResources();
        EventBus.getInstance().fire(new AdminDisconnectedEvent());
    }
}
