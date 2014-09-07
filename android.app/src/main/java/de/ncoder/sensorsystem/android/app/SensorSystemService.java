package de.ncoder.sensorsystem.android.app;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.os.IBinder;
import android.util.Log;
import de.ncoder.sensorsystem.Component;
import de.ncoder.sensorsystem.Container;
import de.ncoder.sensorsystem.Key;
import de.ncoder.sensorsystem.SimpleContainer;
import de.ncoder.sensorsystem.android.app.componentInfo.ComponentInfoManager;
import de.ncoder.sensorsystem.android.app.data.UserManager;
import de.ncoder.sensorsystem.android.logging.DBLogger;
import de.ncoder.sensorsystem.android.manager.SystemLooper;
import de.ncoder.sensorsystem.android.manager.timed.AndroidScheduleManager;
import de.ncoder.sensorsystem.android.manager.timed.AndroidThreadPoolManager;
import de.ncoder.sensorsystem.android.manager.timed.AndroidTimingManager;
import de.ncoder.sensorsystem.android.sensor.*;
import de.ncoder.sensorsystem.manager.AccuracyManager;
import de.ncoder.sensorsystem.manager.event.EventManager;
import de.ncoder.sensorsystem.manager.timed.ScheduleManager;
import de.ncoder.sensorsystem.manager.timed.ThreadPoolManager;
import de.ncoder.sensorsystem.manager.timed.TimingManager;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

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
                new AndroidTimingManager(this, TimingManager.DEFAULT_FRAME_LENGTH));
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
        public <T extends Component> void register(Key<T> key, T actor) {
            container.register(key, actor);
        }

        public <T extends Component> T get(Key<T> key) {
            return container.get(key);
        }

        public void unregister(Key<? extends Component> key) {
            container.unregister(key);
        }

        public boolean isRegistered(Key<? extends Component> key) {
            return container.isRegistered(key);
        }

        public void shutdown() {
            container.shutdown();
        }

        public Set<Map.Entry<Key<? extends Component>, Component>> entrySet() {
            return container.entrySet();
        }
    }

    @Override
    public void onDestroy() {
        container.shutdown();
        super.onDestroy();
    }
}
