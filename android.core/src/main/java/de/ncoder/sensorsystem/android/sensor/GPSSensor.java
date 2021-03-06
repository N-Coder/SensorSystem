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

package de.ncoder.sensorsystem.android.sensor;

import android.location.*;
import android.os.Bundle;
import android.os.Looper;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;

import de.ncoder.sensorsystem.Component;
import de.ncoder.sensorsystem.Container;
import de.ncoder.sensorsystem.DependantComponent;
import de.ncoder.sensorsystem.PrivilegedComponent;
import de.ncoder.sensorsystem.android.manager.SystemLooper;
import de.ncoder.sensorsystem.manager.DataManager;
import de.ncoder.sensorsystem.manager.TimingManager;
import de.ncoder.sensorsystem.manager.accuracy.AccuracyManager;
import de.ncoder.sensorsystem.manager.accuracy.IntAccuracyRange;
import de.ncoder.sensorsystem.sensor.AbstractSensor;
import de.ncoder.typedmap.Key;

/**
 * @deprecated TimingManager is broken
 */
@Deprecated
public class GPSSensor extends AbstractSensor<Location> implements DependantComponent, PrivilegedComponent {
	@Deprecated
	public static final Key<GPSSensor> KEY = new Key<>(GPSSensor.class);

    private static final int DELAY_FRAMES = 4;
    private static final int MAX_SNRS = 30;

    private final LocationManager locationManager;
    private Looper looper;
    private Future<?> scheduled;

    public GPSSensor(LocationManager locationManager) {
        this.locationManager = locationManager;
    }

    @Override
    public void init(Container container) {
        super.init(container);
        looper = getOtherComponent(SystemLooper.KEY).getLooper();
        locationManager.addGpsStatusListener(gpsStatusListener);
        scheduleGPSUpdate();
    }

    private void scheduleGPSUpdate() {
        scheduled = getOtherComponent(TimingManager.KEY).scheduleExecution(new Runnable() {
            @Override
            public void run() {
                locationManager.requestSingleUpdate(getGpsCriteria(), locationListener, looper);

                // scheduleGPSUpdate() could be moved to locationListener if it can be ensured that it will always
                // be called at some time, even if e.g. the provider was disabled while waiting for the location update
                scheduleGPSUpdate();
            }
        }, DELAY_FRAMES);
    }

    @Override
    public void destroy() {
        super.destroy();
        scheduled.cancel(false);
        locationManager.removeUpdates(locationListener);
        locationManager.removeGpsStatusListener(gpsStatusListener);
    }

    // --------------------------------

    private final Criteria criteria = new Criteria();
    private final IntAccuracyRange<Void> accuracyRange = new IntAccuracyRange<>(Criteria.ACCURACY_LOW, Criteria.ACCURACY_HIGH);

    private Criteria getGpsCriteria() {
        criteria.setAccuracy(accuracyRange.scale(getOtherComponent(AccuracyManager.KEY)));
        //criteria.setPowerRequirement(level);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setSpeedRequired(true);
        criteria.setCostAllowed(false);
        return criteria;
    }

    // ------------------------------------------------------------------------
    private Location location;
    private final float[] distance = new float[1];
    private GpsStatus status = null; //will be created and reused by LocationManager.getGpsStatus
    private final List<Float> satelliteSNRs = new ArrayList<>(MAX_SNRS);

    @Override
    public Location get() {
        return location;
    }

    public float getDistance() {
        return distance[0];
    }

    public GpsStatus getGpsStatus() {
        return status;
    }

    public List<Float> getSatelliteSNRs() {
        return satelliteSNRs;
    }

    private final LocationListener locationListener = new LocationListener() {

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
        }

        @Override
        public void onProviderEnabled(String s) {
        }

        @Override
        public void onProviderDisabled(String s) {
        }

        @Override
        public void onLocationChanged(Location newLocation) {
            if (location != null) {
                Location.distanceBetween(newLocation.getLatitude(), newLocation.getLongitude(),
                        location.getLatitude(), location.getLongitude(), distance);
            }
            changed(location, newLocation);
            location = newLocation;
        }
    };

    private final GpsStatus.Listener gpsStatusListener = new GpsStatus.Listener() {
        @Override
        public void onGpsStatusChanged(int event) {
            if (event == GpsStatus.GPS_EVENT_SATELLITE_STATUS || event == GpsStatus.GPS_EVENT_FIRST_FIX) {
                status = locationManager.getGpsStatus(status);
                // Check number of satellites in list to determine fix state
                satelliteSNRs.clear();
                for (GpsSatellite sat : status.getSatellites()) {
                    //do not count satellites the phone doesn't have a connection to
                    if (sat.getSnr() != 0 || sat.usedInFix()) {
                        satelliteSNRs.add(sat.getSnr());
                    }
                }
            }
        }
    };

	private static Set<Key<? extends Component>> dependencies;

	@Override
	public Set<Key<? extends Component>> dependencies() {
		if (dependencies == null) {
			dependencies = DataManager.wrapSet(TimingManager.KEY, SystemLooper.KEY);
		}
		return dependencies;
	}

	private static Set<String> permissions;

	@Override
	public Set<String> requiredPermissions() {
		if (permissions == null) {
			permissions = DataManager.wrapSet("android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION");
		}
		return permissions;
	}
}
