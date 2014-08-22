package de.ncoder.sensorsystem.manager.timed;

import de.ncoder.sensorsystem.AbstractComponent;
import de.ncoder.sensorsystem.Container;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class ThreadPoolManager extends AbstractComponent {
    public static final Container.Key<ThreadPoolManager> KEY = new Container.Key<ThreadPoolManager>(ThreadPoolManager.class);

    private final ExecutorService executor;

    public ThreadPoolManager(ExecutorService executor) {
        this.executor = executor;
    }

    public Future<?> execute(Runnable command) {
        return executor.submit(command);
    }

    public Future<?> executeAwake(Runnable command) {
        return execute(command);
    }
}
