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
import de.ncoder.typedmap.Key;

/**
 * Simple*Events should be easy to instantiate and dispatch, they are not made for extension!
 * Generics make inheritance easier but not usage. For this reason Simple*Events are not Generic.
 * If you want TypeSafety, provide your own implementation of Event.
 */
public class SimpleEvent implements Event {
	private final String tag;
	private final long when;
	private final Key<? extends Component> source;

	public SimpleEvent(String tag, Key<? extends Component> source) {
		this(tag, source, System.currentTimeMillis());
	}

	public SimpleEvent(String tag, Key<? extends Component> source, long when) {
		if (tag == null || tag.isEmpty()) {
			tag = getClass().getName();
		}
		this.tag = tag;
		this.when = when;
		this.source = source;
	}

	@Override
	public String getTag() {
		return tag;
	}

	@Override
	public long getWhen() {
		return when;
	}

	@Override
	public Key<? extends Component> getSource() {
		return source;
	}

	@Override
	public String toString() {
		return getTag() + (getSource() != null ? "<" + getSource() + ">" : "");
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof SimpleEvent)) return false;
		SimpleEvent that = (SimpleEvent) o;
		return when == that.when
				&& (source != null ? source.equals(that.source) : that.source == null)
				&& (tag != null ? tag.equals(that.tag) : that.tag == null);
	}

	@Override
	public int hashCode() {
		int result = tag != null ? tag.hashCode() : 0;
		result = 31 * result + (int) (when ^ (when >>> 32));
		result = 31 * result + (source != null ? source.hashCode() : 0);
		return result;
	}
}
