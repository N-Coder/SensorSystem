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
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import de.ncoder.sensorsystem.Component;
import de.ncoder.sensorsystem.Container;
import de.ncoder.sensorsystem.DependantComponent;
import de.ncoder.sensorsystem.PrivilegedComponent;
import de.ncoder.sensorsystem.android.ContainerService;
import de.ncoder.sensorsystem.manager.DataManager;
import de.ncoder.sensorsystem.manager.ScheduleManager;
import de.ncoder.sensorsystem.manager.accuracy.AccuracyManager;
import de.ncoder.sensorsystem.manager.accuracy.BooleanAccuracyRange;
import de.ncoder.sensorsystem.manager.accuracy.LongAccuracyRange;
import de.ncoder.typedmap.Key;

public class AndroidScheduleManager extends ScheduleManager implements DependantComponent, PrivilegedComponent {
	private static final String TAG = AndroidScheduleManager.class.getSimpleName();
	private String INTENT_ACTION;
	private static final String INTENT_EXTRA_ID = "alarm-id";

	private AlarmManager alarmManager;

	private final AtomicInteger counter = new AtomicInteger(0);
	private final Map<Integer, Runnable> runnables = new ConcurrentHashMap<>();

	@Override
	public void init(Container container) {
		super.init(container);
		Context context = getContext();
		alarmManager = (AlarmManager) (context.getSystemService(Context.ALARM_SERVICE));

		INTENT_ACTION = context.getPackageName() + ".ScheduleManager.ALARM";
		IntentFilter filter = new IntentFilter(INTENT_ACTION);
		filter.addDataScheme(INTENT_EXTRA_ID);
		context.registerReceiver(broadcastReceiver, filter);
	}

	@Override
	public void destroy() {
		getContext().unregisterReceiver(broadcastReceiver);
		for (Integer id : runnables.keySet()) {
			alarmManager.cancel(makePendingIntent(id));
		}
		super.destroy();
	}

	private Context getContext() {
		return getOtherComponent(ContainerService.KEY_CONTEXT);
	}

	private PendingIntent makePendingIntent(int id) {
		Intent intent = new Intent(INTENT_ACTION, Uri.fromParts(INTENT_EXTRA_ID, String.valueOf(id), null));
		//intent.putExtra("source-name", toString());
		//intent.putExtra("source-hash", String.valueOf(hashCode()));
		//intent.putExtra("source-time", System.currentTimeMillis());
		return PendingIntent.getBroadcast(getContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
	}

	private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			int id = Integer.parseInt(intent.getData().getSchemeSpecificPart().replaceAll("[^0-9]", ""));
			Runnable runnable = runnables.get(id);
			//Log.v(TAG, "Run #" + id + ": " + runnable + " from " + intent);
			if (runnable != null) {
				runnable.run();
			} else {
				PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), 0, intent, PendingIntent.FLAG_NO_CREATE);
				Log.w(TAG, "Discarding unknown alarm #" + id + " and cancelling source PendingIntent " + pendingIntent);
				if (pendingIntent != null) {
					alarmManager.cancel(pendingIntent);
				}
			}
		}
	};

	//---------------------------------SCHEDULING FUNCTIONS--------------------

	public Future<?> scheduleRepeatedExecution(Runnable runnable, long initialDelayMillis, long delayMillis, boolean wakeUp, long executionLatency) {
		final int id = counter.getAndIncrement();

		runnables.put(id, runnable);

		int alarmType = wakeUp ? AlarmManager.ELAPSED_REALTIME_WAKEUP : AlarmManager.ELAPSED_REALTIME;
		PendingIntent alarmIntent = makePendingIntent(id);
		//TODO consider using repeated batch window scheduling as described in AlarmManager#setRepeating(...) to correctly use executionLatency
		alarmManager.setRepeating(alarmType, initialDelayMillis, delayMillis, alarmIntent);

		return new AlarmFuture(id, alarmIntent);
	}

	@Override
	public Future<?> scheduleRepeatedExecution(Runnable runnable, long initialDelayMillis, long delayMillis) {
		return scheduleRepeatedExecution(runnable, initialDelayMillis, delayMillis,
				getWakeupTreshold().scale(getOtherComponent(AccuracyManager.KEY)),
				getExecutionLatency().scale(getOtherComponent(AccuracyManager.KEY)));
	}

	public Future<?> scheduleExecution(Runnable runnable, long delayMillis, boolean wakeUp, long executionLatency) {
		final int id = counter.getAndIncrement();
		runnables.put(id, new OneTimeRunnable(id, runnable));

		int alarmType = wakeUp ? AlarmManager.ELAPSED_REALTIME_WAKEUP : AlarmManager.ELAPSED_REALTIME;
		PendingIntent alarmIntent = makePendingIntent(id);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			alarmManager.setWindow(alarmType, delayMillis, executionLatency, alarmIntent);
		} else {
			alarmManager.set(alarmType, delayMillis, alarmIntent);
		}

		return new AlarmFuture(id, alarmIntent);
	}

	@Override
	public Future<?> scheduleExecution(Runnable runnable, long delayMillis) {
		return scheduleExecution(runnable, delayMillis,
				getWakeupTreshold().scale(getOtherComponent(AccuracyManager.KEY)),
				getExecutionLatency().scale(getOtherComponent(AccuracyManager.KEY)));
	}

	//---------------------------------CLASSES---------------------------------

	private class OneTimeRunnable implements Runnable {
		private final int id;
		private final Runnable runnable;

		private OneTimeRunnable(int id, Runnable runnable) {
			this.id = id;
			this.runnable = runnable;
		}

		@Override
		public void run() {
			runnables.remove(id);
			runnable.run();
		}
	}

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

	//---------------------------------COMPONENT INFO--------------------------

	private static Set<Key<? extends Component>> dependencies;

	@Override
	public Set<Key<? extends Component>> dependencies() {
		if (dependencies == null) {
			dependencies = DataManager.<Key<? extends Component>>wrapSet(ContainerService.KEY_CONTEXT);
		}
		return dependencies;
	}

	private static Set<String> permissions;

	@Override
	public Set<String> requiredPermissions() {
		if (permissions == null) {
			permissions = DataManager.wrapSet("android.permission.WAKE_LOCK");
		}
		return permissions;
	}
}
