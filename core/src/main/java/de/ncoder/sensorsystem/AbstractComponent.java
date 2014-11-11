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
