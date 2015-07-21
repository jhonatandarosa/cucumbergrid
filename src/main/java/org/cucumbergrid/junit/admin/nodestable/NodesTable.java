package org.cucumbergrid.junit.admin.nodestable;

import javax.swing.JTable;

public class NodesTable extends JTable {

    private NodesTableModel model = new NodesTableModel();

    public NodesTable() {
        initTable();
    }

    private void initTable() {
        setModel(model);
        setShowGrid(true);

    }
}
