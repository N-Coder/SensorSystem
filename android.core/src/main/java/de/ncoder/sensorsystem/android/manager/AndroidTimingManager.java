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

package de.ncoder.sensorsystem.android.manager;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import java.util.PriorityQueue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import de.ncoder.sensorsystem.Container;
import de.ncoder.sensorsystem.android.ContainerService;
import de.ncoder.sensorsystem.manager.TimingManager;
import de.ncoder.sensorsystem.manager.accuracy.AccuracyManager;

/**
 * @deprecated implementation is broken
 */
@Deprecated
public class AndroidTimingManager extends TimingManager {
    private static final String INTENT_ACTION = AndroidTimingManager.class.getName() + ".ALARM";

    private AlarmManager alarmManager;
    private PendingIntent alarmIntent;

    @Override
    public void init(Container container) {
        super.init(container);
        alarmManager = (AlarmManager) (getContext().getSystemService(Context.ALARM_SERVICE));
        alarmIntent = PendingIntent.getBroadcast(getContext(), 0, new Intent(INTENT_ACTION), PendingIntent.FLAG_CANCEL_CURRENT);
        getContext().registerReceiver(broadcastReceiver, new IntentFilter(INTENT_ACTION));
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, getFrameLength().scale(getOtherComponent(AccuracyManager.KEY)), alarmIntent);
    }

    @Override
    public void destroy() {
        getContext().unregisterReceiver(broadcastReceiver);
        alarmManager.cancel(alarmIntent);
        queue.clear();
        super.destroy();
    }

    private Context getContext() {
        return getOtherComponent(ContainerService.KEY_CONTEXT);
    }

    // ------------------------------------------------------------------------

    private final PriorityQueue<Scheduled> queue = new PriorityQueue<>();
    private int frameCount = 0;

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                while (queue.peek() != null && queue.peek().frameNr < frameCount) {
                    Scheduled next = queue.poll();
                    next.runnable.run();
                    next.reschedule();
                }
            } finally {
                frameCount++;
                alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, getFrameLength().scale(getOtherComponent(AccuracyManager.KEY)), alarmIntent);
            }
        }
    };

    @Override
    public Future<?> scheduleRepeatedExecution(Runnable r, int initialDelayFrames, int delayFrames) {
        Scheduled scheduled = new Scheduled(r, true, delayFrames);
        scheduled.schedule(initialDelayFrames);
        return scheduled;
    }

    @Override
    public Future<?> scheduleExecution(Runnable r, int delayFrames) {
        Scheduled scheduled = new Scheduled(r, false, 0);
        scheduled.schedule(delayFrames);
        return scheduled;
    }

    //TODO This should be split up for repeating and one-time alarms
    private class Scheduled implements Comparable<Scheduled>, Future<Void> {
        private final Runnable runnable;
        private final boolean repeat;
        private final int delay;

        private int frameNr;
        private boolean cancelled = false;

        private Scheduled(Runnable runnable, boolean repeat, int delay) {
            if (repeat && delay < 1) {
                throw new IllegalArgumentException("delay < 1");
            }
            this.runnable = runnable;
            this.repeat = repeat;
            this.delay = delay;
        }

        private void reschedule() {
            if (!cancelled && repeat) {
                frameNr += delay;
                queue.add(this);
            }
        }

        private void schedule(int initialDelay) {
            if (!cancelled) {
                frameNr = frameCount + initialDelay;
                queue.add(this);
            }
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            if (!cancelled && !isDone()) {
                queue.remove(this);
                cancelled = true;
                return true;
            }
            return false;
        }

        @Override
        public boolean isCancelled() {
            return cancelled;
        }

        @Override
        public boolean isDone() {
            return !queue.contains(this);
        }

        @Override
        public Void get() throws InterruptedException, ExecutionException {
            if (isCancelled()) {
                throw new CancellationException();
            }
            //TODO block
            return null;
        }

        @Override
        public Void get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            if (isCancelled()) {
                throw new CancellationException();
            }
            //TODO block
            return null;
        }

        @Override
        public int compareTo(Scheduled another) {
            return Integer.signum(frameNr - another.frameNr);
        }
    }
}
