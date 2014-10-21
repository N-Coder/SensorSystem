package de.ncoder.sensorsystem.android.manager;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import de.ncoder.sensorsystem.Container;
import de.ncoder.sensorsystem.manager.ScheduleManager;
import de.ncoder.sensorsystem.manager.accuracy.AccuracyManager;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class AndroidScheduleManager extends ScheduleManager {
    private static final String INTENT_ACTION = AndroidScheduleManager.class + ".ALARM";
    private static final String INTENT_DATA_SCHEME = "alarm-id";

    private final Context context;
    private final AlarmManager alarmManager;

    private final AtomicInteger counter = new AtomicInteger(0);
    private final Map<Integer, Runnable> runnables = new HashMap<>();

    public AndroidScheduleManager(Context context) {
        this.context = context;
        alarmManager = (AlarmManager) (context.getSystemService(Context.ALARM_SERVICE));
    }

    @Override
    public void init(Container container) {
        super.init(container);
        IntentFilter filter = new IntentFilter(INTENT_ACTION);
        filter.addDataScheme(INTENT_DATA_SCHEME);
        context.registerReceiver(broadcastReceiver, filter);
    }

    @Override
    public void destroy() {
        context.unregisterReceiver(broadcastReceiver);
        for (Integer id : runnables.keySet()) {
            alarmManager.cancel(makePendingIntent(id));
        }
        super.destroy();
    }

    private PendingIntent makePendingIntent(int id) {
        Intent intent = new Intent(INTENT_ACTION, Uri.fromParts(INTENT_DATA_SCHEME, "//" + id, null));
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Runnable runnable = runnables.get(Integer.parseInt(intent.getData().getHost()));
            if (runnable != null) {
                runnable.run();
            }
        }
    };

    @Override
    public Future<?> scheduleRepeatedExecution(Runnable runnable, long delayMillis, long initialDelayMillis) {
        final int id = counter.getAndIncrement();

        runnables.put(id, runnable);

        int alarmType = getWakeupTreshold().scale(getOtherComponent(AccuracyManager.KEY)) ?
                AlarmManager.ELAPSED_REALTIME_WAKEUP : AlarmManager.ELAPSED_REALTIME;
        PendingIntent alarmIntent = makePendingIntent(id);
        //TODO use repeated batch window scheduling (as described in AlarmManager#setRepeating(...))
        alarmManager.setRepeating(alarmType, delayMillis, initialDelayMillis, alarmIntent);

        return new AlarmFuture(id, alarmIntent);
    }

    @Override
    public Future<?> scheduleExecution(Runnable runnable, long delayMillis) {
        final int id = counter.getAndIncrement();

        runnables.put(id, new OneTimeRunnable(id, runnable));

        int alarmType = getWakeupTreshold().scale(getOtherComponent(AccuracyManager.KEY)) ?
                AlarmManager.ELAPSED_REALTIME_WAKEUP : AlarmManager.ELAPSED_REALTIME;
        PendingIntent alarmIntent = makePendingIntent(id);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setWindow(alarmType, delayMillis,
                    getExecutionLatency().scale(getOtherComponent(AccuracyManager.KEY)), alarmIntent);
        } else {
            alarmManager.set(alarmType, delayMillis, alarmIntent);
        }

        return new AlarmFuture(id, alarmIntent);
    }

    //TODO Could be combined with AlarmFuture and effectively replaced with AndroidTimingManager.Scheduled(-Runnable)
    private class OneTimeRunnable implements Runnable {
        private final int id;
        private final Runnable runnable;

        private OneTimeRunnable(int id, Runnable runnable) {
            this.id = id;
            this.runnable = runnable;
        }

        @Override
        public void run() {
            try {
                runnable.run();
            } finally {
                runnables.remove(id);
            }
        }
    }

    //TODO This should be split up for repeating and one-time alarms
    private class AlarmFuture implements Future<Void> {
        private final int id;
        private final PendingIntent alarmIntent;
        private boolean cancelled;

        public AlarmFuture(int id, PendingIntent alarmIntent) {
            this.id = id;
            this.alarmIntent = alarmIntent;
            cancelled = false;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            if (!cancelled && !isDone()) {
                alarmManager.cancel(alarmIntent);
                runnables.remove(id);
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
            return !runnables.containsKey(id);
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
    }
}
