package de.ncoder.sensorsystem.manager.timed;

import de.ncoder.sensorsystem.AbstractComponent;
import de.ncoder.sensorsystem.Container;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class ThreadPoolManager extends AbstractComponent  {
    public static final Container.Key<ThreadPoolManager> KEY = new Container.Key<>(ThreadPoolManager.class);

    private final ExecutorService executor;

    public ThreadPoolManager(ExecutorService executor) {
        this.executor = executor;
    }

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

    public Runnable awakeWrapper(Runnable runnable) {
        return runnable;
    }

    public <T> Callable<T> awakeWrapper(Callable<T> callable) {
        return callable;
    }
}
