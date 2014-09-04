package de.ncoder.sensorsystem.manager.timed;

import java.util.concurrent.FutureTask;

public interface FutureCallback<T> {
    public void onDone(FutureTask<T> task);
}
