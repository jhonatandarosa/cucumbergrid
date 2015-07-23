package org.cucumbergrid.junit.admin.events;

import org.cucumbergrid.junit.admin.events.AdminDisconnectedEvent.AdminDisconnectedHandler;
import org.cucumbergrid.junit.eventbus.Event;
import org.cucumbergrid.junit.eventbus.Handler;
import org.cucumbergrid.junit.eventbus.Type;

public class AdminDisconnectedEvent extends Event<AdminDisconnectedHandler> {

    public interface AdminDisconnectedHandler extends Handler<AdminDisconnectedEvent> {

        void onAdminDisconnected(AdminDisconnectedEvent event);
    }

    public static Type<AdminDisconnectedEvent> TYPE = Type.create(AdminDisconnectedEvent.class);

    @Override
    public void dispatch(AdminDisconnectedHandler handler) {
        handler.onAdminDisconnected(this);
    }

    @Override
    protected Type<AdminDisconnectedEvent> getType() {
        return TYPE;
    }
}
