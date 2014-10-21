package de.ncoder.sensorsystem.events;

import java.util.concurrent.FutureTask;

public interface FutureCallback<T> {
    public void onDone(FutureTask<T> task);
}
