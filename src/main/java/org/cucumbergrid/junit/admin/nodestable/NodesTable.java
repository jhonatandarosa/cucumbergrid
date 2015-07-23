package org.cucumbergrid.junit.admin.nodestable;

import java.util.HashMap;
import java.util.Set;

import javax.swing.JTable;
import org.cucumbergrid.junit.admin.AdminFrame;
import org.cucumbergrid.junit.admin.events.RefreshActiveNodesEvent;
import org.cucumbergrid.junit.admin.events.RefreshActiveNodesEvent.RefreshActiveNodesHandler;
import org.cucumbergrid.junit.eventbus.EventBus;
import org.cucumbergrid.junit.runtime.common.NodeInfo;

public class NodesTable extends JTable {

    private final AdminFrame frame;
    private NodesTableModel model = new NodesTableModel();

    public NodesTable(AdminFrame frame) {
        this.frame = frame;
        initTable();
    }

    private void initTable() {
        setModel(model);
        setShowGrid(true);

        EventBus.getInstance().addHandler(RefreshActiveNodesEvent.TYPE, new RefreshActiveNodesHandler() {
            @Override
            public void onRefreshActiveNodes(RefreshActiveNodesEvent event) {
                refreshTable(event.getNodes());
                frame.unMask();
            }
        });
    }

    private void refreshTable(HashMap<Integer, NodeInfo> nodes) {
        model.clear();

        Set<Integer> channels = nodes.keySet();
        for (Integer channel : channels) {
            model.addRow(channel, nodes.get(channel));
        }
    }
}
