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

import java.io.Serializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import de.ncoder.sensorsystem.Component;
import de.ncoder.sensorsystem.sensor.Sensor;
import de.ncoder.typedmap.Key;

public interface Event extends Serializable {
	/**
	 * @return the system time in milliseconds when this Event occurred
	 */
	public long getWhen();

	/**
	 * Get the Key of the Component this Event originates from.
	 * Components that can be registered for multiple Keys,
	 * need to specify which Key is used for Events.
	 *
	 * @return the Key of the Component this Event originates from
	 */
	@Nullable
	public Key<? extends Component> getSource();

	/**
	 * <p>Get the Tag of this Event.
	 * This is used to differentiate between Events of the same class,
	 * so that instead of defining a new subtype that doesn't add any functionality,
	 * a different tag can be used.
	 * The default value is the fully qualified class name of the Event.</p>
	 * <p>For example, the default Tag of a {@link ValueChangedEvent} would be {@code de.ncoder.sensorsystem.events.event.ValueChangedEvent}.
	 * When the specific value of a {@link Sensor} changed, {@code de.ncoder.sensorsystem.sensor.Sensor.Changed} could be used.</p>
	 *
	 * @return the Tag of this Event
	 */
	@Nonnull
	public String getTag();
}
