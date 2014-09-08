package de.ncoder.sensorsystem.manager.timed;

import de.ncoder.sensorsystem.AbstractComponent;
import de.ncoder.sensorsystem.Key;

import java.util.concurrent.*;

public class ThreadPoolManager extends AbstractComponent implements Executor {
    public static final Key<ThreadPoolManager> KEY = new Key<>(ThreadPoolManager.class);

    private final ExecutorService executor;

    public ThreadPoolManager(ExecutorService executor) {
        this.executor = executor;
    }

    @Override
    public void execute(Runnable command) {
        executor.execute(command);
    }

    public <T> Future<T> submit(Callable<T> task) {
        return executor.submit(task);
    }

    public <T> Future<T> submit(Runnable task, T result) {
        return executor.submit(task, result);
    }

    public Future<?> submit(Runnable task) {
        return executor.submit(task);
    }

    @Override
    public boolean isActive() {
        return super.isActive() && !executor.isShutdown();
    }

    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return executor.awaitTermination(timeout, unit);
    }

    public boolean isTerminated() {
        return executor.isTerminated();
    }

    public Runnable awakeWrapper(Runnable runnable) {
        return runnable;
    }

    public <T> Callable<T> awakeWrapper(Callable<T> callable) {
        return callable;
    }
}
