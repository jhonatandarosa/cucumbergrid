package org.cucumbergrid.junit.eventbus;

public interface Handler<E extends Event<? extends Handler<E>>> {
}
