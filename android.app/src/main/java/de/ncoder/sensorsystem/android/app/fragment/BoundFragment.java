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

package de.ncoder.sensorsystem.android.app.fragment;

import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import de.ncoder.sensorsystem.Component;
import de.ncoder.sensorsystem.Container;
import de.ncoder.sensorsystem.android.app.SensorSystemService;
import de.ncoder.typedmap.Key;

public class BoundFragment extends Fragment implements ServiceConnection {
	// CONTAINER --------------------------------------------------------------

	@Nullable
	private Container container;

	@Nullable
	protected Container getContainer() {
		return container;
	}

	@Nullable
	protected <T extends Component> T getComponent(Key<T> key) {
		if (getContainer() != null) {
			return getContainer().get(key);
		} else {
			return null;
		}
	}

	// SERVICE ----------------------------------------------------------------

	private boolean bound = false;

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		Log.i(getClass().getSimpleName(), "SensorSystem Service connected");
		container = (Container) service;
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		Log.i(getClass().getSimpleName(), "SensorSystem Service disconnected");
		container = null;
	}

	@Override
	public void onResume() {
		super.onResume();

		Intent service = new Intent(getActivity(), SensorSystemService.class);
		if (getActivity().bindService(service, this, Context.BIND_ABOVE_CLIENT)) {
			bound = true;
		} else {
			Log.w(getClass().getSimpleName(), "SensorSystem Service bind failed");
		}
	}

	@Override
	public void onPause() {
		if (bound) {
			getActivity().unbindService(this);
			bound = false;
		}
		super.onPause();
	}
}
