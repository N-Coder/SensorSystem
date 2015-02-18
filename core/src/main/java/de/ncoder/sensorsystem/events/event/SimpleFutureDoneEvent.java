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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import de.ncoder.sensorsystem.Component;
import de.ncoder.typedmap.Key;

public class SimpleFutureDoneEvent extends SimpleEvent implements FutureDoneEvent<Object> {
	@Nonnull
	private final Future<?> future;

	public SimpleFutureDoneEvent(@Nonnull Future<?> future, Key<? extends Component> source) {
		super(null, source);
		this.future = future;
	}

	public SimpleFutureDoneEvent(@Nonnull Future<?> future, Key<? extends Component> source, long when) {
		super(null, source, when);
		this.future = future;
	}

	public SimpleFutureDoneEvent(@Nonnull Future<?> future, String tag, Key<? extends Component> source) {
		super(tag, source);
		this.future = future;
	}

	public SimpleFutureDoneEvent(@Nonnull Future<?> future, String tag, Key<? extends Component> source, long when) {
		super(tag, source, when);
		this.future = future;
	}

	@Nonnull
	@Override
	@SuppressWarnings("unchecked")
	public Future<Object> getFuture() {
		return (Future<Object>) future;
	}

	@Nullable
	@Override
	public Object getResult() throws ExecutionException {
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

	@Nullable
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

	@Nonnull
	@Override
	public String toString() {
		StringBuilder msg = new StringBuilder();
		msg.append(getTag());
		if (getSource() != null) {
			msg.append("<").append(getSource()).append(">");
		}
		if (msg.length() > 0) {
			msg.append(" ");
		}
		msg.append(future);
		try {
			//TODO this should not block
			Object ret = future.get();
			msg.append(" done: ").append(ret);
		} catch (InterruptedException | ExecutionException | CancellationException e) {
			msg.append(" failed: ").append(e);
		}
		return msg.toString();
	}
}
