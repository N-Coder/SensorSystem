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

package de.ncoder.sensorsystem;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.OverridingMethodsMustInvokeSuper;

import de.ncoder.sensorsystem.events.EventManager;
import de.ncoder.sensorsystem.events.event.Event;
import de.ncoder.typedmap.Key;

public class AbstractComponent implements Component {
	@Nullable
	private Container container;
	@Nullable
	private Key<? extends Component> key;

	@Override
	@OverridingMethodsMustInvokeSuper
	public void init(@Nonnull Container container, @Nonnull Key<? extends Component> key) {
		if (this.container != null || this.key != null) {
			throw new IllegalStateException("Component " + getClass().getSimpleName() + " can't be shared!");
		}
		this.container = Objects.requireNonNull(container, "container");
		this.key = Objects.requireNonNull(key, "key");
	}

	@Override
	@OverridingMethodsMustInvokeSuper
	public void destroy(@Nullable Key<? extends Component> key) {
		assert this.key == null || key == this.key;
		container = null;
		this.key = null;
	}

	protected boolean isActive() {
		return container != null;
	}

	@Nullable
	protected Container getContainer() {
		return container;
	}

	@Nullable
	public Key<? extends Component> getKey() {
		return key;
	}

	@Nullable
	protected <T extends Component> T getOtherComponent(@Nonnull Key<T> key) {
		Container container = getContainer();
		if (container != null) {
			return container.get(key);
		} else {
			return null;
		}
	}

	@Nonnull
	protected <T extends Component> T requireOtherComponent(@Nonnull Key<T> key) {
		T component = getOtherComponent(key);
		if (component == null) {
			throw new SimpleContainer.DependencyException(getKey(), this, key);
		}
		return component;
	}

	protected void publish(@Nonnull Event event) {
		EventManager eventManager = getOtherComponent(EventManager.KEY);
		if (eventManager != null) {
			eventManager.publish(event);
		}
	}

	@Override
	public String toString() {
		String name = getClass().getName();
		Key<? extends Component> key = getKey();
		if (key != null && !key.getValueClass().equals(getClass())) {
			//append Key if it doesn't exactly match this Component
			name += "<" + key.toString() + ">";
		}
		return name;
	}
}
