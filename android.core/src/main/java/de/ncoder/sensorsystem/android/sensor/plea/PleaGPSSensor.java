package de.ncoder.sensorsystem.android.sensor.plea;

import android.content.Context;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;

import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import de.ncoder.sensorsystem.Component;
import de.ncoder.sensorsystem.Container;
import de.ncoder.sensorsystem.DependantComponent;
import de.ncoder.sensorsystem.Utils;
import de.ncoder.sensorsystem.android.ContainerService;
import de.ncoder.sensorsystem.android.manager.SystemLooper;
import de.ncoder.sensorsystem.manager.ScheduleManager;
import de.ncoder.sensorsystem.sensor.AbstractSensor;
import de.ncoder.typedmap.Key;

public class PleaGPSSensor extends AbstractSensor<PleaGPSData> implements DependantComponent {
	public static final Key<PleaGPSSensor> KEY = new Key<>(PleaGPSSensor.class);
	public static final Key<MovementTracker> KEY_TRACKER = new Key<>(MovementTracker.class);
	private String TAG = this.getClass().getSimpleName();

	private static final int GPS_SEARCHING_TIME = 30; //seconds
	private static final int GPS_INTERRUPTION_TIME = 30; //seconds
	private static final int MOVEMENT_TRACKING_TIME = 30; //seconds
	private static final int MOVEMENT_INTERRUPTION_TIME = 0; //seconds
	private static final int THRESHOLD_MOVEMENT_SAMPLES = 10;
	private static final float THRESHOLD_MOVEMENT_MAX_VALUE = 4.0f;
	private static final double THRESHOLD_MOVEMENT_AVG_MSE = 0.001d;

	private int movementCounter = 0;
	private STATE state;

	private PleaGPSData lastGpsData;
	private Location cachedLocation = null; //will be created and reused by LocationListener
	private final float[] cachedDistance = new float[1];
	private GpsStatus cachedGpsStatus = null; //will be created and reused by GPSStatusListener

	@Override
	public void init(@Nonnull Container container, @Nonnull Key<? extends Component> key) {
		super.init(container, key);
		getLocationManager().addGpsStatusListener(gpsStatusListener);
		container.register(KEY_TRACKER, new MovementTracker());
		startGpsLoop();
	}

	@Override
	public void destroy(Key<? extends Component> key) {
		getContainer().unregister(KEY_TRACKER);
		getLocationManager().removeUpdates(simpleLocationListener);
		getLocationManager().removeGpsStatusListener(gpsStatusListener);
		super.destroy(key);
	}

	@Override
	public PleaGPSData get() {
		return lastGpsData;
	}

	private Context getContext() {
		return getOtherComponent(ContainerService.KEY_CONTEXT);
	}

	private LocationManager getLocationManager() {
		return (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
	}

	private Looper getLooper() {
		return getOtherComponent(SystemLooper.KEY).getLooper();
	}

	private Future<?> scheduleExecution(Runnable r, long delay, TimeUnit unit) {
		return getOtherComponent(ScheduleManager.KEY).scheduleExecution(r, delay, unit);
	}

	// ------------------------------------------------------------------------

	private enum STATE {
		GPS_SEARCHING, GPS_FETCH_SUCCESSFUL, GPS_FETCH_FAILED, WAITING_FOR_MOVEMENT, MUCH_MOVEMENT, LITTLE_MOVEMENT, DISABLED
	}

	private void decideNextStep() {
		Log.v(TAG, "DecideNextStep, state = " + state);
		switch (state) {
			case GPS_FETCH_FAILED:
				//track movement for MOVEMENT_TRACKING_TIME (30) seconds and start instantly
				checkSignificantMovement(0, TimeUnit.SECONDS, MOVEMENT_TRACKING_TIME, TimeUnit.SECONDS);
				break;
			case GPS_FETCH_SUCCESSFUL:
				Log.v(TAG, "Starting to search for GPs in " + GPS_INTERRUPTION_TIME + " secs.");
				//search for GPS for GPS_SEARCHING_TIME (30) seconds and start after GPS_INTERRUPTION_TIME (30) seconds
				searchForGPS(GPS_INTERRUPTION_TIME, TimeUnit.SECONDS, GPS_SEARCHING_TIME, TimeUnit.SECONDS);
				break;
			case LITTLE_MOVEMENT:
				movementCounter++;
				Log.v(TAG, "MovementCounter: " + movementCounter);
				if (movementCounter < THRESHOLD_MOVEMENT_SAMPLES) {
					//track movement for MOVEMENT_TRACKING_TIME (30) seconds and start after MOVEMENT_INTERRUPTION_TIME (0) seconds
					checkSignificantMovement(MOVEMENT_INTERRUPTION_TIME, TimeUnit.SECONDS, MOVEMENT_TRACKING_TIME, TimeUnit.SECONDS);
				} else {
					startGpsLoop();
					movementCounter = 0;
				}
				break;
			case MUCH_MOVEMENT:
				movementCounter = 0;
				startGpsLoop();
				break;
			default:
				Log.w(TAG, "Illegal state " + state + " for next step. PleaGPSSensor probably crashed.");
				break;
		}
	}

	private void startGpsLoop() {
		//search for GPS for GPS_SEARCHING_TIME (30) seconds and start instantly
		searchForGPS(0, TimeUnit.SECONDS, GPS_SEARCHING_TIME, TimeUnit.SECONDS);
	}

	/**
	 * Tries to get a location fix for the given amount of time. After waiting for the specified time, the searching will be interrupted.
	 *
	 * @param searchTime   the time to keep looking for a gps fix.
	 * @param searchUnit   the time unit of searchTime.
	 * @param delayFromNow the time to wait until the search will start
	 * @param delayUnit    the time unit of delayFromNow
	 */
	private void searchForGPS(long delayFromNow, TimeUnit delayUnit, final long searchTime, final TimeUnit searchUnit) {
		scheduleExecution(new Runnable() {
			@Override
			public void run() {
				//start searching
				Log.d(TAG, "Starting to search for GPS");
				state = STATE.GPS_SEARCHING;
				getLocationManager().requestSingleUpdate(LocationManager.GPS_PROVIDER, simpleLocationListener, getLooper());

				scheduleExecution(new Runnable() {
					@Override
					public void run() {
						if (state == STATE.GPS_SEARCHING) {
							//stop searching after the given amount of time
							Log.d(TAG, "Stopping to search for GPS, search failed.");
							getLocationManager().removeUpdates(simpleLocationListener);
							state = STATE.GPS_FETCH_FAILED;
							decideNextStep();
						}
					}
				}, searchTime, searchUnit);
			}
		}, delayFromNow, delayUnit);
	}

	private void checkSignificantMovement(long delayFromNow, TimeUnit delayUnit, final long trackingTime, final TimeUnit trackingUnit) {
		scheduleExecution(new Runnable() {
			@Override
			public void run() {
				//start tracking acceleration sensor
				Log.d(TAG, "Starting to track acceleration.");
				getOtherComponent(KEY_TRACKER).reset();
				scheduleExecution(new Runnable() {
					@Override
					public void run() {
						//stop tracking acceleration sensor
						Log.d(TAG, "Stopping to track acceleration.");
						MovementTracker tracker = getOtherComponent(KEY_TRACKER);
						Log.v(TAG, "Number of acceleration events: " + tracker.getEventCount());
						float[] average = tracker.getAverage();
						double averageMSE = MovementTracker.calcMSE(average[0], average[1], average[2]);
						Log.v(TAG, "Average MSE: " + averageMSE);
						float maximumValue = tracker.getMaximumValue();
						Log.v(TAG, "Maximum value: " + maximumValue);

						//TODO decide if little or much movement (tune values, check if other significant features)
						if (maximumValue > THRESHOLD_MOVEMENT_MAX_VALUE || averageMSE > THRESHOLD_MOVEMENT_AVG_MSE) {
							state = STATE.MUCH_MOVEMENT;
							Log.v(TAG, "state --> MUCH_MOVEMENT");
						} else {
							state = STATE.LITTLE_MOVEMENT;
							Log.v(TAG, "state --> LITTLE_MOVEMENT");
						}
						decideNextStep();
					}
				}, trackingTime, trackingUnit);
			}
		}, delayFromNow, delayUnit);
	}

	private final LocationListener simpleLocationListener = new LocationListener() {
		@Override
		public void onLocationChanged(Location newLocation) {
			if (lastGpsData != null) {
				Location.distanceBetween(newLocation.getLatitude(),
						newLocation.getLongitude(),
						lastGpsData.getLocationLatitude(),
						lastGpsData.getLocationLongitude(),
						cachedDistance);
			}
			cachedLocation = newLocation;
			PleaGPSData newGpsData = new PleaGPSData(cachedLocation, cachedGpsStatus, false, cachedDistance[0]);
			changed(lastGpsData, newGpsData);
			lastGpsData = newGpsData;

			Log.d(TAG, "Stopping to search for GPS, search successful.");
			state = STATE.GPS_FETCH_SUCCESSFUL;
			decideNextStep();
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
		}

		@Override
		public void onProviderEnabled(String provider) {
		}

		@Override
		public void onProviderDisabled(String provider) {
		}
	};

	private final GpsStatus.Listener gpsStatusListener = new GpsStatus.Listener() {
		@Override
		public void onGpsStatusChanged(int event) {
			if (event == GpsStatus.GPS_EVENT_SATELLITE_STATUS || event == GpsStatus.GPS_EVENT_FIRST_FIX) {
				cachedGpsStatus = getLocationManager().getGpsStatus(cachedGpsStatus);
				PleaGPSData newGpsData = new PleaGPSData(cachedLocation, cachedGpsStatus, true, cachedDistance[0]);
				changed(lastGpsData, newGpsData);
				lastGpsData = newGpsData;
			}
		}
	};

	private static Set<Key<? extends Component>> dependencies;

	@Nonnull
	@Override
	public Set<Key<? extends Component>> dependencies() {
		if (dependencies == null) {
			dependencies = Utils.wrapSet(
					ScheduleManager.KEY, SystemLooper.KEY, ContainerService.KEY_CONTEXT
			);
		}
		return dependencies;
	}
}
