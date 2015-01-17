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

public abstract class CachedSensor<T> extends AbstractSensor<T> {
    private T cachedValue;
    private boolean cacheValid = false;
    private Thread updateThread = null;

    @Override
    public T get() {
        if (!hasCachedValue()) {
            synchronized (this) {
                if (!hasCachedValue()) {
                    if (!isUpdating()) {
                        updateCache();
                    } else if (updateThread != Thread.currentThread()) {
                        //this should have been delayed until the update was finished, so now we can retry
                        return get();
                    } // else a currently running update causes a new update, use the old value
                }
            }
        }
        return cachedValue;
    }

    public void invalidate() {
        cacheValid = false;
    }

    public boolean hasCachedValue() {
        return cacheValid;
    }

    protected synchronized void updateCache() {
        updateThread = Thread.currentThread();
        try {
            updateCache(fetch());
        } finally {
            updateThread = null;
        }
    }

    protected synchronized void updateCache(T newValue) {
        T oldValue = cachedValue;
        cachedValue = newValue;
        cacheValid = true;
        changed(oldValue, newValue);
    }

    protected abstract T fetch();

    protected boolean isUpdating() {
        return updateThread != null;
    }
}
