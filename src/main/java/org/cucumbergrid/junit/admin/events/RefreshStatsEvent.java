package org.cucumbergrid.junit.admin.events;

import org.cucumbergrid.junit.admin.events.RefreshStatsEvent.RefreshStatsHandler;
import org.cucumbergrid.junit.eventbus.Event;
import org.cucumbergrid.junit.eventbus.Handler;
import org.cucumbergrid.junit.eventbus.Type;
import org.cucumbergrid.junit.runtime.common.admin.GridStats;

public class RefreshStatsEvent extends Event<RefreshStatsHandler> {

    public interface RefreshStatsHandler extends Handler<RefreshStatsEvent> {

        void onRefreshStats(RefreshStatsEvent event);

    }

    public static Type<RefreshStatsEvent> TYPE = Type.create(RefreshStatsEvent.class);

    private final GridStats stats;

    public RefreshStatsEvent(GridStats stats) {
        this.stats = stats;
    }

    public GridStats getStats() {
        return stats;
    }

    @Override
    public void dispatch(RefreshStatsHandler handler) {
        handler.onRefreshStats(this);
    }

    @Override
    protected Type<RefreshStatsEvent> getType() {
        return TYPE;
    }
}
