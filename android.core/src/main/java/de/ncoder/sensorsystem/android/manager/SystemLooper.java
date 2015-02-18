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

package de.ncoder.sensorsystem.android.manager;

import android.os.Looper;

import java.util.Objects;

import javax.annotation.Nonnull;

import de.ncoder.sensorsystem.Component;
import de.ncoder.sensorsystem.Container;
import de.ncoder.sensorsystem.events.EventManager;
import de.ncoder.sensorsystem.events.event.ComponentEvent;
import de.ncoder.sensorsystem.events.event.Event;
import de.ncoder.typedmap.Key;

public class SystemLooper extends Thread implements Component {
	public static final Key<SystemLooper> KEY = new Key<>(SystemLooper.class);

	private Container container;
	private Key<? extends Component> lastKey;
	private Looper looper;

	@Override
	public void init(@Nonnull Container container, @Nonnull Key<? extends Component> key) {
		if (this.container != null && container != this.container) {
			throw new IllegalStateException("This component can't be shared across containers!");
		}
		this.container = Objects.requireNonNull(container, "container");
		lastKey = Objects.requireNonNull(key, "key");
		this.start();
	}

	@Override
	public void run() {
		Looper.prepare();
		looper = Looper.myLooper();
		publish(new ComponentEvent(lastKey, ComponentEvent.Type.STARTED));
		Looper.loop();
		looper = null;
		publish(new ComponentEvent(lastKey, ComponentEvent.Type.STOPPED));
		if (container != null) {
			container.unregister(this);
		}
	}

	private void publish(Event event) {
		if (container != null) {
			EventManager eventManager = container.get(EventManager.KEY);
			if (eventManager != null) {
				eventManager.publish(event);
			}
		}
	}

	public Looper getLooper() {
		return looper;
	}

	@Override
	@SuppressWarnings("deprecation")
	public void destroy(Key<? extends Component> key) {
		if (looper != null) {
			looper.quit();
		}
		container = null;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
}
