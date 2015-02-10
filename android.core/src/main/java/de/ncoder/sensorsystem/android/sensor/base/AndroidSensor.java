/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Niko Fink
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package de.ncoder.sensorsystem.android.sensor.base;

import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import java.util.concurrent.TimeUnit;

import de.ncoder.sensorsystem.Component;
import de.ncoder.sensorsystem.Container;
import de.ncoder.sensorsystem.android.manager.SystemLooper;
import de.ncoder.sensorsystem.events.EventListener;
import de.ncoder.sensorsystem.events.EventManager;
import de.ncoder.sensorsystem.events.event.Event;
import de.ncoder.sensorsystem.manager.accuracy.AccuracyManager;
import de.ncoder.sensorsystem.manager.accuracy.LongAccuracyRange;
import de.ncoder.sensorsystem.sensor.AbstractSensor;
import de.ncoder.typedmap.Key;

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
	public void init(Container container, Key<? extends Component> key) {
		super.init(container, key);
		updateSensorListener();
		EventManager eventManager = getOtherComponent(EventManager.KEY);
		if (eventManager != null) {
			eventManager.subscribe(accuracyListener);
		}
	}

	@Override
	public void destroy(Key<? extends Component> key) {
		EventManager eventManager = getOtherComponent(EventManager.KEY);
		if (eventManager != null) {
			eventManager.unsubscribe(accuracyListener);
		}
		sensorManager.unregisterListener(this, sensor);
		super.destroy(key);
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
	protected void changed(boolean hasOldValue, T oldValue, T newValue) {
		if (!mayChange()) return;
		if (oldValue == null || !hasOldValue) {
			oldValue = cachedValue;
		}
		cachedValue = newValue;
		super.changed(hasOldValue, oldValue, newValue);
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
