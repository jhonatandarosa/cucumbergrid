package org.cucumbergrid.junit.eventbus;

public abstract class Event<H extends Handler<? extends Event<H>>> {

    public abstract void dispatch(H handler);

    protected abstract <E extends Event<H>> Type<E> getType();
}
