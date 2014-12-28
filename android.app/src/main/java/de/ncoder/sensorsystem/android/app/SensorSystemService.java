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

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.os.IBinder;
import android.util.Log;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.Collection;

import de.ncoder.sensorsystem.Component;
import de.ncoder.sensorsystem.Container;
import de.ncoder.sensorsystem.SimpleContainer;
import de.ncoder.sensorsystem.android.app.componentInfo.ComponentInfoManager;
import de.ncoder.sensorsystem.android.app.data.UserManager;
import de.ncoder.sensorsystem.android.logging.DBLogger;
import de.ncoder.sensorsystem.android.manager.AndroidScheduleManager;
import de.ncoder.sensorsystem.android.manager.AndroidThreadPoolManager;
import de.ncoder.sensorsystem.android.manager.AndroidTimingManager;
import de.ncoder.sensorsystem.android.manager.SystemLooper;
import de.ncoder.sensorsystem.android.sensor.AccelerationSensor;
import de.ncoder.sensorsystem.android.sensor.BrightnessSensor;
import de.ncoder.sensorsystem.android.sensor.GPSSensor;
import de.ncoder.sensorsystem.android.sensor.MagneticSensor;
import de.ncoder.sensorsystem.android.sensor.ProximitySensor;
import de.ncoder.sensorsystem.events.EventManager;
import de.ncoder.sensorsystem.manager.ScheduleManager;
import de.ncoder.sensorsystem.manager.ThreadPoolManager;
import de.ncoder.sensorsystem.manager.TimingManager;
import de.ncoder.sensorsystem.manager.accuracy.AccuracyManager;
import de.ncoder.typedmap.Key;
import de.ncoder.typedmap.TypedMap;

public class SensorSystemService extends Service {
    private final Container container = new SimpleContainer();

    public SensorSystemService() {
    }

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
        container.register(EventManager.KEY,
                new EventManager());
        container.register(AccuracyManager.KEY,
                new AccuracyManager());
        container.register(ThreadPoolManager.KEY,
                new AndroidThreadPoolManager(AndroidThreadPoolManager.DEFAULT_EXECUTOR(), this));
        container.register(ScheduleManager.KEY,
                new AndroidScheduleManager(this));
        container.register(TimingManager.KEY,
                new AndroidTimingManager(this));
        container.register(SystemLooper.KEY,
                new SystemLooper());
        container.register(DBLogger.KEY,
                new DBLogger(this));
        container.register(UserManager.KEY,
                new UserManager());

        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        container.register(AccelerationSensor.KEY,
                new AccelerationSensor(sensorManager));
        container.register(BrightnessSensor.KEY,
                new BrightnessSensor(sensorManager));
        container.register(MagneticSensor.KEY,
                new MagneticSensor(sensorManager));
        container.register(ProximitySensor.KEY,
                new ProximitySensor(sensorManager));
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        container.register(GPSSensor.KEY,
                new GPSSensor(locationManager));
    }

    @Override
    public IBinder onBind(Intent intent) {
        return theBinder;
    }

    private final Binder theBinder = new Binder();

    public class Binder extends android.os.Binder implements Container {
        public <T extends Component, V extends T> void register(Key<T> key, V component) {
            container.register(key, component);
        }

        public <T extends Component> T get(Key<T> key) {
            return container.get(key);
        }

        public void unregister(Key<?> key) {
            container.unregister(key);
        }

        @Override
        public void unregister(Component component) {
            container.unregister(component);
        }

        public boolean isRegistered(Key<?> key) {
            return container.isRegistered(key);
        }

        public void shutdown() {
            container.shutdown();
            stopSelf();
        }

        public TypedMap<? extends Component> getData() {
            return container.getData();
        }

        @Override
        public Collection<Key<? extends Component>> getKeys() {
            return container.getKeys();
        }
    }

    @Override
    public void onDestroy() {
        container.shutdown();
        super.onDestroy();
    }
}
