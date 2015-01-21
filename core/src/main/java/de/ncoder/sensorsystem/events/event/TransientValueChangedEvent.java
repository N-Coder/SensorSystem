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

public class TransientValueChangedEvent<ValueT, SourceT extends Component> extends SimpleEvent<SourceT> implements ValueChangedEvent<ValueT> {
	private final ValueT newValue;

	public TransientValueChangedEvent(String name, ValueT newValue) {
		this(name, null, newValue);
	}

	public TransientValueChangedEvent(SourceT source, ValueT newValue) {
		this(null, source, newValue);
	}

	public TransientValueChangedEvent(String name, SourceT source, ValueT newValue) {
		super(name, source);
		this.newValue = newValue;
	}

	public TransientValueChangedEvent(String name, SourceT source, long when, ValueT newValue) {
		super(name, source, when);
		this.newValue = newValue;
	}

	public boolean hasOldValue() {
		return false;
	}

	@Override
	@Deprecated
	public ValueT getOldValue() {
		return null;
	}

	@Override
	public ValueT getNewValue() {
		return newValue;
	}

	@Override
	public String toString() {
		return "[" + getName() + ": " + EventUtils.toString(getNewValue()) + "]";
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;
		TransientValueChangedEvent that = (TransientValueChangedEvent) o;
		return !(newValue != null ? !newValue.equals(that.newValue) : that.newValue != null);
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + (newValue != null ? newValue.hashCode() : 0);
		return result;
	}
}
