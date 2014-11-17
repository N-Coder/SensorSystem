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

import android.content.Context;
import android.os.PowerManager;
import de.ncoder.sensorsystem.manager.ThreadPoolManager;

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
        while (wakelock.isHeld()) {
            wakelock.release();
        }
    }

    @Override
    public Runnable awakeWrapper(Runnable runnable) {
        return new AwakeRunnable(runnable);
    }

    @Override
    public <T> Callable<T> awakeWrapper(Callable<T> callable) {
        return new AwakeCallable<>(callable);
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

    private class AwakeCallable<T> implements Callable<T> {
        private final Callable<T> callable;

        private AwakeCallable(Callable<T> callable) {
            this.callable = callable;
        }

        @Override
        public T call() throws Exception {
            wakelock.acquire();
            try {
                return callable.call();
            } finally {
                wakelock.release();
            }
        }
    }
}
