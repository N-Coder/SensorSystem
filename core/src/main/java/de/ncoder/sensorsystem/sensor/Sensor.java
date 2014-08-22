package de.ncoder.sensorsystem.sensor;

import de.ncoder.sensorsystem.Component;

public interface Sensor<T> extends Component {
    public T get();

    public long lastChange();

    public String getName();
}
