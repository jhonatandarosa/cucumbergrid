package org.cucumbergrid.junit.eventbus;

public class Type<T> {

    private Class<T> clazz;

    private Type(Class<T> clazz) {
        this.clazz = clazz;
    }

    public static <E extends Event<? extends Handler<E>>> Type<E> create(Class<E> clazz) {
        return new Type(clazz);
    }
}
