package de.ncoder.sensorsystem.android.sensor;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import de.ncoder.sensorsystem.Container;
import de.ncoder.sensorsystem.android.sensor.base.AndroidSensor;

public class BrightnessSensor extends AndroidSensor<Float> {
    public static final Container.Key<BrightnessSensor> KEY = new Container.Key<>(BrightnessSensor.class);

    public BrightnessSensor(SensorManager sensorManager) {
        super(sensorManager, sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        changed(null, event.values[0]);
    }
}
