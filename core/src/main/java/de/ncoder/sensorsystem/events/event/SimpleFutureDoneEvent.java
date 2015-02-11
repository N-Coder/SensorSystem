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

package de.ncoder.sensorsystem.events.event;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import de.ncoder.sensorsystem.Component;

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
