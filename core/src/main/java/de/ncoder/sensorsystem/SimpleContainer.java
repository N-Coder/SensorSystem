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
import de.ncoder.sensorsystem.events.event.ComponentEvent;
import de.ncoder.sensorsystem.events.event.Event;
import de.ncoder.sensorsystem.events.event.SimpleEvent;
import de.ncoder.sensorsystem.manager.ThreadPoolManager;
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
    public synchronized <T extends Component> void register(Key<T> key, T component) {
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
        publish(new ComponentEvent(key, component, ComponentEvent.Type.ADDED));
    }

    @Override
    public synchronized void unregister(Key<? extends Component> key) {
        checkRemove(key);
        Component component = components.remove(key);
        if (component != null) {
            component.destroy();
            publish(new ComponentEvent(key, component, ComponentEvent.Type.REMOVED));
        }
    }

    @Override
    public synchronized void unregister(Component component) {
        boolean removed = false;
        Iterator<Map.Entry<Key<? extends Component>, Component>> it = components.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Key<? extends Component>, Component> entry = it.next();
            if (entry.getValue() == component) {
                checkRemove(entry.getKey());
                it.remove();
                publish(new ComponentEvent(entry.getKey(), component, ComponentEvent.Type.REMOVED));
                removed = true;
            }
        }
        if (removed) {
            component.destroy();
        }
    }

    private void checkRemove(Key<? extends Component> key) {
        if (checkDependencies) {
            for (Map.Entry<Key<? extends Component>, Component> entry : components.entrySet()) {
                if (entry.getValue() instanceof DependantComponent
                        && ((DependantComponent) entry.getValue()).dependencies().contains(key)) {
                    throw new DependencyException(entry.getKey(), key);
                }
            }
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
        publish(new PreShutdownEvent());
        List<Map.Entry<Key<? extends Component>, Component>> entries = new ArrayList<>(components.entrySet());
        Collections.sort(entries, new Comparator<Map.Entry<Key<? extends Component>, Component>>() {
            @Override
            public int compare(Map.Entry<Key<? extends Component>, Component> o1, Map.Entry<Key<? extends Component>, Component> o2) {
                Key<? extends Component> k1 = o1.getKey();
                Key<? extends Component> k2 = o2.getKey();
                return Integer.signum(importance(k1) - importance(k2));
            }

            private int importance(Key<? extends Component> key) {
                if (key == EventManager.KEY) {
                    return 100;
                } else if (key == ThreadPoolManager.KEY) {
                    return 90;
                } else if (key.getValueClass().getName().startsWith(SimpleContainer.class.getPackage().getName())) {
                    return 10;
                } else {
                    return 0;
                }
            }
        });
        for (Map.Entry<Key<? extends Component>, Component> entry : entries) {
            components.remove(entry.getKey());
            entry.getValue().destroy();
            publish(new ComponentEvent(entry.getKey(), entry.getValue(), ComponentEvent.Type.REMOVED));
        }
    }

    private void publish(Event event) {
        EventManager manager = get(EventManager.KEY);
        if (manager != null) {
            manager.publish(event);
        }
    }

    // ------------------------------------------------------------------------

    public static class PreShutdownEvent extends SimpleEvent<Component> {
        public static final String NAME = Container.class.getName() + ".PRE_SHUTDOWN";

        public PreShutdownEvent() {
            super(NAME, null);
        }
    }

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
