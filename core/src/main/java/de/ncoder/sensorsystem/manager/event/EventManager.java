package de.ncoder.sensorsystem.manager.event;

import de.ncoder.sensorsystem.AbstractComponent;
import de.ncoder.typedmap.Key;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class EventManager extends AbstractComponent {
    public static final Key<EventManager> KEY = new Key<>(EventManager.class);

    @Override
    public void destroy() {
        super.destroy();
        listeners.clear();
    }

    // ------------------------------------------------------------------------

    private final List<Listener> listeners = new CopyOnWriteArrayList<>();

    public void subscribe(Listener listener) {
        listeners.add(listener);
    }

    public void unsubscribe(Listener listener) {
        listeners.remove(listener);
    }

    public void publish(Event event) {
        for (Listener listener : listeners) {
            listener.handle(event);
        }
    }

    // ------------------------------------------------------------------------

    public static interface Listener {
        public void handle(Event event);
    }
}
