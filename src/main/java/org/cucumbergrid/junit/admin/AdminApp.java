package org.cucumbergrid.junit.admin;

import javax.swing.SwingUtilities;
import org.cucumbergrid.junit.runtime.common.Message;
import org.cucumbergrid.junit.runtime.common.MessageID;
import org.cucumbergrid.junit.runtime.common.admin.AdminMessage;
import org.cucumbergrid.junit.runtime.node.CucumberGridClientHandler;
import org.cucumbergrid.junit.runtime.node.client.GridClient;
import org.jboss.netty.channel.Channel;

public class AdminApp implements CucumberGridClientHandler {

    private GridClient client;
    private AdminFrame frame;

    public void start() {
        client = new GridClient(null, 26000, 26001);
        client.setHandler(this);
        frame = new AdminFrame(this);

        frame.setVisible(true);
    }

    public void send(AdminMessage msg) {
        client.send(new Message(MessageID.ADMIN, msg));
    }

    public static void main(String[] args) {
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

    }

    public void connect() {
        client.init();
    }
}
