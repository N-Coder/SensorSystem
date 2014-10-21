package de.ncoder.sensorsystem.manager;

import de.ncoder.sensorsystem.AbstractComponent;
import de.ncoder.sensorsystem.manager.accuracy.BooleanAccuracyRange;
import de.ncoder.sensorsystem.manager.accuracy.LongAccuracyRange;
import de.ncoder.typedmap.Key;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public abstract class ScheduleManager extends AbstractComponent {
    public static final Key<ScheduleManager> KEY = new Key<>(ScheduleManager.class);

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

    private final LongAccuracyRange<TimeUnit> latency = new LongAccuracyRange<>(
            10L, 0L, TimeUnit.SECONDS
    );

    public LongAccuracyRange<TimeUnit> getExecutionLatency() {
        return latency;
    }

    private final BooleanAccuracyRange<Void> wakeupTreshold = new BooleanAccuracyRange<>(
            10, false
    );

    public BooleanAccuracyRange<Void> getWakeupTreshold() {
        return wakeupTreshold;
    }
}

