package de.ncoder.sensorsystem;

import de.ncoder.typedmap.Key;
import de.ncoder.typedmap.TypedMap;

import java.util.Collection;

public interface Container {
    <T extends Component> void register(Key<T> key, T actor);

    void unregister(Key<? extends Component> key);

    void unregister(Component component);

    <T extends Component> T get(Key<T> key);

    boolean isRegistered(Key<? extends Component> key);

    TypedMap<? extends Component> getData();

    Collection<Key<? extends Component>> getKeys();

    void shutdown();
}
