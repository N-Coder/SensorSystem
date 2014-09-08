package de.ncoder.sensorsystem;

import de.ncoder.typedmap.Key;
import de.ncoder.typedmap.TypedMap;

public interface Container {
    <T extends Component> void register(Key<T> key, T actor);

    void unregister(Key<? extends Component> key);

    <T extends Component> T get(Key<T> key);

    boolean isRegistered(Key<? extends Component> key);

    TypedMap<Component> getData();

    void shutdown();

}
