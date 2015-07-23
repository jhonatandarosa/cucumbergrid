package org.cucumbergrid.junit.admin;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import org.cucumbergrid.junit.admin.events.AdminConnectedEvent;
import org.cucumbergrid.junit.admin.events.AdminConnectedEvent.AdminConnectedHandler;
import org.cucumbergrid.junit.admin.events.AdminDisconnectedEvent;
import org.cucumbergrid.junit.admin.events.AdminDisconnectedEvent.AdminDisconnectedHandler;
import org.cucumbergrid.junit.eventbus.EventBus;
import org.cucumbergrid.junit.runtime.common.admin.AdminMessageID;

public class AdminActionsPanel extends JPanel {

    private AdminFrame frame;
    private AdminActionButton btFinishGracefully;
    private AdminActionButton btRefresh;
    private JButton btConnect;
    private AdminActionButton btFinish;

    public AdminActionsPanel(AdminFrame frame) {
        this.frame = frame;
        initGUI();
        initHandlers();
    }

    private void initHandlers() {
        EventBus eventBus = EventBus.getInstance();

        eventBus.addHandler(AdminConnectedEvent.TYPE, new AdminConnectedHandler() {
            @Override
            public void onAdminConnected(AdminConnectedEvent event) {
                setAdminActionsEnabled(true);
            }
        });

        eventBus.addHandler(AdminDisconnectedEvent.TYPE, new AdminDisconnectedHandler() {
            @Override
            public void onAdminDisconnected(AdminDisconnectedEvent event) {
                setAdminActionsEnabled(false);
            }
        });
    }

    private void initGUI() {
        setBorder(new BevelBorder(BevelBorder.RAISED));
        GridLayout mgr = new GridLayout(10, 2, 5, 5);
        setLayout(mgr);

        btConnect = new JButton("Connect");
        btConnect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (getAdminApp().isConnected()) {
                    getAdminApp().disconnect();
                } else {
                    getAdminApp().connect();
                }
            }
        });
        add(btConnect);

        btRefresh = new AdminActionButton(this, "Refresh", AdminMessageID.REFRESH);
        btRefresh.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.mask();
            }
        });
        add(btRefresh);

        btFinishGracefully = new AdminActionButton(this, "Finish gracefully", AdminMessageID.FINISH, true);
        btFinishGracefully.setEnabled(false);
        add(btFinishGracefully);

        btFinish = new AdminActionButton(this, "Force finish", AdminMessageID.FINISH, false);
        add(btFinish);

        setAdminActionsEnabled(false);
    }

    public void setAdminActionsEnabled(boolean enabled) {
        btRefresh.setEnabled(enabled);
//        btFinishGracefully.setEnabled(enabled);
        btFinish.setEnabled(enabled);

        btConnect.setText(enabled ? "Disconnect" : "Connect");
    }

    public AdminApp getAdminApp() {
        return frame.getAdminApp();
    }
}
