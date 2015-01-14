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
import de.ncoder.sensorsystem.events.event.SimpleValueChangedEvent;

public abstract class AbstractSensor<T> extends AbstractComponent implements Sensor<T> {
    private long lastChanged;
    private long maxChangeRate = 200 /*ms*/; //not an AccuracyRange for efficiency reasons

    protected boolean mayChange() {
        return System.currentTimeMillis() - lastChanged > maxChangeRate;
    }

    protected void changed(T oldValue, T newValue) {
        if (!mayChange()) return;
        lastChanged = System.currentTimeMillis();
        publishChange(oldValue, newValue);
    }

    protected void publishChange(T oldValue, T newValue) {
        publish(new SimpleValueChangedEvent<>(
                getName(), this, lastChanged, oldValue, newValue
        ));
    }

    public long getMaxChangeRate() {
        return maxChangeRate;
    }

    public void setMaxChangeRate(long maxChangeRate, TimeUnit unit) {
        this.maxChangeRate = unit.toMillis(maxChangeRate);
    }

    @Override
    public String getName() {
        return getClass().getName();
    }

    @Override
    public long lastChange() {
        return lastChanged;
    }
}
