package de.ncoder.sensorsystem.manager.timed;

import de.ncoder.sensorsystem.AbstractComponent;
import de.ncoder.sensorsystem.Key;
import de.ncoder.sensorsystem.manager.AccuracyManager;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public abstract class ScheduleManager extends AbstractComponent {
    public static final Key<ScheduleManager> KEY = new Key<>(ScheduleManager.class);

    public ScheduleManager() {
        this(0);
    }

    public ScheduleManager(long maximumInaccuracy) {
        this.maximumInaccuracy = maximumInaccuracy;
    }

    // ------------------------------------------------------------------------

    public abstract Future<?> scheduleRepeatedExecution(Runnable r, long delayMillis, long initialDelayMillis);

    public abstract Future<?> scheduleExecution(Runnable r, long delayMillis);

    public Future<?> scheduleRepeatedExecution(Runnable r, long delay, long initialDelay, TimeUnit unit) {
        return scheduleRepeatedExecution(r, unit.toMillis(delay), unit.toMillis(initialDelay));
    }

    public Future<?> scheduleExecution(Runnable r, long delay, TimeUnit unit) {
        return scheduleExecution(r, unit.toMillis(delay));
    }

    // --------------------------------ACCURACY--------------------------------

    private static final long minimumInaccuracy = 0;
    private long maximumInaccuracy; // ms

    public void setMaximumInaccuracy(long maximumInaccuracy) {
        this.maximumInaccuracy = maximumInaccuracy;
    }

    public void setMaximumInaccuracy(long maximumInaccuracy, TimeUnit unit) {
        setMaximumInaccuracy(unit.toMillis(maximumInaccuracy));
    }

    public long getMaximumInaccuracy() {
        return maximumInaccuracy;
    }

    public long getCurrentInaccuracy() {
        AccuracyManager accuracyManager = getOtherComponent(AccuracyManager.KEY);
        if (accuracyManager != null) {
            return accuracyManager.scale(getMaximumInaccuracy(), minimumInaccuracy);
        } else {
            return minimumInaccuracy;
        }
    }
}

