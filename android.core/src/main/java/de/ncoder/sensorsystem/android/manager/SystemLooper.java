package de.ncoder.sensorsystem.android.manager;

import android.os.Looper;
import de.ncoder.sensorsystem.Component;
import de.ncoder.sensorsystem.Container;

public class SystemLooper extends Thread implements Component {
    public static final Container.Key<SystemLooper> KEY = new Container.Key<>(SystemLooper.class);

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
        Looper.loop();
    }

    public Looper getLooper() {
        return looper;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void destroy() {
        container = null;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
