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

import de.ncoder.sensorsystem.AbstractComponent;
import de.ncoder.sensorsystem.events.event.CachedValueChangedEvent;
import de.ncoder.typedmap.Key;

public class AccuracyManager extends AbstractComponent {
	public static final Key<AccuracyManager> KEY = new Key<>(AccuracyManager.class);
	public static final String TAG_CHANGED = AccuracyManager.class.getName() + ".AccuracyChanged";

	public static final int ACCURACY_MIN = 0;
	public static final int ACCURACY_LOW = 25;
	public static final int ACCURACY_MED = 50;
	public static final int ACCURACY_HIGH = 75;
	public static final int ACCURACY_MAX = 100;

	public static final int DEFAULT_ACCURACY = ACCURACY_MED;

	private int accuracy;

	public AccuracyManager() {
		setAccuracy(ACCURACY_MED);
	}

	public AccuracyManager(int accuracy) {
		setAccuracy(accuracy);
	}

	public void setAccuracy(int accuracy) {
		if (accuracy < ACCURACY_MIN || accuracy > ACCURACY_MAX) {
			throw new IllegalArgumentException("Accuracy " + accuracy + " is out of bounds");
		}
		CachedValueChangedEvent<Integer> event = new CachedValueChangedEvent<>(
				TAG_CHANGED, getKey(), this.accuracy, accuracy);
		this.accuracy = accuracy;
		publish(event);
	}

	public int getAccuracy() {
		return accuracy;
	}
}
