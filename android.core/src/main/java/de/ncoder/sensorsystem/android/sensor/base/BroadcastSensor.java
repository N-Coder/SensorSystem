package de.ncoder.sensorsystem.android.sensor.base;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import de.ncoder.sensorsystem.Container;
import de.ncoder.sensorsystem.android.ContainerService;
import de.ncoder.sensorsystem.sensor.CachedSensor;

public abstract class BroadcastSensor<T> extends CachedSensor<T> {
    @Override
    public void init(Container container) {
        super.init(container);
        getOtherComponent(ContainerService.KEY_CONTEXT)
                .registerReceiver(receiver, getBroadcastIntentFilter());
    }

    @Override
    public void destroy() {
        getOtherComponent(ContainerService.KEY_CONTEXT).unregisterReceiver(receiver);
        super.destroy();
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            onBroadcastReceive(context, intent);
        }
    };

    protected abstract IntentFilter getBroadcastIntentFilter();

    public abstract void onBroadcastReceive(Context context, Intent intent);
}
