/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Niko Fink
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package de.ncoder.sensorsystem.android;

import android.app.Service;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.IBinder;

import java.util.Collection;

import javax.annotation.Nonnull;

import de.ncoder.sensorsystem.Component;
import de.ncoder.sensorsystem.Container;
import de.ncoder.sensorsystem.SimpleContainer;
import de.ncoder.typedmap.Key;
import de.ncoder.typedmap.TypedMap;

public class ContainerService extends Service implements Container {
	public static final Key<ContextComponent> KEY_CONTEXT = new Key<>(ContextComponent.class, "ContainerContext");

	@Nonnull
	private final Container container;

	public ContainerService() {
		SimpleContainer simpleContainer = new SimpleContainer();
		simpleContainer.setCheckDependencies(BuildConfig.DEBUG);
		container = simpleContainer;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		container.register(KEY_CONTEXT, new ContextComponent(this));
	}

	@Override
	public void onDestroy() {
		container.shutdown();
		super.onDestroy();
	}

	@Nonnull
	@Override
	public IBinder onBind(Intent intent) {
		return theBinder;
	}

	private final Binder theBinder = new Binder();

	public class Binder extends android.os.Binder implements Container {
		public <T extends Component, V extends T> void register(@Nonnull Key<T> key, @Nonnull V component) {
			container.register(key, component);
		}

		public <T extends Component> T get(@Nonnull Key<T> key) {
			return container.get(key);
		}

		@Nonnull
		@Override
		public <T extends Component> T require(@Nonnull Key<T> key) throws SimpleContainer.DependencyException {
			return container.require(key);
		}

		public void unregister(@Nonnull Key<?> key) {
			container.unregister(key);
		}

		@Override
		public void unregister(@Nonnull Component component) {
			container.unregister(component);
		}

		public boolean isRegistered(@Nonnull Key<?> key) {
			return container.isRegistered(key);
		}

		public void shutdown() {
			container.shutdown();
			stopSelf();
		}

		@Nonnull
		public TypedMap<? extends Component> getData() {
			return container.getData();
		}

		@Nonnull
		@Override
		public Collection<Key<? extends Component>> getKeys() {
			return container.getKeys();
		}
	}

	public static class ContextComponent extends ContextWrapper implements Component {
		public ContextComponent(Context base) {
			super(base);
		}

		@Override
		public void init(@Nonnull Container container, @Nonnull Key<? extends Component> key) {
		}

		@Override
		public void destroy(Key<? extends Component> key) {
		}

		@Override
		public String toString() {
			return getClass().getSimpleName();
		}
	}

	@Nonnull
	public Container getContainer() {
		return container;
	}

	@Override
	public <T extends Component, V extends T> void register(@Nonnull Key<T> key, @Nonnull V component) {
		container.register(key, component);
	}

	public void registerService(@Nonnull Key<? extends Component> key,
	                            @Nonnull Class<? extends Service> component) {
		ServiceToComponentBridge.startService(this, this.getClass(), key, component);
	}

	@Override
	public void unregister(@Nonnull Key<?> key) {
		container.unregister(key);
	}

	@Override
	public void unregister(@Nonnull Component component) {
		container.unregister(component);
	}

	@Override
	public <T extends Component> T get(@Nonnull Key<T> key) {
		return container.get(key);
	}

	@Nonnull
	@Override
	public <T extends Component> T require(@Nonnull Key<T> key) throws SimpleContainer.DependencyException {
		return container.require(key);
	}

	@Override
	public boolean isRegistered(@Nonnull Key<?> key) {
		return container.isRegistered(key);
	}

	@Nonnull
	@Override
	public TypedMap<? extends Component> getData() {
		return container.getData();
	}

	@Nonnull
	@Override
	public Collection<Key<? extends Component>> getKeys() {
		return container.getKeys();
	}

	@Override
	public void shutdown() {
		container.shutdown();
	}
}
