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

package de.ncoder.sensorsystem.android.app;

import android.content.Context;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.util.Log;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

import de.ncoder.sensorsystem.android.ContainerService;
import de.ncoder.sensorsystem.android.app.componentInfo.ComponentInfoManager;
import de.ncoder.sensorsystem.android.app.data.UserManager;
import de.ncoder.sensorsystem.android.logging.DBLogger;
import de.ncoder.sensorsystem.android.manager.AndroidScheduleManager;
import de.ncoder.sensorsystem.android.manager.AndroidThreadPoolManager;
import de.ncoder.sensorsystem.android.manager.SystemLooper;
import de.ncoder.sensorsystem.android.sensor.AccelerationSensor;
import de.ncoder.sensorsystem.android.sensor.BrightnessSensor;
import de.ncoder.sensorsystem.android.sensor.MagneticSensor;
import de.ncoder.sensorsystem.android.sensor.ProximitySensor;
import de.ncoder.sensorsystem.events.EventManager;
import de.ncoder.sensorsystem.manager.ScheduleManager;
import de.ncoder.sensorsystem.manager.ThreadPoolManager;
import de.ncoder.sensorsystem.manager.accuracy.AccuracyManager;

public class SensorSystemService extends ContainerService {
	@Override
	public void onCreate() {
		super.onCreate();
		addComponents();
		try {
			ComponentInfoManager.parseResources(getResources().getXml(R.xml.info_activities));
		} catch (XmlPullParserException | IOException e) {
			Log.w(ComponentInfoManager.class.getSimpleName(), "Can't parse resources, detail views won't be available", e);
		}
	}

	private void addComponents() {
		register(EventManager.KEY, new EventManager());
		register(AccuracyManager.KEY, new AccuracyManager());
		register(ThreadPoolManager.KEY, new AndroidThreadPoolManager());
		register(ScheduleManager.KEY, new AndroidScheduleManager());
		//register(TimingManager.KEY, new AndroidTimingManager());
		register(SystemLooper.KEY, new SystemLooper());
		register(DBLogger.KEY, new DBLogger());
		register(UserManager.KEY, new UserManager());

		SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		register(AccelerationSensor.KEY, new AccelerationSensor(sensorManager));
		register(BrightnessSensor.KEY, new BrightnessSensor(sensorManager));
		register(MagneticSensor.KEY, new MagneticSensor(sensorManager));
		register(ProximitySensor.KEY, new ProximitySensor(sensorManager));
		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		//register(GPSSensor.KEY, new GPSSensor(locationManager));
	}
}
