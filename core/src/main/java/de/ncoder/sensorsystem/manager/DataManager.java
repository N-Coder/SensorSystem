package de.ncoder.sensorsystem.manager;

import de.ncoder.sensorsystem.AbstractComponent;
import de.ncoder.sensorsystem.Component;
import de.ncoder.sensorsystem.DependantComponent;
import de.ncoder.sensorsystem.events.FutureCallback;
import de.ncoder.sensorsystem.events.event.SimpleFutureDoneEvent;
import de.ncoder.typedmap.Key;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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
            LinkedHashSet<Key<? extends Component>> d = new LinkedHashSet<>();
            d.add(ThreadPoolManager.KEY);
            dependencies = Collections.unmodifiableSet(d);
        }
        return dependencies;
    }

    protected <T> FutureCallback<T> defaultCallback() {
        return new FutureCallback<T>() {
            @Override
            public void onDone(FutureTask<T> task) {
                publish(new SimpleFutureDoneEvent<>(task, DataManager.this));
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
