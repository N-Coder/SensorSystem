package de.ncoder.sensorsystem.manager.timed;

import de.ncoder.sensorsystem.AbstractComponent;
import de.ncoder.sensorsystem.Key;
import de.ncoder.sensorsystem.manager.AccuracyManager;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public abstract class TimingManager extends AbstractComponent {
    public static final Key<TimingManager> KEY = new Key<>(TimingManager.class);
    public static final FrameLength DEFAULT_FRAME_LENGTH = new FrameLength(15, 60, TimeUnit.SECONDS); //TODO define timing frames

    public TimingManager(FrameLength frameLength) {
        this.frameLength = frameLength;
    }

    // ------------------------------------------------------------------------

    public abstract Future<?> scheduleRepeatedExecution(Runnable r, int delayFrames, int initialDelayFrames);

    public abstract Future<?> scheduleExecution(Runnable r, int delayFrames);

    // --------------------------------ACCURACY--------------------------------

    private FrameLength frameLength;

    public FrameLength getFrameLength() {
        return frameLength;
    }

    public void setFrameLength(FrameLength frameLength) {
        this.frameLength = frameLength;
    }

    public long getCurrentFrameLength() {
        AccuracyManager accuracyManager = getOtherComponent(AccuracyManager.KEY);
        if (accuracyManager != null) {
            return accuracyManager.scale(frameLength.getMaximum(), frameLength.getMinimum());
        } else {
            return frameLength.getMinimum();
        }
    }

    public static class FrameLength {
        private long maximum, minimum; //ms

        public FrameLength(long fixedFrameLength) {
            set(fixedFrameLength);
        }

        public FrameLength(long fixedFrameLength, TimeUnit unit) {
            set(fixedFrameLength, unit);
        }

        public FrameLength(long maximum, long minimum) {
            set(maximum, minimum);
        }

        public FrameLength(long maximum, long minimum, TimeUnit unit) {
            set(maximum, minimum, unit);
        }

        /**
         * most accurate
         */
        public long getMinimum() {
            return minimum;
        }

        /**
         * least accurate
         */
        public long getMaximum() {
            return maximum;
        }

        public void set(long fixedFrameLength) {
            set(fixedFrameLength, fixedFrameLength);
        }

        public void set(long fixedFrameLength, TimeUnit unit) {
            set(fixedFrameLength, fixedFrameLength, unit);
        }

        public void set(long minimum, long maximum, TimeUnit unit) {
            set(unit.toMillis(minimum), unit.toMillis(maximum));
        }

        public void set(long minimum, long maximum) {
            this.minimum = minimum;
            this.maximum = maximum;
        }
    }
}
