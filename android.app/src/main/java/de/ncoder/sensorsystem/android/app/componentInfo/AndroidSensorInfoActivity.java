package de.ncoder.sensorsystem.android.app.componentInfo;

import android.content.ComponentName;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.TextView;
import de.ncoder.sensorsystem.Component;
import de.ncoder.sensorsystem.Container;
import de.ncoder.sensorsystem.android.app.R;
import de.ncoder.sensorsystem.android.sensor.base.AndroidSensor;
import de.ncoder.sensorsystem.manager.AccuracyManager;
import de.ncoder.sensorsystem.manager.event.Event;
import de.ncoder.sensorsystem.manager.event.EventManager;
import de.ncoder.sensorsystem.manager.event.EventUtils;
import de.ncoder.sensorsystem.manager.event.ValueChangedEvent;

import java.util.concurrent.TimeUnit;

public class AndroidSensorInfoActivity extends ComponentInfoActivity implements EventManager.Listener {
    @Nullable
    private AndroidSensor<?> sensor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_component_sensor);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        super.onServiceConnected(name, service);

        String clazz = getIntent().getStringExtra(EXTRA_KEY_CLASS);
        String identifier = getIntent().getStringExtra(EXTRA_KEY_IDENTIFIER);
        try {
            Container.Key key = Container.Key.findKey(clazz, identifier);
            Component component = getComponent(key);
            sensor = (AndroidSensor<?>) component;
        } catch (ClassNotFoundException | ClassCastException e) {
            Log.w(getClass().getSimpleName(), "Sensor not available", e);
        }

        runOnUiThread(updateAccuracy);
        runOnUiThread(updateValues);

        EventManager eventManager = getComponent(EventManager.KEY);
        if (eventManager != null) {
            eventManager.subscribe(this);
        } else {
            Log.w(getClass().getSimpleName(), "No EventManager available");
        }
    }

    @Override
    public void onPause() {
        EventManager eventManager = getComponent(EventManager.KEY);
        if (eventManager != null) {
            eventManager.unsubscribe(this);
        }
        super.onPause();
    }

    private long lastUpdate = System.currentTimeMillis();
    private long updateTime = 0;

    @Override
    public void handle(Event event) {
        if (event instanceof AccuracyManager.AccuracyChangedEvent) {
            runOnUiThread(updateAccuracy);
        } else if (event instanceof ValueChangedEvent) {
            if (event.getSource() != null && event.getSource() == sensor) {
                updateTime = System.currentTimeMillis() - lastUpdate;
                lastUpdate = System.currentTimeMillis();
                runOnUiThread(updateValues);
            }
        }
    }

    private final Runnable updateValues = new Runnable() {
        @Override
        public void run() {
            if (sensor != null) {
                ((TextView) findViewById(R.id.sensor_value)).setText(
                        EventUtils.toString(sensor.get()));
                ((TextView) findViewById(R.id.sensor_last_update)).setText(
                        formatDuration(updateTime, TimeUnit.MILLISECONDS));
            } else {
                ((TextView) findViewById(R.id.sensor_value)).setText(R.string.sensor_value_unknown);
            }
        }
    };

    private final Runnable updateAccuracy = new Runnable() {
        @Override
        public void run() {
            if (sensor != null) {
                ((TextView) findViewById(R.id.sensor_accuracy)).setText(
                        formatDuration(sensor.getAccuracy(), AndroidSensor.TIME_UNIT));
                ((TextView) findViewById(R.id.sensor_batch)).setText(
                        formatDuration(sensor.getMaxBatchReportLatency(), AndroidSensor.TIME_UNIT));
            } else {
                ((TextView) findViewById(R.id.sensor_accuracy)).setText(R.string.sensor_value_unknown);
                ((TextView) findViewById(R.id.sensor_batch)).setText(R.string.sensor_value_unknown);
            }
        }
    };

    private String formatDuration(long time, TimeUnit unit) {
        if (TimeUnit.HOURS.convert(time, unit) > 0) {
            return TimeUnit.HOURS.convert(time, unit) + " h";
        } else if (TimeUnit.MINUTES.convert(time, unit) > 0) {
            return TimeUnit.MINUTES.convert(time, unit) + " min";
        } else if (TimeUnit.SECONDS.convert(time, unit) > 0) {
            return TimeUnit.SECONDS.convert(time, unit) + " s";
        } else if (TimeUnit.MILLISECONDS.convert(time, unit) > 0) {
            return TimeUnit.MILLISECONDS.convert(time, unit) + " ms";
        } else if (TimeUnit.MICROSECONDS.convert(time, unit) > 0) {
            return TimeUnit.MICROSECONDS.convert(time, unit) + " µs";
        } else {
            return TimeUnit.NANOSECONDS.convert(time, unit) + " ns";
        }
    }
}
