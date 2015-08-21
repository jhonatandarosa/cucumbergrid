package org.cucumbergrid.junit.admin;

import java.awt.Dimension;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.cucumbergrid.junit.admin.events.RefreshStatsEvent;
import org.cucumbergrid.junit.admin.events.RefreshStatsEvent.RefreshStatsHandler;
import org.cucumbergrid.junit.eventbus.EventBus;
import org.cucumbergrid.junit.runtime.common.admin.GridStats;

public class StatsPanel extends JPanel {

    private JLabel lblTotalFeatures;

    private GridStats stats = new GridStats();
    private JLabel lblFeaturesExecuted;

    public StatsPanel() {
        initGUI();

        EventBus.getInstance().addHandler(RefreshStatsEvent.TYPE, new RefreshStatsHandler() {
            @Override
            public void onRefreshStats(RefreshStatsEvent event) {
                stats = event.getStats();
                refreshStats();
            }
        });
    }

    private void initGUI() {
        Dimension sz = new Dimension(800, 200);
        setPreferredSize(sz);

        setLayout(null);

        int baseX = 10;
        int baseY = 10;
        int baseH = 20;

        int row = 0;
        lblTotalFeatures = new JLabel();
        lblTotalFeatures.setBounds(baseX, baseY + baseH * row, 200, baseH);
        add(lblTotalFeatures);

        row++;

        lblFeaturesExecuted = new JLabel();
        lblFeaturesExecuted.setBounds(baseX, baseY + baseH * row, 200, baseH);
        add(lblFeaturesExecuted);

        refreshStats();
    }

    private void refreshStats() {
        lblTotalFeatures.setText("Total features: " + stats.totalFeatures);
        lblFeaturesExecuted.setText("Features executed: " + stats.featuresExecuted);
    }
}
