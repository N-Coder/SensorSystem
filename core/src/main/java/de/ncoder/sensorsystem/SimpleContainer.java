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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import de.ncoder.sensorsystem.events.EventManager;
import de.ncoder.sensorsystem.events.event.ComponentEvent;
import de.ncoder.sensorsystem.events.event.Event;
import de.ncoder.sensorsystem.events.event.SimpleEvent;
import de.ncoder.sensorsystem.remote.RemoteContainer;
import de.ncoder.typedmap.Key;
import de.ncoder.typedmap.TypedMap;

public class SimpleContainer implements Container, RemoteContainer {
	public static final String TAG_PRE_SHUTDOWN = Container.class.getName() + ".PRE_SHUTDOWN";

	private boolean checkDependencies;
	private final TypedMap<Component> components = new TypedMap<>(new ConcurrentHashMap<Key<? extends Component>, Component>());
	private final List<Key<? extends Component>> log = new LinkedList<>();

	public SimpleContainer() {
		checkDependencies = true;
		try {
			assert false;
			checkDependencies = false;
		} catch (AssertionError ignore) {
		}
	}

	@Override
	public synchronized <T extends Component, V extends T> void register(Key<T> key, V component) {
		Objects.requireNonNull(key, "key");
		Objects.requireNonNull(component, "component");
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
		try {
			components.putTyped(key, component);
			component.init(this, key);
			log.add(key);
			publish(new ComponentEvent(key, ComponentEvent.Type.ADDED));
		} catch (RuntimeException e) {
			components.remove(key);
			throw e;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public synchronized void unregister(Key<?> keyUnchecked) {
		if (Component.class.isAssignableFrom(keyUnchecked.getValueClass())) {
			Key<? extends Component> key = (Key<? extends Component>) keyUnchecked;
			checkRemove(key);
			Component component = components.remove(key);
			if (component != null) {
				component.destroy(key);
				publish(new ComponentEvent(key, ComponentEvent.Type.REMOVED));
			}
		} else {
			components.remove(keyUnchecked);
		}
	}

	@Override
	public synchronized void unregister(Component component) {
		Iterator<Map.Entry<Key<? extends Component>, Component>> it = components.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<Key<? extends Component>, Component> entry = it.next();
			if (entry.getValue() == component) {
				Key<? extends Component> key = entry.getKey();
				checkRemove(key);
				it.remove();
				component.destroy(key);
				publish(new ComponentEvent(key, ComponentEvent.Type.REMOVED));
			}
		}
	}

	private void checkRemove(Key<? extends Component> key) {
		if (checkDependencies) {
			for (Map.Entry<Key<? extends Component>, Component> entry : components.entrySet()) {
				if (entry.getValue() instanceof DependantComponent
						&& ((DependantComponent) entry.getValue()).dependencies().contains(key)) {
					throw new DependencyException(entry.getKey(), entry.getValue(), key);
				}
			}
		}
	}

	@Override
	public <T extends Component> T get(Key<T> key) {
		return components.get(key);
	}

	@Override
	public boolean isRegistered(Key<?> key) {
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

	public Set<String> getPermissions() {
		Set<String> permissions = new HashSet<>();
		for (Component component : components.values()) {
			if (component instanceof PrivilegedComponent) {
				permissions.addAll(((PrivilegedComponent) component).requiredPermissions());
			}
		}
		return permissions;
	}

	// ------------------------------------------------------------------------

	@Override
	public void shutdown() {
		publish(new SimpleEvent(TAG_PRE_SHUTDOWN, null));
		ListIterator<Key<? extends Component>> it = log.listIterator(log.size());
		while (it.hasPrevious()) {
			Key<? extends Component> key = it.previous();
			it.remove();
			Component component = components.remove(key);
			if (component != null) {
				component.destroy(key);
				publish(new ComponentEvent(key, ComponentEvent.Type.REMOVED));
			}
		}
		log.clear();
		assert components.isEmpty() : "Not all components were removed: " + components;
	}

	private void publish(Event event) {
		EventManager manager = get(EventManager.KEY);
		if (manager != null) {
			manager.publish(event);
		}
	}

	// ------------------------------------------------------------------------

	public static class DependencyException extends IllegalStateException {
		private final Key<? extends Component> dependant, dependency;

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
