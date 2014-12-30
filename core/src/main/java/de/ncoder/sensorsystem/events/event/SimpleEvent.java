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

public class SimpleEvent<T extends Component> implements Event {
    private final long when;
    private final transient T source;
    private final String name;

    public SimpleEvent(String name, T source) {
        this(name, source, System.currentTimeMillis());
    }

    public SimpleEvent(String name, T source, long when) {
        if (name == null && source != null) {
            name = source.getClass().toString();
        }
        this.name = name;
        this.when = when;
        this.source = source;
    }

    @Override
    public long getWhen() {
        return when;
    }

    @Override
    public T getSource() {
        return source;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "[" + getName() + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SimpleEvent)) return false;
        SimpleEvent that = (SimpleEvent) o;
        return when == that.when
                && (name != null ? name.equals(that.name) : that.name == null)
                && (source != null ? source.equals(that.source) : that.source == null);

    }

    @Override
    public int hashCode() {
        int result = (int) (when ^ (when >>> 32));
        result = 31 * result + (source != null ? source.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }
}
