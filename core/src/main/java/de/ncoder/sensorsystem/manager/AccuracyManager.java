package de.ncoder.sensorsystem.manager;

import de.ncoder.sensorsystem.AbstractComponent;
import de.ncoder.sensorsystem.Container;
import de.ncoder.sensorsystem.manager.event.EventManager;
import de.ncoder.sensorsystem.manager.event.SimpleValueChangedEvent;

public class AccuracyManager extends AbstractComponent {
    public static final Container.Key<AccuracyManager> KEY = new Container.Key<AccuracyManager>(AccuracyManager.class);

    public static final int ACCURACY_MIN = 0;
    public static final int ACCURACY_LOW = 25;
    public static final int ACCURACY_MED = 50;
    public static final int ACCURACY_HIGH = 75;
    public static final int ACCURACY_MAX = 100;

    private int accuracy;

    public AccuracyManager() {
        setAccuracy(ACCURACY_MED);
    }

    public AccuracyManager(int accuracy) {
        setAccuracy(accuracy);
    }

    public void setAccuracy(int accuracy) {
        if (accuracy < ACCURACY_MIN || accuracy > ACCURACY_MAX) {
            throw new IllegalArgumentException("Accuracy " + accuracy + " is out of bounds");
        }
        AccuracyChangedEvent event = new AccuracyChangedEvent(this.accuracy, accuracy);

        this.accuracy = accuracy;

        EventManager eventManager = getOtherComponent(EventManager.KEY);
        if (eventManager != null) {
            eventManager.publish(event);
        }
    }

    public int getAccuracy() {
        return accuracy;
    }

    public int scale(int leastAccurate, int mostAccurate) {
        return Math.round((mostAccurate - leastAccurate) * (accuracy / (float) ACCURACY_MAX)) + leastAccurate;
    }

    public long scale(long leastAccurate, long mostAccurate) {
        return Math.round((mostAccurate - leastAccurate) * (accuracy / (double) ACCURACY_MAX)) + leastAccurate;
    }

    public float scale(float leastAccurate, float mostAccurate) {
        return (mostAccurate - leastAccurate) * (accuracy / (float) ACCURACY_MAX) + leastAccurate;
    }

    public double scale(double leastAccurate, double mostAccurate) {
        return (mostAccurate - leastAccurate) * (accuracy / (double) ACCURACY_MAX) + leastAccurate;
    }

    // ------------------------------------------------------------------------

    public class AccuracyChangedEvent extends SimpleValueChangedEvent<Integer, AccuracyManager> {
        private AccuracyChangedEvent(int oldValue, int newValue) {
            super(AccuracyChangedEvent.class.getName(), AccuracyManager.this, oldValue, newValue);
        }
    }
}
