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

package de.ncoder.sensorsystem.android.app.componentInfo;

import android.content.ComponentName;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Html;
import android.util.Log;
import android.widget.TextView;

import de.ncoder.sensorsystem.android.app.R;
import de.ncoder.sensorsystem.android.logging.DBLogger;
import de.ncoder.sensorsystem.events.EventListener;
import de.ncoder.sensorsystem.events.EventManager;
import de.ncoder.sensorsystem.events.event.Event;

public class DBLoggerInfoActivity extends ComponentInfoActivity implements EventListener {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_component_dblogger);
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		super.onServiceConnected(name, service);
		updateText(0);
		EventManager eventManager = getComponent(EventManager.KEY);
		if (eventManager != null) {
			eventManager.subscribe(this);
		} else {
			Log.w(getClass().getSimpleName(), "No EventManager available");
		}
		if (getComponent(DBLogger.KEY) == null) {
			Log.w(getClass().getSimpleName(), "No DBLogger available");
		}
	}

	@Override
	public void onPause() {
		EventManager eventManager = getComponent(EventManager.KEY);
		if (eventManager != null) {
			eventManager.unsubscribe(this);
		}
		super.onPause();
	}

	@Override
	public void handle(Event event) {
		updateText(count + 1);
	}

	private int count;
	private long lastUpdate;

	private void updateText(int estimatedCount) {
		count = estimatedCount;
		runOnUiThread(updateText);
		if (System.currentTimeMillis() - lastUpdate > 200) {
			DBLogger dbLogger = getComponent(DBLogger.KEY);
			if (dbLogger != null) {
				count = dbLogger.getLogEntryCount();
				runOnUiThread(updateText);
				lastUpdate = System.currentTimeMillis();
			}
		}
	}

	private final Runnable updateText = new Runnable() {
		@Override
		public void run() {
			((TextView) findViewById(R.id.log_entries_count)).setText(
					Html.fromHtml(String.format(getString(R.string.text_log_entries_count), count))
			);
		}
	};
}
