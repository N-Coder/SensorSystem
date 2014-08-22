package de.ncoder.sensorsystem.manager.event;

public interface ValueChangedEvent<T> extends Event {
    public T getOldValue();

    public T getNewValue();
}
