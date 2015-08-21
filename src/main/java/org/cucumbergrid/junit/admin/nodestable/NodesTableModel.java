package org.cucumbergrid.junit.admin.nodestable;

import javax.swing.table.DefaultTableModel;
import org.cucumbergrid.junit.runtime.common.NodeInfo;

public class NodesTableModel extends DefaultTableModel {

    public NodesTableModel() {
        setColumnIdentifiers(new String[]{
                "Channel",
                "Hostname",
                "Username",
                "Address",
        });
    }

    public void clear() {
        setRowCount(0);
    }

    public void addRow(Integer channel, NodeInfo nodeInfo) {
        addRow(new String[]{
                channel.toString(),
                nodeInfo.getOs().getHostname(),
                nodeInfo.getOs().getUsername(),
                nodeInfo.getAddress()
        });
    }
}
