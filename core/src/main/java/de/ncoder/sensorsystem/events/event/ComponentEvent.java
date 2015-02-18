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

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import de.ncoder.sensorsystem.Component;
import de.ncoder.typedmap.Key;

public class ComponentEvent extends SimpleEvent {
	public static enum Type {
		ADDED, STARTED, STOPPED, REMOVED;

		@Nonnull
		@Override
		public String toString() {
			return ComponentEvent.class.getName() + "." + super.toString();
		}
	}

	@Nonnull
	private final Type type;
	@Nonnull
	private final String action;

	public ComponentEvent(Key<? extends Component> key, @Nonnull Type type, @Nonnull String action) {
		super(null, Objects.requireNonNull(key));
		this.type = type;
		this.action = action;
	}

	public ComponentEvent(Key<? extends Component> key, @Nonnull Type type) {
		this(key, type, type.name().toLowerCase());
	}

	@Nonnull
	public Type getType() {
		return type;
	}

	@Nullable
	public Key<? extends Component> getKey() {
		return super.getSource();
	}

	@Nonnull
	public String getAction() {
		return action;
	}

	@Nonnull
	public String toString() {
		return getSource() + " " + getAction();
	}
}
