package de.ncoder.sensorsystem.manager.event;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public interface FutureDoneEvent<Result> extends Event {
    public Future<Result> getFuture();

    public Result getResult() throws ExecutionException;

    public boolean wasSuccess();

    public Throwable getException();
}
