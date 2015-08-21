package org.cucumbergrid.junit.eventbus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventBus {

    private static EventBus instance;

    public static EventBus getInstance() {
        if (instance == null) {
            instance = new EventBus();
        }
        return instance;
    }

    private Map<Type, List<Handler>> eventMap = new HashMap<>();

    private EventBus() {
    }

    private List<Handler> getHandlers(Type type) {
        List<Handler> handlers = eventMap.get(type);
        if (handlers == null) {
            handlers = new ArrayList<>();
            eventMap.put(type, handlers);
        }
        return handlers;
    }

    public <E extends Event<? extends Handler<E>>> void  addHandler(Type<E> type, Handler<E> handler) {
        List<Handler> events = getHandlers(type);
        events.add(handler);
    }

    public void fire(Event event) {
        List<Handler> handlers = getHandlers(event.getType());
        for (Handler handler : handlers) {
            event.dispatch(handler);
        }
    }
}
