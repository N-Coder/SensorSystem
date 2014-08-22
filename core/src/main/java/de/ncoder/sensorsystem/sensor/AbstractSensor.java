package de.ncoder.sensorsystem.sensor;

import de.ncoder.sensorsystem.AbstractComponent;
import de.ncoder.sensorsystem.manager.event.EventManager;
import de.ncoder.sensorsystem.manager.event.SimpleValueChangedEvent;

public abstract class AbstractSensor<T> extends AbstractComponent implements Sensor<T> {
    private long lastChanged;

    protected void changed(T oldValue, T newValue) {
        lastChanged = System.currentTimeMillis();
        EventManager eventManager = getOtherComponent(EventManager.KEY);
        if (eventManager != null) {
            eventManager.publish(new SimpleValueChangedEvent<>(
                    getName(), this, lastChanged, oldValue, newValue
            ));
        }
    }

    @Override
    public String getName() {
        return getClass().getName();
    }

    @Override
    public long lastChange() {
        return lastChanged;
    }
}
