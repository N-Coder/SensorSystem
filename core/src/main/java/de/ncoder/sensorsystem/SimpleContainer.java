package de.ncoder.sensorsystem;

import de.ncoder.sensorsystem.manager.event.ContainerEvent;
import de.ncoder.sensorsystem.manager.event.EventManager;
import de.ncoder.typedmap.Key;
import de.ncoder.typedmap.TypedMap;

import java.util.Iterator;

public class SimpleContainer implements Container {
    private final TypedMap<Component> components = new TypedMap<>();

    @Override
    public <T extends Component> void register(Key<T> key, T component) {
        if (key == null) {
            throw new NullPointerException("key");
        }
        if (component == null) {
            throw new NullPointerException("component");
        }
        if (isRegistered(key)) {
            throw new IllegalArgumentException("Component for " + key + " already registered");
        }
        components.putTyped(key, component);
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
    public <T extends Component> T get(Key<T> key) {
        return components.get(key);
    }

    @Override
    public boolean isRegistered(Key<? extends Component> key) {
        return components.containsKey(key);
    }

    private TypedMap<Component> componentsUnmodifiable;

    public TypedMap<Component> getData() {
        if (componentsUnmodifiable == null) {
            componentsUnmodifiable = components.unmodifiableView();
        }
        return componentsUnmodifiable;
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
