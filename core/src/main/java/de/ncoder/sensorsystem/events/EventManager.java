package de.ncoder.sensorsystem.events;

import de.ncoder.sensorsystem.AbstractComponent;
import de.ncoder.sensorsystem.Container;
import de.ncoder.sensorsystem.events.event.Event;
import de.ncoder.typedmap.Key;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class EventManager extends AbstractComponent implements RemoteEventManager {
    private static final Logger log = LoggerFactory.getLogger(EventManager.class);

    public static final Key<EventManager> KEY = new Key<>(EventManager.class);

    @Override
    public void init(Container container) {
        super.init(container);
        if (log.isTraceEnabled()) {
            subscribe(new Listener() {
                @Override
                public void handle(Event event) throws RemoteException {
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

    private final List<Listener> listeners = new CopyOnWriteArrayList<>();

    public void subscribe(Listener listener) {
        listeners.add(listener);
    }

    public void unsubscribe(Listener listener) {
        listeners.remove(listener);
    }

    public void publish(Event event) {
        for (Listener listener : listeners) {
            try {
                listener.handle(event);
            } catch (RemoteException e) {
                log.warn("Could not send event to RemoteListener " + listener, e);
            }
        }
    }

    // ------------------------------------------------------------------------

    public static interface Listener extends Remote {
        void handle(Event event) throws RemoteException;
    }
}
