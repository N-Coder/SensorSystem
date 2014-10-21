package de.ncoder.sensorsystem.events.event;

public interface ValueChangedEvent<T> extends Event {
    public T getOldValue();

    public T getNewValue();
}
