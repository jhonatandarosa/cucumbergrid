package org.cucumbergrid.junit.admin;

import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.WindowConstants;
import org.cucumbergrid.junit.admin.nodestable.NodesTable;

public class AdminFrame extends JFrame {

    private final AdminApp adminApp;
    private JPanel mainPanel;
    private JTable nodesTable;
    private JPanel adminActionsPanel;

    public AdminFrame(AdminApp adminApp) {
        this.adminApp = adminApp;
        initGUI();
    }

    private void initGUI() {
        setTitle("CucumberGrid Admin");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        add(getAdminActionsPanel(), BorderLayout.WEST);
        add(getMainPanel(), BorderLayout.CENTER);
    }

    private JPanel getAdminActionsPanel() {
        if (adminActionsPanel == null) {
            adminActionsPanel = new AdminActionsPanel(this);
            Dimension sz = new Dimension(200, getHeight());
            adminActionsPanel.setMinimumSize(sz);
            adminActionsPanel.setPreferredSize(sz);
        }
        return adminActionsPanel;
    }

    private JPanel getMainPanel() {
        if (mainPanel == null) {
            mainPanel = new JPanel();
            mainPanel.setLayout(new BorderLayout());
            JTable table = getNodesTable();
            mainPanel.add(table.getTableHeader(), BorderLayout.PAGE_START);
            JScrollPane scroll = new JScrollPane(table);
            table.setFillsViewportHeight(true);
            mainPanel.add(scroll);
        }
        return mainPanel;
    }

    private JTable getNodesTable() {
        if (nodesTable == null) {
            nodesTable = new NodesTable();
        }
        return nodesTable;
    }

    public AdminApp getAdminApp() {
        return adminApp;
    }
}
