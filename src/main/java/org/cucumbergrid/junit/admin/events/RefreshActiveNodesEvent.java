package org.cucumbergrid.junit.admin.events;

import java.util.HashMap;

import org.cucumbergrid.junit.admin.events.RefreshActiveNodesEvent.RefreshActiveNodesHandler;
import org.cucumbergrid.junit.eventbus.Event;
import org.cucumbergrid.junit.eventbus.Handler;
import org.cucumbergrid.junit.eventbus.Type;
import org.cucumbergrid.junit.runtime.common.NodeInfo;

public class RefreshActiveNodesEvent extends Event<RefreshActiveNodesHandler> {

    public interface RefreshActiveNodesHandler extends Handler<RefreshActiveNodesEvent> {

        void onRefreshActiveNodes(RefreshActiveNodesEvent event);

    }

    public static Type<RefreshActiveNodesEvent> TYPE = Type.create(RefreshActiveNodesEvent.class);

    private final HashMap<Integer, NodeInfo> nodes;

    public RefreshActiveNodesEvent(HashMap<Integer, NodeInfo> nodes) {
        this.nodes = nodes;
    }

    public HashMap<Integer, NodeInfo> getNodes() {
        return nodes;
    }

    @Override
    public void dispatch(RefreshActiveNodesHandler handler) {
        handler.onRefreshActiveNodes(this);
    }

    @Override
    protected Type<RefreshActiveNodesEvent> getType() {
        return TYPE;
    }
}
