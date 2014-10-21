package de.ncoder.sensorsystem.android.manager;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import de.ncoder.sensorsystem.Container;
import de.ncoder.sensorsystem.manager.TimingManager;

import java.util.PriorityQueue;
import java.util.concurrent.*;

public class AndroidTimingManager extends TimingManager {
    private static final String INTENT_ACTION = AndroidTimingManager.class + ".ALARM";

    private final Context context;
    private final AlarmManager alarmManager;
    private final PendingIntent alarmIntent;


    public AndroidTimingManager(Context context, FrameLength frameLength) {
        super(frameLength);
        this.context = context;
        alarmManager = (AlarmManager) (context.getSystemService(Context.ALARM_SERVICE));
        alarmIntent = PendingIntent.getBroadcast(context, 0, new Intent(INTENT_ACTION), PendingIntent.FLAG_CANCEL_CURRENT);
    }

    @Override
    public void init(Container container) {
        super.init(container);
        context.registerReceiver(broadcastReceiver, new IntentFilter(INTENT_ACTION));
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, getCurrentFrameLength(), alarmIntent);
    }

    @Override
    public void destroy() {
        context.unregisterReceiver(broadcastReceiver);
        alarmManager.cancel(alarmIntent);
        queue.clear();
        super.destroy();
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
                alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, getCurrentFrameLength(), alarmIntent);
            }
        }
    };

    @Override
    public Future<?> scheduleRepeatedExecution(Runnable r, int delayFrames, int initialDelayFrames) {
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
            if (!cancelled) {
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
            return queue.contains(this);
        }

        @Override
        public Void get() throws InterruptedException, ExecutionException {
            if (isCancelled()) {
                throw new CancellationException();
            }
            return null;
        }

        @Override
        public Void get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            if (isCancelled()) {
                throw new CancellationException();
            }
            return null;
        }

        @Override
        public int compareTo(Scheduled another) {
            return Integer.signum(frameNr - another.frameNr);
        }
    }
}