package de.ncoder.sensorsystem.android.sensor;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import de.ncoder.sensorsystem.Key;
import de.ncoder.sensorsystem.android.sensor.base.BufferedAndroidSensor;

import java.util.Arrays;

public class AccelerationSensor extends BufferedAndroidSensor<float[]> {
    public static final Key<AccelerationSensor> KEY = new Key<>(AccelerationSensor.class);

    public AccelerationSensor(SensorManager sensorManager) {
        super(sensorManager,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                new float[30][]);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        changed(null, Arrays.copyOf(event.values, 3));
    }
}
