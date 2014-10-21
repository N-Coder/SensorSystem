package de.ncoder.sensorsystem.events;

import de.ncoder.sensorsystem.AbstractComponent;
import de.ncoder.sensorsystem.Container;
import de.ncoder.sensorsystem.events.event.Event;
import de.ncoder.typedmap.Key;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class EventManager extends AbstractComponent implements RemoteEventManager {
    private static final Logger log = LoggerFactory.getLogger(EventManager.class);

    public static final Key<EventManager> KEY = new Key<>(EventManager.class);

    @Override
    public void init(Container container) {
        super.init(container);
        if (log.isTraceEnabled()) {
            subscribe(new EventListener() {
                @Override
                public void handle(Event event) {
                    log.trace(event.toString());
                }
            });
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        listeners.clear();
    }

    // ------------------------------------------------------------------------

    private final List<EventListener> listeners = new CopyOnWriteArrayList<>();

    public void subscribe(EventListener listener) {
        listeners.add(listener);
    }

    public void unsubscribe(EventListener listener) {
        listeners.remove(listener);
    }

    public void publish(Event event) {
        for (EventListener listener : listeners) {
            try {
                listener.handle(event);

                // Dear compiler, please trust me, I know what I'm doing
                if (false) throw new IOException();
            } catch (IOException e) {
                log.warn("Could not send event to RemoteListener " + listener, e);
            }
        }
    }
}
