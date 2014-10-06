package de.ncoder.sensorsystem.android.sensor.base;

import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.util.Log;
import de.ncoder.sensorsystem.Container;
import de.ncoder.sensorsystem.android.manager.SystemLooper;
import de.ncoder.sensorsystem.events.EventManager;
import de.ncoder.sensorsystem.events.event.Event;
import de.ncoder.sensorsystem.manager.AccuracyManager;
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

        int accuracy = getAccuracy();
        Log.d(getClass().getSimpleName(), "Accuracy: " + accuracy + "µs = " + TimeUnit.MICROSECONDS.toMillis(accuracy) + "ms");

        Handler handler = null;
        SystemLooper looper = getContainer().get(SystemLooper.KEY);
        if (looper != null) {
            handler = new Handler(looper.getLooper());
        }

        if (!sensorManager.registerListener(this, sensor, accuracy, getMaxBatchReportLatency(), handler)) {
            Log.w(getClass().getSimpleName(), "Could not register sensor listener");
        }
    }

    @Override
    public T get() {
        return cachedValue;
    }

    @Override
    protected void changed(T oldValue, T newValue) {
        //TODO limit change rate (AccuracySensor floods the MQ and database)
        if (oldValue == null) {
            oldValue = cachedValue;
        }
        cachedValue = newValue;
        super.changed(oldValue, newValue);
    }

    public static final TimeUnit TIME_UNIT = TimeUnit.MICROSECONDS;

    private static final int RATE_LEAST_ACC = (int) TIME_UNIT.convert(25, TimeUnit.SECONDS);
    private static final int RATE_MOST_ACC = (int) TIME_UNIT.convert(5, TimeUnit.SECONDS);

    public int getAccuracy() {
        AccuracyManager accuracyManager = getOtherComponent(AccuracyManager.KEY);
        if (accuracyManager != null) {
            return accuracyManager.scale(RATE_LEAST_ACC, RATE_MOST_ACC);
        } else {
            return RATE_LEAST_ACC;
        }
    }

    private static final int BATCH_LEAST_ACC = (int) TIME_UNIT.convert(10, TimeUnit.SECONDS);
    private static final int BATCH_MOST_ACC = (int) TIME_UNIT.convert(200, TimeUnit.MILLISECONDS);

    public int getMaxBatchReportLatency() {
        AccuracyManager accuracyManager = getOtherComponent(AccuracyManager.KEY);
        if (accuracyManager != null) {
            return accuracyManager.scale(BATCH_LEAST_ACC, BATCH_MOST_ACC);
        } else {
            return BATCH_LEAST_ACC;
        }
    }

    private final EventManager.Listener accuracyListener = new EventManager.Listener() {
        @Override
        public void handle(Event event) {
            if (event instanceof AccuracyManager.AccuracyChangedEvent) {
                updateSensorListener();
            }
        }
    };
}
