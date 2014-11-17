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

import de.ncoder.sensorsystem.events.EventManager;
import de.ncoder.sensorsystem.events.event.Event;
import de.ncoder.typedmap.Key;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class AbstractComponent implements Component {
    private Container container;

    @Override
    public void init(Container container) {
        this.container = container;
    }

    @Override
    public void destroy() {
        container = null;
    }

    protected boolean isActive() {
        return container != null;
    }

    protected Container getContainer() {
        return container;
    }

    protected <T extends Component> T getOtherComponent(Key<T> key) {
        Container container = getContainer();
        if (container != null) {
            return container.get(key);
        } else {
            return null;
        }
    }

    protected void publish(Event event) {
        EventManager eventManager = getOtherComponent(EventManager.KEY);
        if (eventManager != null) {
            eventManager.publish(event);
        }
    }

    @Override
    public String toString() {
        String name = getClass().getSimpleName();
        if (name == null || name.isEmpty()) {
            name = getClass().getName();
            int index = name.lastIndexOf(".");
            if (index >= 0 && index + 1 < name.length()) {
                name = name.substring(index + 1);
            }
        }
        return name;
    }

    @SafeVarargs
    public static Set<Key<? extends Component>> wrapDependencies(Key<? extends Component>... keys) {
        return Collections.unmodifiableSet(new HashSet<>(Arrays.asList(keys)));
    }
}
