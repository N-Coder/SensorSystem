package de.ncoder.sensorsystem.events;

import de.ncoder.sensorsystem.events.event.Event;

public interface EventListener {
    void handle(Event event);
}
