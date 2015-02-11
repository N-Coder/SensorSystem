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

import de.ncoder.sensorsystem.Component;
import de.ncoder.sensorsystem.events.EventUtils;
import de.ncoder.typedmap.Key;

public class CachedValueChangedEvent<V> extends SimpleEvent implements ValueChangedEvent<V> {
	private final V oldValue, newValue;

	public CachedValueChangedEvent(Key<? extends Component> source, V oldValue, V newValue) {
		this(null, source, oldValue, newValue);
	}

	public CachedValueChangedEvent(String tag, Key<? extends Component> source, V oldValue, V newValue) {
		super(tag, source);
		this.oldValue = oldValue;
		this.newValue = newValue;
	}

	public CachedValueChangedEvent(String tag, Key<? extends Component> source, long when, V oldValue, V newValue) {
		super(tag, source, when);
		this.oldValue = oldValue;
		this.newValue = newValue;
	}

	public boolean hasOldValue() {
		return true;
	}

	@Override
	public V getOldValue() {
		return oldValue;
	}

	@Override
	public V getNewValue() {
		return newValue;
	}

	@Override
	public String toString() {
		StringBuilder msg = new StringBuilder();
		msg.append(getTag());
		if (getSource() != null) {
			msg.append("<").append(getSource()).append(">");
		}
		if (msg.length() > 0) {
			msg.append(": ");
		}
		msg.append(EventUtils.toString(getOldValue())).append(" -> ").append(EventUtils.toString(getNewValue()));
		return msg.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;
		CachedValueChangedEvent that = (CachedValueChangedEvent) o;
		return !(newValue != null ? !newValue.equals(that.newValue) : that.newValue != null)
				&& !(oldValue != null ? !oldValue.equals(that.oldValue) : that.oldValue != null);
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + (oldValue != null ? oldValue.hashCode() : 0);
		result = 31 * result + (newValue != null ? newValue.hashCode() : 0);
		return result;
	}
}
