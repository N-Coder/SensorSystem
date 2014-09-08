package de.ncoder.sensorsystem;

import de.ncoder.typedmap.Key;

import java.util.Map;
import java.util.Set;

public interface Container {
    <T extends Component> void register(Key<T> key, T actor);

    void unregister(Key<? extends Component> key);

    <T extends Component> T get(Key<T> key);

    boolean isRegistered(Key<? extends Component> key);

    Set<Map.Entry<Key<? extends Component>, Component>> entrySet();

    void shutdown();

}
