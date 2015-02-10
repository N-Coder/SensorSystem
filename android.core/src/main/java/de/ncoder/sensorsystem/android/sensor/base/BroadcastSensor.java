package de.ncoder.sensorsystem.android.sensor.base;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import java.util.Set;

import de.ncoder.sensorsystem.Component;
import de.ncoder.sensorsystem.Container;
import de.ncoder.sensorsystem.DependantComponent;
import de.ncoder.sensorsystem.android.ContainerService;
import de.ncoder.sensorsystem.manager.DataManager;
import de.ncoder.sensorsystem.sensor.CachedSensor;
import de.ncoder.typedmap.Key;

public abstract class BroadcastSensor<T> extends CachedSensor<T> implements DependantComponent {
	@Override
	public void init(Container container, Key<? extends Component> key) {
		super.init(container, key);
		getOtherComponent(ContainerService.KEY_CONTEXT)
				.registerReceiver(receiver, getBroadcastIntentFilter());
	}

	@Override
	public void destroy(Key<? extends Component> key) {
		getOtherComponent(ContainerService.KEY_CONTEXT).unregisterReceiver(receiver);
		super.destroy(key);
	}

	private final BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			onBroadcastReceive(context, intent);
		}
	};

	protected abstract IntentFilter getBroadcastIntentFilter();

	public abstract void onBroadcastReceive(Context context, Intent intent);

	private static Set<Key<? extends Component>> dependencies;

	@Override
	public Set<Key<? extends Component>> dependencies() {
		if (dependencies == null) {
			dependencies = DataManager.<Key<? extends Component>>wrapSet(ContainerService.KEY_CONTEXT);
		}
		return dependencies;
	}
}
