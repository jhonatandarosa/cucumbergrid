package org.cucumbergrid.junit.admin.nodestable;

import javax.swing.table.DefaultTableModel;

public class NodesTableModel extends DefaultTableModel {

    public NodesTableModel() {
        setColumnIdentifiers(new String[]{
                "Channel",
                "Hostname",
                "Username",
                "Address",
        });

        addRow(new String[]{
                "1231", "D0961", "jhonatan.rosa", "192.168.172.67"
        });
    }
}
