package de.ncoder.sensorsystem.android.manager.timed;

import android.content.Context;
import android.os.PowerManager;
import de.ncoder.sensorsystem.manager.timed.ThreadPoolManager;

import java.util.concurrent.*;

public class AndroidThreadPoolManager extends ThreadPoolManager {
    private static final String WAKELOCK_TAG = AndroidThreadPoolManager.class.getName() + ".WAKE_LOCK";

    private final PowerManager.WakeLock wakelock;

    public AndroidThreadPoolManager(ExecutorService executor, Context context) {
        super(executor);
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        wakelock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKELOCK_TAG);
    }

    public static ExecutorService DEFAULT_EXECUTOR() {
        return new ThreadPoolExecutor(0, Integer.MAX_VALUE,
                20L, TimeUnit.SECONDS,
                new SynchronousQueue<Runnable>());
    }

    @Override
    public void destroy() {
        super.destroy();
        while(wakelock.isHeld()) {
            wakelock.release();
        }
    }

    @Override
    public Future<?> executeAwake(Runnable command) {
        return super.executeAwake(new AwakeRunnable(command));
    }

    private class AwakeRunnable implements Runnable {
        private final Runnable runnable;

        private AwakeRunnable(Runnable runnable) {
            this.runnable = runnable;
        }

        @Override
        public void run() {
            wakelock.acquire();
            try {
                runnable.run();
            } finally {
                wakelock.release();
            }
        }
    }
}
