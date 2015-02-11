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

package de.ncoder.sensorsystem.sensor;

import java.util.concurrent.TimeUnit;

import de.ncoder.sensorsystem.AbstractComponent;
import de.ncoder.sensorsystem.Component;
import de.ncoder.sensorsystem.events.event.CachedValueChangedEvent;
import de.ncoder.sensorsystem.events.event.TransientValueChangedEvent;
import de.ncoder.sensorsystem.events.event.ValueChangedEvent;
import de.ncoder.typedmap.Key;

public abstract class AbstractSensor<T> extends AbstractComponent implements Sensor<T> {
	protected static long DEFAULT_MAX_CHANGE_RATE = 200 /*ms*/;

	private long lastChanged;
	private long maxChangeRate = DEFAULT_MAX_CHANGE_RATE; //not an AccuracyRange for efficiency reasons

	protected boolean mayChange() {
		return System.currentTimeMillis() - lastChanged > maxChangeRate;
	}

	protected final void changed(T oldValue, T newValue) {
		changed(true, oldValue, newValue);
	}

	protected final void changed(T newValue) {
		changed(false, null, newValue);
	}

	protected void changed(boolean hasOldValue, T oldValue, T newValue) {
		if (!mayChange()) return;
		lastChanged = System.currentTimeMillis();
		publishChange(hasOldValue, oldValue, newValue);
	}

	protected void publishChange(boolean hasOldValue, T oldValue, T newValue) {
		publish(newChangedEvent(hasOldValue, oldValue, newValue));
	}

	protected ValueChangedEvent<T> newChangedEvent(boolean hasOldValue, T oldValue, T newValue) {
		if (hasOldValue) {
			return new CachedValueChangedEvent<>(
					TAG_CHANGED, getKey(), lastChanged, oldValue, newValue
			);
		} else {
			return new TransientValueChangedEvent<>(
					TAG_CHANGED, getKey(), lastChanged, newValue
			);
		}
	}

	public long getMaxChangeRate() {
		return maxChangeRate;
	}

	public void setMaxChangeRate(long maxChangeRate, TimeUnit unit) {
		this.maxChangeRate = unit.toMillis(maxChangeRate);
	}

	@Override
	public Key<? extends Component> getKey() {
		return super.getKey();
	}

	@Override
	public long lastChange() {
		return lastChanged;
	}
}
