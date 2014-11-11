package de.ncoder.sensorsystem.android.manager;

import android.os.Looper;
import de.ncoder.sensorsystem.Component;
import de.ncoder.sensorsystem.Container;
import de.ncoder.sensorsystem.events.EventManager;
import de.ncoder.sensorsystem.events.event.ComponentEvent;
import de.ncoder.sensorsystem.events.event.Event;
import de.ncoder.typedmap.Key;

public class SystemLooper extends Thread implements Component {
    public static final Key<SystemLooper> KEY = new Key<>(SystemLooper.class);

    private Container container;
    private Looper looper;

    @Override
    public void init(Container container) {
        this.container = container;
        this.start();
    }

    @Override
    public void run() {
        Looper.prepare();
        looper = Looper.myLooper();
        publish(new ComponentEvent(this, ComponentEvent.Type.STARTED));
        Looper.loop();
        looper = null;
        publish(new ComponentEvent(this, ComponentEvent.Type.STOPPED));
        if (container != null) {
            container.unregister(this);
        }
    }

    private void publish(Event event) {
        if (container != null) {
            EventManager eventManager = container.get(EventManager.KEY);
            if (eventManager != null) {
                eventManager.publish(event);
            }
        }
    }

    public Looper getLooper() {
        return looper;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void destroy() {
        if (looper != null) {
            looper.quit();
        }
        container = null;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
