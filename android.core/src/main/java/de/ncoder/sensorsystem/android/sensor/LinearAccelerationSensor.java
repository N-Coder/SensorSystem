package de.ncoder.sensorsystem.android.sensor;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;

import java.util.Arrays;

import de.ncoder.sensorsystem.android.sensor.base.BufferedAndroidSensor;
import de.ncoder.typedmap.Key;

public class LinearAccelerationSensor extends BufferedAndroidSensor<float[]> {
	public static final Key<LinearAccelerationSensor> KEY = new Key<>(LinearAccelerationSensor.class);

	public LinearAccelerationSensor(SensorManager sensorManager) {
		super(sensorManager, sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), new float[30][]);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int i) {
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		changed(null, Arrays.copyOf(event.values, 3));
	}
}
