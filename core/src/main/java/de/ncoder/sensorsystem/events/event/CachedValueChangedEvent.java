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

public class CachedValueChangedEvent<ValueT, SourceT extends Component> extends SimpleEvent<SourceT> implements ValueChangedEvent<ValueT> {
	private final ValueT oldValue, newValue;

	public CachedValueChangedEvent(String name, ValueT oldValue, ValueT newValue) {
		this(name, null, oldValue, newValue);
	}

	public CachedValueChangedEvent(SourceT source, ValueT oldValue, ValueT newValue) {
		this(null, source, oldValue, newValue);
	}

	public CachedValueChangedEvent(String name, SourceT source, ValueT oldValue, ValueT newValue) {
		super(name, source);
		this.oldValue = oldValue;
		this.newValue = newValue;
	}

	public CachedValueChangedEvent(String name, SourceT source, long when, ValueT oldValue, ValueT newValue) {
		super(name, source, when);
		this.oldValue = oldValue;
		this.newValue = newValue;
	}

	public boolean hasOldValue() {
		return true;
	}

	@Override
	public ValueT getOldValue() {
		return oldValue;
	}

	@Override
	public ValueT getNewValue() {
		return newValue;
	}

	@Override
	public String toString() {
		return "[" + getName() + ": " + EventUtils.toString(getOldValue()) + "->" + EventUtils.toString(getNewValue()) + "]";
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
