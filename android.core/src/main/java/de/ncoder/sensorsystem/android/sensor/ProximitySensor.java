package de.ncoder.sensorsystem.android.sensor;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import de.ncoder.sensorsystem.Key;
import de.ncoder.sensorsystem.android.sensor.base.AndroidSensor;

public class ProximitySensor extends AndroidSensor<Float> {
    public static final Key<ProximitySensor> KEY = new Key<>(ProximitySensor.class);

    public ProximitySensor(SensorManager sensorManager) {
        super(sensorManager, sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY));
    }

    public boolean isCovered() {
        return get() < sensor.getMaximumRange();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        changed(null, event.values[0]);
    }
}
