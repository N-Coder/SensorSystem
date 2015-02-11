package de.ncoder.sensorsystem.android;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import de.ncoder.sensorsystem.Component;
import de.ncoder.sensorsystem.Container;
import de.ncoder.typedmap.Key;

public class ServiceToComponentBridge<S extends Service, C extends Component> {
	private final String TAG = "ServiceToComponentBridge[" + getClass().getSimpleName() + "]";

	public static final String EXTRA_CONTAINER_SERVICE_INTENT = ServiceToComponentBridge.class.getName() + ".ContainerService";
	public static final String EXTRA_COMPONENT_KEY = ServiceToComponentBridge.class.getName() + ".ComponentKey";

	// START UTILITY ----------------------------------------------------------

	/**
	 * Adds a new Component of type {@code serviceComponent} with Key {@code componentKey}
	 * to the Container identified by {@code containerService} running in the Context {@code context}
	 */
	public static void startService(Context context,
	                                Intent containerService,
	                                Key<? extends Component> componentKey,
	                                Class<? extends Service> serviceComponent) {
		Intent intent = new Intent(context, serviceComponent);
		intent.putExtra(EXTRA_CONTAINER_SERVICE_INTENT, containerService);
		intent.putExtra(EXTRA_COMPONENT_KEY, componentKey);
		context.startService(intent);
	}

	/**
	 * Adds a new Component of type {@code serviceComponent} with Key {@code componentKey}
	 * to the Container identified by {@code containerService} running in the Context {@code context}
	 */
	public static void startService(Context context,
	                                Class<? extends Service> containerService,
	                                Key<? extends Component> componentKey,
	                                Class<? extends Service> serviceComponent) {
		startService(context, new Intent(context, containerService), componentKey, serviceComponent);
	}

	// LIFECYCLE --------------------------------------------------------------

	public boolean valid = false;
	private boolean unbinding = false;
	private final S service;
	private final C component;
	private Container container;
	private ComponentName containerService;

	public ServiceToComponentBridge(S service, C component) {
		this.service = service;
		this.component = component;
	}

	public void unbind() {
		Log.v(TAG, "unbind() called");
		if (!unbinding) {
			unbinding = true;
			try {
				doUnbind();
			} finally {
				unbinding = false;
			}
		}
	}

	private void doUnbind() {
		valid = false;
		if (container != null) {
			container.unregister(component); //calls component.destroy();
		}
		if (bound) {
			getContext().unbindService(containerConnection);
			bound = false;
		}
		service.stopSelf(); //calls service.onDestroy();
	}

	// BINDING ----------------------------------------------------------------

	private boolean bound = false;
	private Key key;

	public void bindToContainer(Intent containerService, Key key) { //called by service.onBind/onStartCommand
		if (bound) {
			if (this.containerService == null
					|| !this.containerService.equals(containerService.getComponent())
					|| !this.key.equals(key)) {
				throw new IllegalStateException("Binding via an already bound bridge! " +
						"Current [" + this.containerService + ", " + this.key + "], " +
						"Requested [" + containerService + ", " + key + "]");
			} else {
				return;
			}
		}
		if (containerService == null) {
			throw new IllegalArgumentException("Can't start ServiceComponent without EXTRA_CONTAINER_BIND_INTENT");
		}
		if (key == null) {
			throw new IllegalArgumentException("Can't start ServiceComponent without EXTRA_COMPONENT_KEY");
		} else if (!key.isPossibleValue(component)) {
			throw new IllegalArgumentException("EXTRA_COMPONENT_KEY " + key + " is not a valid Key for " + component);
		}
		this.key = key;
		Log.v(TAG, "bindToContainer(" + containerService + ", " + key + ") called");
		if (getContext().bindService(containerService, containerConnection, 0)) {
			bound = true;
			Log.v(TAG, "bindService(" + containerService + ") successful");
		} else {
			throw new IllegalStateException("Can't bind to Container Service " + containerService);
		}
	}

	public void bindToContainer(Bundle data) {
		Intent containerService = data.getParcelable(EXTRA_CONTAINER_SERVICE_INTENT);
		Key key = (Key) data.getSerializable(EXTRA_COMPONENT_KEY);
		bindToContainer(containerService, key);
	}

	private final ServiceConnection containerConnection = new ServiceConnection() {
		@SuppressWarnings("unchecked")
		@Override
		public void onServiceConnected(ComponentName name, IBinder binder) {
			Log.v(TAG, "onServiceConnected(" + name + ", " + binder + ") called");
			containerService = name;
			container = (Container) binder;
			container.register(key, component); //calls component.bind()
			valid = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			Log.v(TAG, "onServiceDisconnected() called");
			bound = false;
			unbind();
		}
	};

	// ABSTRACT COMPONENT -----------------------------------------------------

	public boolean isActive() {
		return valid;
	}

	public Container getContainer() {
		return container;
	}

	public S getService() {
		return service;
	}

	public C getComponent() {
		return component;
	}

	private Context getContext() {
		return getService().getBaseContext();
	}
}
