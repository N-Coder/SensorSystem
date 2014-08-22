package de.ncoder.sensorsystem;

import de.ncoder.sensorsystem.manager.event.ContainerEvent;
import de.ncoder.sensorsystem.manager.event.EventManager;

import java.util.*;

public class SimpleContainer implements Container {
    private final Map<Key<? extends Component>, Component> components = new HashMap<>();

    @Override
    public <T extends Component> void register(Key<T> key, T component) {
        if (isRegistered(key)) {
            throw new IllegalArgumentException("Component for " + key + " already registered");
        }
        components.put(key, component);
        component.init(this);
        publish(new ContainerEvent.ComponentAdded(key, component));
    }

    @Override
    public void unregister(Key<? extends Component> key) {
        Component component = components.remove(key);
        if (component != null) {
            component.destroy();
            publish(new ContainerEvent.ComponentRemoved(key, component));
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Component> T get(Key<T> key) {
        return (T) components.get(key);
    }

    @Override
    public boolean isRegistered(Key<? extends Component> key) {
        return components.containsKey(key);
    }

    private Set<Map.Entry<Key<? extends Component>, Component>> entriesView;

    public Set<Map.Entry<Key<? extends Component>, Component>> entrySet() {
        if (entriesView == null) {
            entriesView = Collections.unmodifiableSet(components.entrySet());
        }
        return entriesView;
    }

    // ------------------------------------------------------------------------

    @Override
    public void shutdown() {
        publish(new ContainerEvent.ShutdownRequested());
        Iterator<Component> it = components.values().iterator();
        while (it.hasNext()) {
            Component component = it.next();
            component.destroy();
            it.remove();
        }
    }

    private void publish(ContainerEvent event) {
        EventManager manager = get(EventManager.KEY);
        if (manager != null) {
            manager.publish(event);
        }
    }
}
