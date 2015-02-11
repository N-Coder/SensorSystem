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

package de.ncoder.sensorsystem.android.app.componentInfo;

import android.content.ComponentName;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

import de.ncoder.sensorsystem.Component;
import de.ncoder.sensorsystem.Container;
import de.ncoder.sensorsystem.android.app.R;
import de.ncoder.sensorsystem.android.sensor.base.AndroidSensor;
import de.ncoder.sensorsystem.events.EventListener;
import de.ncoder.sensorsystem.events.EventManager;
import de.ncoder.sensorsystem.events.EventUtils;
import de.ncoder.sensorsystem.events.event.Event;
import de.ncoder.sensorsystem.events.event.ValueChangedEvent;
import de.ncoder.sensorsystem.manager.accuracy.AccuracyManager;
import de.ncoder.typedmap.Key;

public class AndroidSensorInfoActivity extends ComponentInfoActivity implements EventListener {
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
			Key key = Key.forName(clazz, identifier);
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
			Container container = getContainer();
			if (sensor != null && container != null) {
				AccuracyManager accuracyManager = container.get(AccuracyManager.KEY);
				((TextView) findViewById(R.id.sensor_accuracy)).setText(formatDuration(
						sensor.getAccuracy().scale(accuracyManager),
						sensor.getAccuracy().getAdditional()
				));
				((TextView) findViewById(R.id.sensor_batch)).setText(formatDuration(
						sensor.getBatchReportLatency().scale(accuracyManager),
						sensor.getBatchReportLatency().getAdditional()
				));
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
