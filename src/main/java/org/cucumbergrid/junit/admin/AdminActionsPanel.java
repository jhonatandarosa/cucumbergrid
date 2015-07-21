package org.cucumbergrid.junit.admin;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JPanel;
import org.cucumbergrid.junit.runtime.common.admin.AdminMessageID;

public class AdminActionsPanel extends JPanel {

    private AdminFrame frame;

    public AdminActionsPanel(AdminFrame frame) {
        this.frame = frame;
        initGUI();
    }

    private void initGUI() {
        GridLayout mgr = new GridLayout(10, 2, 5, 5);
        setLayout(mgr);

        JButton bt = new JButton("Connect");
        bt.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                getAdminApp().connect();
            }
        });
        add(bt);
        add(new AdminActionButton(this, "Finish gracefully", AdminMessageID.FINISH_GRACEFULLY));
    }

    public AdminApp getAdminApp() {
        return frame.getAdminApp();
    }
}
