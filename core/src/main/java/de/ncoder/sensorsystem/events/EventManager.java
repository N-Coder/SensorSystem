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

package de.ncoder.sensorsystem.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import de.ncoder.sensorsystem.AbstractComponent;
import de.ncoder.sensorsystem.Component;
import de.ncoder.sensorsystem.Container;
import de.ncoder.sensorsystem.events.event.Event;
import de.ncoder.typedmap.Key;

public class EventManager extends AbstractComponent implements RemoteEventManager {
	private static final Logger log = LoggerFactory.getLogger(EventManager.class);

	public static final Key<EventManager> KEY = new Key<>(EventManager.class);

	@Override
	public void init(Container container, Key<? extends Component> key) {
		super.init(container, key);
		if (log.isTraceEnabled()) {
			subscribe(new EventListener() {
				@Override
				public void handle(Event event) {
					log.trace(event.toString());
				}
			});
		}
	}

	@Override
	public void destroy(Key<? extends Component> key) {
		listeners.clear();
		super.destroy(key);
	}

	// ------------------------------------------------------------------------

	private final List<EventListener> listeners = new CopyOnWriteArrayList<>();

	public void subscribe(EventListener listener) {
		listeners.add(listener);
	}

	public void unsubscribe(EventListener listener) {
		listeners.remove(listener);
	}

	public void publish(Event event) {
		for (EventListener listener : listeners) {
			try {
				listener.handle(event);

				// Dear compiler, please trust me, I know what I'm doing
				if (false) throw new IOException();
			} catch (IOException e) {
				log.warn("Could not send event to RemoteListener " + listener, e);
			}
		}
	}
}
