package de.ncoder.sensorsystem;

import de.ncoder.sensorsystem.manager.event.ContainerEvent;
import de.ncoder.sensorsystem.manager.event.EventManager;
import de.ncoder.sensorsystem.remote.RemoteContainer;
import de.ncoder.typedmap.Key;
import de.ncoder.typedmap.TypedMap;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.*;

public class SimpleContainer implements Container, RemoteContainer {
    public static boolean defaultCheckDependencies;

    static {
        defaultCheckDependencies = true;
        try {
            assert false;
            defaultCheckDependencies = false;
        } catch (AssertionError ignore) {
        }
    }

    // ------------------------------------------------------------------------

    private boolean checkDependencies = defaultCheckDependencies;
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
        if (checkDependencies && component instanceof DependantComponent) {
            for (Key<? extends Component> dep : ((DependantComponent) component).dependencies()) {
                if (!isRegistered(dep)) {
                    throw new DependencyException(key, component, dep);
                }
            }
        }
        components.putTyped(key, component);
        component.init(this);
        publish(new ContainerEvent.ComponentAdded(key, component));
    }

    @Override
    public void unregister(Key<? extends Component> key) {
        if (checkDependencies) {
            for (Map.Entry<Key<? extends Component>, Component> entry : components.entrySet()) {
                if (entry.getValue() instanceof DependantComponent
                        && ((DependantComponent) entry.getValue()).dependencies().contains(key)) {
                    throw new DependencyException(entry.getKey(), key);
                }
            }
        }
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

    private transient TypedMap<Component> componentsUnmodifiable;

    public TypedMap<Component> getData() {
        if (componentsUnmodifiable == null) {
            componentsUnmodifiable = components.unmodifiableView();
        }
        return componentsUnmodifiable;
    }

    private transient UnmodifiableSet<Key<? extends Component>> keysUnmodifiable;

    @Override
    public Collection<Key<? extends Component>> getKeys() {
        if (keysUnmodifiable == null) {
            keysUnmodifiable = new UnmodifiableSet<>(components.keySet());
        }
        return keysUnmodifiable;
    }

    public boolean isCheckingDependencies() {
        return checkDependencies;
    }

    public void setCheckDependencies(boolean checkDependencies) {
        this.checkDependencies = checkDependencies;
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

    // ------------------------------------------------------------------------

    public static class DependencyException extends IllegalStateException {
        private final Key<? extends Component> dependant, dependency;

        public DependencyException(Key<? extends Component> dependant, Key<? extends Component> dependency) {
            this(dependant, null, dependency);
        }

        public DependencyException(Key<? extends Component> dependant, Component component, Key<? extends Component> dependency) {
            super("Component " + (component != null ? component + " " : "")
                    + "for key " + dependant + " is missing dependency " + dependency);
            this.dependant = dependant;
            this.dependency = dependency;
        }

        public Key<? extends Component> getDependant() {
            return dependant;
        }

        public Key<? extends Component> getDependency() {
            return dependency;
        }
    }

    private static class UnmodifiableSet<E> implements Set<E>, Serializable {
        private transient Set<E> s;

        private UnmodifiableSet(Set<E> collection) {
            s = collection;
        }

        @Override
        public boolean add(E e) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean addAll(Collection<? extends E> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean contains(Object o) {
            return s.contains(o);
        }

        @Override
        public boolean containsAll(Collection<?> o) {
            return s.containsAll(o);
        }

        @Override
        public boolean isEmpty() {
            return s.isEmpty();
        }

        @Override
        public Iterator<E> iterator() {
            return new Iterator<E>() {
                Iterator<E> iterator = s.iterator();

                @Override
                public boolean hasNext() {
                    return iterator.hasNext();
                }

                @Override
                public E next() {
                    return iterator.next();
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }

        @Override
        public boolean remove(Object o) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int size() {
            return s.size();
        }

        @Override
        public Object[] toArray() {
            return s.toArray();
        }

        @Override
        public <T> T[] toArray(T[] array) {
            return s.toArray(array);
        }

        @Override
        public String toString() {
            return s.toString();
        }

        @Override
        public boolean equals(Object object) {
            return s.equals(object);
        }

        @Override
        public int hashCode() {
            return s.hashCode();
        }

        private void writeObject(ObjectOutputStream stream) throws IOException {
            stream.defaultWriteObject();
            stream.writeInt(s.size());
            for (E key : s) {
                stream.writeObject(key);
            }
        }

        @SuppressWarnings("unchecked")
        private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
            stream.defaultReadObject();
            int size = stream.readInt();
            s = new HashSet<>(size);
            for (int i = size; --i >= 0; ) {
                s.add((E) stream.readObject());
            }
        }
    }
}
