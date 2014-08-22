package de.ncoder.sensorsystem.manager.event;

import de.ncoder.sensorsystem.Component;

public interface Event {
    public long getWhen();

    public Component getSource();

    public String getName();
}
