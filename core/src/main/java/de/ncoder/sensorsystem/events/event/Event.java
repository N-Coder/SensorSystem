package de.ncoder.sensorsystem.events.event;

import de.ncoder.sensorsystem.Component;

import java.io.Serializable;

public interface Event extends Serializable {
    public long getWhen();

    public Component getSource();

    public String getName();
}
