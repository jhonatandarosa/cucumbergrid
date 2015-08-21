package org.cucumbergrid.junit.admin;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import javax.swing.JButton;
import org.cucumbergrid.junit.runtime.common.admin.AdminMessage;
import org.cucumbergrid.junit.runtime.common.admin.AdminMessageID;

public class AdminActionButton extends JButton {

    private final Serializable[] data;
    private AdminActionsPanel parent;
    private AdminMessageID messageID;

    public AdminActionButton(AdminActionsPanel parent, String title, AdminMessageID messageID, Serializable... data) {
        super(title);
        this.parent = parent;
        this.messageID = messageID;
        this.data = data;

        init();
    }

    private void init() {
        addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                parent.getAdminApp().send(new AdminMessage(messageID, data));
            }
        });
    }
}
