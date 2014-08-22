package de.ncoder.sensorsystem.android.sensor;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import de.ncoder.sensorsystem.Container;
import de.ncoder.sensorsystem.android.sensor.base.BufferedAndroidSensor;

import java.util.Arrays;

public class MagneticSensor extends BufferedAndroidSensor<float[]> {
    public static final Container.Key<MagneticSensor> KEY = new Container.Key<>(MagneticSensor.class);

    public MagneticSensor(SensorManager sensorManager) {
        super(sensorManager,
                sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
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
