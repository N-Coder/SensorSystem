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

import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import de.ncoder.sensorsystem.AbstractComponent;
import de.ncoder.sensorsystem.Component;
import de.ncoder.sensorsystem.DependantComponent;
import de.ncoder.sensorsystem.Utils;
import de.ncoder.sensorsystem.events.FutureCallback;
import de.ncoder.sensorsystem.events.event.SimpleFutureDoneEvent;
import de.ncoder.typedmap.Key;

public abstract class DataManager extends AbstractComponent implements DependantComponent {
	protected ReadWriteLock lock = new ReentrantReadWriteLock();

	public Lock getReadLock() {
		return lock.readLock();
	}

	// ------------------------------------------------------------------------

	private static Set<Key<? extends Component>> dependencies;

	@Override
	public Set<Key<? extends Component>> dependencies() {
		if (dependencies == null) {
			dependencies = Utils.<Key<? extends Component>>wrapSet(ThreadPoolManager.KEY);
		}
		return dependencies;
	}

	protected <T> FutureCallback<T> defaultCallback() {
		return new FutureCallback<T>() {
			@Override
			public void onDone(FutureTask<T> task) {
				publish(new SimpleFutureDoneEvent(task, getKey()));
			}
		};
	}

	protected <T> FutureTask<T> execute(final Callable<T> callable) {
		return execute(callable, this.<T>defaultCallback());
	}

	protected <T> FutureTask<T> execute(final Callable<T> callable, final FutureCallback<T> callback) {
		FutureTask<T> futureTask = new FutureTask<T>(callable) {
			@Override
			public void run() {
				lock.writeLock().lock();
				try {
					super.run();
				} finally {
					lock.writeLock().unlock();
				}
			}

			@Override
			protected void done() {
				callback.onDone(this);
			}
		};
		getOtherComponent(ThreadPoolManager.KEY).execute(futureTask);
		return futureTask;
	}
}
