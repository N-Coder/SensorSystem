package de.ncoder.sensorsystem.sensor;

import de.ncoder.sensorsystem.AbstractComponent;
import de.ncoder.sensorsystem.events.event.SimpleValueChangedEvent;

import java.util.concurrent.TimeUnit;

public abstract class AbstractSensor<T> extends AbstractComponent implements Sensor<T> {
    private long lastChanged;
    private long maxChangeRate = 200 /*ms*/; //not an AccuracyRange for efficiency reasons

    protected boolean mayChange() {
        return System.currentTimeMillis() - lastChanged > maxChangeRate;
    }

    protected void changed(T oldValue, T newValue) {
        if (!mayChange()) return;
        lastChanged = System.currentTimeMillis();
        publish(new SimpleValueChangedEvent<>(
                getName(), this, lastChanged, oldValue, newValue
        ));
    }

    public long getMaxChangeRate() {
        return maxChangeRate;
    }

    public void setMaxChangeRate(long maxChangeRate, TimeUnit unit) {
        this.maxChangeRate = unit.toMillis(maxChangeRate);
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
