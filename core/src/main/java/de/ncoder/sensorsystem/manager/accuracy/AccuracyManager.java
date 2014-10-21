package de.ncoder.sensorsystem.manager.accuracy;

import de.ncoder.sensorsystem.AbstractComponent;
import de.ncoder.sensorsystem.events.EventManager;
import de.ncoder.sensorsystem.events.event.SimpleValueChangedEvent;
import de.ncoder.typedmap.Key;

public class AccuracyManager extends AbstractComponent {
    public static final Key<AccuracyManager> KEY = new Key<>(AccuracyManager.class);

    public static final int ACCURACY_MIN = 0;
    public static final int ACCURACY_LOW = 25;
    public static final int ACCURACY_MED = 50;
    public static final int ACCURACY_HIGH = 75;
    public static final int ACCURACY_MAX = 100;

    public static final int DEFAULT_ACCURACY = ACCURACY_MED;

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

    // ------------------------------------------------------------------------

    public class AccuracyChangedEvent extends SimpleValueChangedEvent<Integer, AccuracyManager> {
        //TODO check if serializable

        private AccuracyChangedEvent(int oldValue, int newValue) {
            super(AccuracyChangedEvent.class.getName(), AccuracyManager.this, oldValue, newValue);
        }
    }
}
