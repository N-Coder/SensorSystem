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

