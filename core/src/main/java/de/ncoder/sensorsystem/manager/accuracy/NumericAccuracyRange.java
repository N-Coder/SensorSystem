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

package de.ncoder.sensorsystem.manager.accuracy;

public abstract class NumericAccuracyRange<V extends Number, T> extends AccuracyRange<V, T> {
    private V leastAccurate, mostAccurate;

    public NumericAccuracyRange(V leastAccurate, V mostAccurate, T additional) {
        super(additional);
        this.leastAccurate = leastAccurate;
        this.mostAccurate = mostAccurate;
    }

    public NumericAccuracyRange(V leastAccurate, V mostAccurate) {
        this(leastAccurate, mostAccurate, null);
    }

    public NumericAccuracyRange(V fixedValue, T additional) {
        this(fixedValue, fixedValue, additional);
    }

    public NumericAccuracyRange(V fixedValue) {
        this(fixedValue, (T) null);
    }

    public V getLeastAccurate() {
        return leastAccurate;
    }

    public void setLeastAccurate(V leastAccurate) {
        this.leastAccurate = leastAccurate;
    }

    public V getMostAccurate() {
        return mostAccurate;
    }

    public void setMostAccurate(V mostAccurate) {
        this.mostAccurate = mostAccurate;
    }

    public void setFixedAccuracy(V fixedAccuracy) {
        setLeastAccurate(fixedAccuracy);
        setMostAccurate(fixedAccuracy);
    }
}
