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

public class BooleanAccuracyRange<V> extends AccuracyRange<Boolean, V> {
    private int valueThreshold;
    private boolean lowAccuracyValue;

    public BooleanAccuracyRange(int valueThreshold, boolean lowAccuracyValue, V additional) {
        super(additional);
        this.valueThreshold = valueThreshold;
        this.lowAccuracyValue = lowAccuracyValue;
    }

    public BooleanAccuracyRange(int valueThreshold, boolean lowAccuracyValue) {
        super(null);
        this.valueThreshold = valueThreshold;
        this.lowAccuracyValue = lowAccuracyValue;
    }

    public int getValueThreshold() {
        return valueThreshold;
    }

    public void setValueThreshold(int valueThreshold) {
        this.valueThreshold = valueThreshold;
    }

    public boolean getLowAccuracyValue() {
        return lowAccuracyValue;
    }

    public void setLowAccuracyValue(boolean lowAccuracyValue) {
        this.lowAccuracyValue = lowAccuracyValue;
    }

    public boolean getHighAccuracyValue() {
        return !lowAccuracyValue;
    }

    @Override
    public Boolean scale(int accuracy) {
        return accuracy <= getValueThreshold() ? getLowAccuracyValue() : getHighAccuracyValue();
    }
}
