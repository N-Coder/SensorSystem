package de.ncoder.sensorsystem.android.sensor.plea;

import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;

import java.util.ArrayList;
import java.util.List;

public class PleaGPSData {
	private final Location location;
	private final GpsStatus gpsStatus;
	private final boolean gpsStatusChanged;
	private final float distanceToLastLocation;

	public PleaGPSData(Location location, GpsStatus gpsStatus, boolean gpsStatusChanged, float distanceToLastLocation) {
		this.location = location;
		this.gpsStatus = gpsStatus;
		this.gpsStatusChanged = gpsStatusChanged;
		this.distanceToLastLocation = distanceToLastLocation;
	}

	public Location getLocation() {
		return location;
	}

	public GpsStatus getGpsStatus() {
		return gpsStatus;
	}

	public float getDistanceToLastLocation() {
		return distanceToLastLocation;
	}

	public boolean isLocationChanged() {
		return !isGpsStatusChanged();
	}

	public boolean isGpsStatusChanged() {
		return gpsStatusChanged;
	}

	public float getLocationAccuracy() {
		if (location != null) {
			return location.getAccuracy();
		}
		return -1;
	}

	public double getLocationLongitude() {
		if (location != null) {
			return location.getLongitude();
		}
		return -1;
	}

	public double getLocationLatitude() {
		if (location != null) {
			return location.getLatitude();
		}
		return -1;
	}

	public long getLocationTime() {
		if (location != null) {
			return location.getTime();
		}
		return -1;
	}

	public List<GpsSatellite> getSatellites() {
		if (gpsStatus != null) {
			final Iterable<GpsSatellite> satellites = gpsStatus.getSatellites();
			ArrayList<GpsSatellite> satList = new ArrayList<>();
			for (GpsSatellite curSat : satellites) {
				satList.add(curSat);
			}
			return satList;
		}
		return null;
	}

	public List<GpsSatellite> getSatellitesUsedInFix() {
		if (gpsStatus != null) {
			Iterable<GpsSatellite> allSats = gpsStatus.getSatellites();
			ArrayList<GpsSatellite> usedSatList = new ArrayList<>();
			for (GpsSatellite curSat : allSats) {
				//do not count satellites the phone doesn't have a connection to
				if (curSat.getSnr() != 0 || curSat.usedInFix()) {
					usedSatList.add(curSat);
				}
			}
			return usedSatList;
		}
		return null;
	}

	public double getAverageSnr() {
		List<GpsSatellite> satellitesUsedInFix = getSatellitesUsedInFix();
		double totalSnr = 0;
		for (GpsSatellite curSat : satellitesUsedInFix) {
			totalSnr += curSat.getSnr();
		}
		return totalSnr / satellitesUsedInFix.size();
	}

	public double getMinimumSnr() {
		List<GpsSatellite> satellitesUsedInFix = getSatellitesUsedInFix();
		if (satellitesUsedInFix.size() > 0) {
			double minSnr = satellitesUsedInFix.get(0).getSnr();
			for (GpsSatellite curSat : satellitesUsedInFix) {
				if (curSat.getSnr() < minSnr) {
					minSnr = curSat.getSnr();
				}
			}
			return minSnr;
		} else {
			return -1;
		}
	}

	public double getMaximumSnr() {
		List<GpsSatellite> satellitesUsedInFix = getSatellitesUsedInFix();
		if (satellitesUsedInFix.size() > 0) {
			double maxSnr = satellitesUsedInFix.get(0).getSnr();
			for (GpsSatellite curSat : satellitesUsedInFix) {
				if (curSat.getSnr() > maxSnr) {
					maxSnr = curSat.getSnr();
				}
			}
			return maxSnr;
		} else {
			return -1;
		}
	}
}
