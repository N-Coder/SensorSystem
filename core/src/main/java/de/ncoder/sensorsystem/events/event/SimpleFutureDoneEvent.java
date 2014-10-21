package de.ncoder.sensorsystem.events.event;

import de.ncoder.sensorsystem.Component;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class SimpleFutureDoneEvent<Result, Source extends Component>
        extends SimpleEvent<Source> implements FutureDoneEvent<Result> {
    private final static String DEFAULT_NAME = FutureDoneEvent.class.getName();

    private final Future<Result> future;

    public SimpleFutureDoneEvent(Future<Result> future, Source source) {
        super(DEFAULT_NAME, source);
        this.future = future;
    }

    public SimpleFutureDoneEvent(Future<Result> future, Source source, long when) {
        super(DEFAULT_NAME, source, when);
        this.future = future;
    }

    public SimpleFutureDoneEvent(Future<Result> future, String name, Source source) {
        super(name, source);
        this.future = future;
    }

    public SimpleFutureDoneEvent(Future<Result> future, String name, Source source, long when) {
        super(name, source, when);
        this.future = future;
    }

    @Override
    public Future<Result> getFuture() {
        return future;
    }

    @Override
    public Result getResult() throws ExecutionException {
        try {
            return future.get();
        } catch (InterruptedException e) {
            throw new ExecutionException(e);
        }
    }

    @Override
    public boolean wasSuccess() {
        try {
            future.get();
            return true;
        } catch (ExecutionException | InterruptedException | CancellationException e) {
            return false;
        }
    }

    @Override
    public Throwable getException() {
        try {
            future.get();
            return null;
        } catch (InterruptedException | CancellationException e) {
            return e;
        } catch (ExecutionException e) {
            return e.getCause();
        }
    }

    @Override
    public String toString() {
        try {
            return "[Future " + future + " done: " + future.get() + "]";
        } catch (InterruptedException | ExecutionException | CancellationException e) {
            return "[Future " + future + " failed: " + e + "]";
        }
    }
}
