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
    private JPanel statsPanel;

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
            mainPanel.add(getStatsPanel(), BorderLayout.PAGE_START);

            JTable table = getNodesTable();
            mainPanel.add(table.getTableHeader(), BorderLayout.NORTH);
            JScrollPane scroll = new JScrollPane(table);
            table.setFillsViewportHeight(true);
            mainPanel.add(scroll);
        }
        return mainPanel;
    }

    private JPanel getStatsPanel() {
        if (statsPanel == null) {
            statsPanel = new StatsPanel();
        }
        return statsPanel;
    }

    private JTable getNodesTable() {
        if (nodesTable == null) {
            nodesTable = new NodesTable(this);
        }
        return nodesTable;
    }

    public AdminApp getAdminApp() {
        return adminApp;
    }

    public void mask() {
        mainPanel.setEnabled(false);
    }

    public void unMask() {
        mainPanel.setEnabled(true);
    }

}
