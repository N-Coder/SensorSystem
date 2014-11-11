package de.ncoder.sensorsystem.android.sensor.base;

import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import de.ncoder.sensorsystem.Container;
import de.ncoder.sensorsystem.android.manager.SystemLooper;
import de.ncoder.sensorsystem.events.EventListener;
import de.ncoder.sensorsystem.events.EventManager;
import de.ncoder.sensorsystem.events.event.Event;
import de.ncoder.sensorsystem.manager.accuracy.AccuracyManager;
import de.ncoder.sensorsystem.manager.accuracy.LongAccuracyRange;
import de.ncoder.sensorsystem.sensor.AbstractSensor;

import java.util.concurrent.TimeUnit;

public abstract class AndroidSensor<T> extends AbstractSensor<T> implements SensorEventListener {
    protected final SensorManager sensorManager;
    protected final Sensor sensor;
    private T cachedValue;

    public AndroidSensor(SensorManager sensorManager, Sensor sensor) {
        this.sensorManager = sensorManager;
        this.sensor = sensor;
        if (sensor == null) {
            Log.w(getClass().getSimpleName(), "Device doesn't have the required sensor type installed");
        }
    }

    @Override
    public void init(Container container) {
        super.init(container);
        updateSensorListener();
        EventManager eventManager = getOtherComponent(EventManager.KEY);
        if (eventManager != null) {
            eventManager.subscribe(accuracyListener);
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        EventManager eventManager = getOtherComponent(EventManager.KEY);
        if (eventManager != null) {
            eventManager.unsubscribe(accuracyListener);
        }
        sensorManager.unregisterListener(this, sensor);
    }

    protected void updateSensorListener() {
        sensorManager.unregisterListener(AndroidSensor.this, sensor);

        long accuracy = getAccuracy().scale(getOtherComponent(AccuracyManager.KEY));
        long batchLatency = getBatchReportLatency().scale(getOtherComponent(AccuracyManager.KEY));
        Log.d(getClass().getSimpleName(), "Accuracy: " + getAccuracy().getAdditional().toMillis(accuracy) + "ms, " +
                "BatchLatency: " + getBatchReportLatency().getAdditional().toMillis(batchLatency) + "ms");

        Handler handler = null;
        SystemLooper looper = getContainer().get(SystemLooper.KEY);
        if (looper != null && looper.getLooper() != null) {
            handler = new Handler(looper.getLooper());
        }
        boolean success;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            success = sensorManager.registerListener(this, sensor,
                    (int) getAccuracy().getAdditional().toMicros(accuracy),
                    (int) getBatchReportLatency().getAdditional().toMicros(batchLatency),
                    handler);
        } else {
            success = sensorManager.registerListener(this, sensor,
                    (int) getAccuracy().getAdditional().toMicros(accuracy),
                    handler);
        }
        if (!success) {
            Log.w(getClass().getSimpleName(), "Could not register sensor listener");
        }
    }

    @Override
    public T get() {
        return cachedValue;
    }

    @Override
    protected void changed(T oldValue, T newValue) {
        if (!mayChange()) return;
        if (oldValue == null) {
            oldValue = cachedValue;
        }
        cachedValue = newValue;
        super.changed(oldValue, newValue);
    }

    private final LongAccuracyRange<TimeUnit> accuracy = new LongAccuracyRange<>(
            25_000_000L, 5_000_000L, TimeUnit.MICROSECONDS
    );

    public LongAccuracyRange<TimeUnit> getAccuracy() {
        return accuracy;
    }

    private final LongAccuracyRange<TimeUnit> batchReportLatency = new LongAccuracyRange<>(
            10_000_000L, 200_000L, TimeUnit.MICROSECONDS
    );

    public LongAccuracyRange<TimeUnit> getBatchReportLatency() {
        return batchReportLatency;
    }

    private final EventListener accuracyListener = new EventListener() {
        @Override
        public void handle(Event event) {
            if (event instanceof AccuracyManager.AccuracyChangedEvent) {
                updateSensorListener();
            }
        }
    };
}
