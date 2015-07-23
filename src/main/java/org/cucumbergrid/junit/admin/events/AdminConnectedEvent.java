package org.cucumbergrid.junit.admin.events;

import org.cucumbergrid.junit.admin.events.AdminConnectedEvent.AdminConnectedHandler;
import org.cucumbergrid.junit.eventbus.Event;
import org.cucumbergrid.junit.eventbus.Handler;
import org.cucumbergrid.junit.eventbus.Type;

public class AdminConnectedEvent extends Event<AdminConnectedHandler> {

    public interface AdminConnectedHandler extends Handler<AdminConnectedEvent> {

        void onAdminConnected(AdminConnectedEvent event);
    }

    public static Type<AdminConnectedEvent> TYPE = Type.create(AdminConnectedEvent.class);

    @Override
    public void dispatch(AdminConnectedHandler handler) {
        handler.onAdminConnected(this);
    }

    @Override
    protected Type<AdminConnectedEvent> getType() {
        return TYPE;
    }
}
