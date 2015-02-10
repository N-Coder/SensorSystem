package de.ncoder.sensorsystem.android.sensor.plea;

import java.util.Set;

import de.ncoder.sensorsystem.AbstractComponent;
import de.ncoder.sensorsystem.Component;
import de.ncoder.sensorsystem.Container;
import de.ncoder.sensorsystem.DependantComponent;
import de.ncoder.sensorsystem.android.sensor.LinearAccelerationSensor;
import de.ncoder.sensorsystem.events.EventListener;
import de.ncoder.sensorsystem.events.EventManager;
import de.ncoder.sensorsystem.events.event.Event;
import de.ncoder.sensorsystem.events.event.ValueChangedEvent;
import de.ncoder.sensorsystem.manager.DataManager;
import de.ncoder.typedmap.Key;

public class MovementTracker extends AbstractComponent implements EventListener, DependantComponent {
	public static final Key<MovementTracker> KEY_GLOBAL = new Key<>(MovementTracker.class);

	private float sumX;
	private float sumY;
	private float sumZ;
	private float maxX;
	private float maxY;
	private float maxZ;
	private double maxMSE;
	private int eventCount;

	@Override
	public void init(Container container) {
		super.init(container);
		getOtherComponent(EventManager.KEY).subscribe(this);
	}

	@Override
	public void destroy() {
		getOtherComponent(EventManager.KEY).unsubscribe(this);
		super.destroy();
	}

	public void reset() {
		sumX = 0.0f;
		sumY = 0.0f;
		sumZ = 0.0f;
		maxX = 0.0f;
		maxY = 0.0f;
		maxZ = 0.0f;
		maxMSE = 0.0d;
		eventCount = 0;
	}

	public int getEventCount() {
		return eventCount;
	}

	public float[] getAverage() {
		return new float[]{sumX / eventCount, sumY / eventCount, sumZ / eventCount,};
	}

	/**
	 * @return The maximum value for the x axis that has been recorded by this listener.
	 */
	public float getMaximumX() {
		return maxX;
	}

	/**
	 * @return The maximum value for the y axis that has been recorded by this listener.
	 */
	public float getMaximumY() {
		return maxY;
	}

	/**
	 * @return The maximum value for the z axis that has been recorded by this listener.
	 */
	public float getMaximumZ() {
		return maxZ;
	}

	/**
	 * Get the maximum value of all axis.
	 * If the x axis has had the biggest value, this value is returned.
	 * If the y axis has had the biggest value, then the y value is returned.
	 * Same goes for z.
	 *
	 * @return The maximum value of all axis.
	 */
	public float getMaximumValue() {
		if (maxX >= maxY && maxX >= maxZ) {
			return maxX;
		} else if (maxZ >= maxY && maxZ >= maxX) {
			return maxZ;
		} else if (maxY >= maxX && maxY >= maxZ) {
			return maxY;
		}
		return -1;
	}

	@Override
	public void handle(Event event) {
		if (event instanceof ValueChangedEvent && event.getSource() instanceof LinearAccelerationSensor) {
			eventCount++;
			float[] linearAccel = (float[]) ((ValueChangedEvent) event).getNewValue();

			float accelX = linearAccel[0];
			if (accelX > maxX) {
				maxX = accelX;
			}
			sumX = sumX + accelX;

			float accelY = linearAccel[1];
			if (accelY > maxY) {
				maxY = accelY;
			}
			sumY = sumY + accelY;

			float accelZ = linearAccel[2];
			if (accelZ > maxZ) {
				maxZ = accelZ;
			}
			sumZ = sumZ + accelZ;

			double mse = calcMSE(accelX, accelY, accelZ);
			if (mse > maxMSE) {
				maxMSE = mse;
			}
		}
	}

	/**
	 * Calculates the MSE (mean squared error), comparing the three axis to zero.
	 * See http://en.wikipedia.org/wiki/Mean_squared_error for further information.
	 *
	 * @return the MSE
	 */
	public static double calcMSE(float accelX, float accelY, float accelZ) {
		double powSum = Math.pow(accelX, 2) + Math.pow(accelY, 2) + Math.pow(accelZ, 2);
		return powSum / 3;
	}

	private static Set<Key<? extends Component>> dependencies;

	@Override
	public Set<Key<? extends Component>> dependencies() {
		if (dependencies == null) {
			dependencies = DataManager.<Key<? extends Component>>wrapSet(LinearAccelerationSensor.KEY, EventManager.KEY);
		}
		return dependencies;
	}
}
