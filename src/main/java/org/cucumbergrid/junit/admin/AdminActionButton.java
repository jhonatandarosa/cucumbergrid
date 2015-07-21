package org.cucumbergrid.junit.admin;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import org.cucumbergrid.junit.runtime.common.admin.AdminMessage;
import org.cucumbergrid.junit.runtime.common.admin.AdminMessageID;

public class AdminActionButton extends JButton {

    private AdminActionsPanel parent;
    private AdminMessageID messageID;

    public AdminActionButton(AdminActionsPanel parent, String title, AdminMessageID messageID) {
        super(title);
        this.parent = parent;
        this.messageID = messageID;

        init();
    }

    private void init() {
        addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                parent.getAdminApp().send(new AdminMessage(messageID));
            }
        });
    }
}
