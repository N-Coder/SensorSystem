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

import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;

import javax.annotation.Nonnull;

import de.ncoder.sensorsystem.AbstractComponent;
import de.ncoder.sensorsystem.Component;
import de.ncoder.sensorsystem.events.event.ComponentEvent;
import de.ncoder.typedmap.Key;

public class ThreadPoolManager extends AbstractComponent implements Executor {
	public static final Key<ThreadPoolManager> KEY = new Key<>(ThreadPoolManager.class);

	@Nonnull
	private final ExecutorService executor;

	public ThreadPoolManager() {
		this(new ThreadPoolExecutor(0, Integer.MAX_VALUE,
				60L, TimeUnit.SECONDS,
				new SynchronousQueue<Runnable>()));
	}

	public ThreadPoolManager(@Nonnull ExecutorService executor) {
		this.executor = Objects.requireNonNull(executor, "executor");
	}

	@Override
	public void execute(@Nonnull Runnable command) {
		executor.execute(command);
	}

	@Nonnull
	public <T> Future<T> submit(@Nonnull Callable<T> task) {
		return executor.submit(task);
	}

	@Nonnull
	public <T> Future<T> submit(@Nonnull Runnable task, T result) {
		return executor.submit(task, result);
	}

	@Nonnull
	public Future<?> submit(@Nonnull Runnable task) {
		return executor.submit(task);
	}

	@Override
	public void destroy(Key<? extends Component> key) {
		List<Runnable> disposed = executor.shutdownNow();
		publish(new ComponentEvent(getKey(), ComponentEvent.Type.STOPPED, "disposed Events " + disposed));
		super.destroy(key);
	}

	@Override
	public boolean isActive() {
		return super.isActive() && !executor.isShutdown();
	}

	public boolean awaitTermination(long timeout, @Nonnull TimeUnit unit) throws InterruptedException {
		return executor.awaitTermination(timeout, unit);
	}

	public boolean isTerminated() {
		return executor.isTerminated();
	}

	@Nonnull
	public Runnable awakeWrapper(@Nonnull Runnable runnable) {
		return runnable;
	}

	@Nonnull
	public <T> Callable<T> awakeWrapper(@Nonnull Callable<T> callable) {
		return callable;
	}
}
