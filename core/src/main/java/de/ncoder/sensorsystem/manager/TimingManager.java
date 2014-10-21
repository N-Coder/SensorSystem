package de.ncoder.sensorsystem.manager;

import de.ncoder.sensorsystem.AbstractComponent;
import de.ncoder.sensorsystem.manager.accuracy.LongAccuracyRange;
import de.ncoder.typedmap.Key;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public abstract class TimingManager extends AbstractComponent {
    public static final Key<TimingManager> KEY = new Key<>(TimingManager.class);

    // ------------------------------------------------------------------------

    public abstract Future<?> scheduleRepeatedExecution(Runnable r, int delayFrames, int initialDelayFrames);

    public abstract Future<?> scheduleExecution(Runnable r, int delayFrames);

    // --------------------------------ACCURACY--------------------------------

    private final LongAccuracyRange<TimeUnit> frameLength = new LongAccuracyRange<>(
            15L, 60L, TimeUnit.SECONDS
    );

    public LongAccuracyRange<TimeUnit> getFrameLength() {
        return frameLength;
    }
}
