package de.ncoder.sensorsystem.android.sensor.base;

import android.hardware.Sensor;
import android.hardware.SensorManager;

public abstract class BufferedAndroidSensor<T> extends AndroidSensor<T> {
    private final T[] buffer;
    private int pointer = 0;

    public BufferedAndroidSensor(SensorManager sensorManager, Sensor sensor, T[] buffer) {
        super(sensorManager, sensor);
        this.buffer = buffer;
    }

    @Override
    protected void changed(T oldValue, T newValue) {
        if (!mayChange()) return;
        buffer[pointer] = newValue;
        pointer = (pointer + 1) % buffer.length;
        super.changed(oldValue, newValue);
    }

    public T[] getBuffer() {
        return buffer;
    }

    public int getBufferEnd() {
        return pointer;
    }
}
