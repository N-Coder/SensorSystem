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

package de.ncoder.sensorsystem.android.logging;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.Set;

import de.ncoder.sensorsystem.AbstractComponent;
import de.ncoder.sensorsystem.Component;
import de.ncoder.sensorsystem.Container;
import de.ncoder.sensorsystem.DependantComponent;
import de.ncoder.sensorsystem.android.ContainerService;
import de.ncoder.sensorsystem.events.EventListener;
import de.ncoder.sensorsystem.events.EventManager;
import de.ncoder.sensorsystem.events.event.Event;
import de.ncoder.sensorsystem.events.event.ValueChangedEvent;
import de.ncoder.typedmap.Key;

public class DBLogger extends AbstractComponent implements EventListener, DependantComponent {
    public static final Key<DBLogger> KEY = new Key<>(DBLogger.class);

    private DBHelper dbHelper;
    private SQLiteDatabase database;

    @Override
    public void init(Container container) {
        dbHelper = new DBHelper(getOtherComponent(ContainerService.KEY_CONTEXT));
        super.init(container);
        database = dbHelper.getWritableDatabase();
        EventManager eventManager = getOtherComponent(EventManager.KEY);
        if (eventManager != null) {
            eventManager.subscribe(this);
        } else {
            Log.w(getClass().getSimpleName(), "DBLogger makes no sense if EventManger is not available");
        }
    }

    @Override
    public void destroy() {
        EventManager eventManager = getOtherComponent(EventManager.KEY);
        if (eventManager != null) {
            eventManager.unsubscribe(this);
        }
        dbHelper.close();
        super.destroy();
    }

    @Override
    public void handle(Event event) {
        if (event instanceof ValueChangedEvent) {
            ContentValues values = new ContentValues();
            values.put(DBHelper.COLUMN_SOURCE, String.valueOf(event.getSource()));
            values.put(DBHelper.COLUMN_KEY, event.getName());
            values.put(DBHelper.COLUMN_TIMESTAMP, event.getWhen());
            values.put(DBHelper.COLUMN_VALUE, JSONUtils.toJSONString(((ValueChangedEvent) event).getNewValue()));
            long insertId = database.insert(DBHelper.TABLE_LOG, null, values);
            if (insertId < 0) {
                Log.w(getClass().getSimpleName(), "Logging event failed: " + insertId);
            }
        }
    }

    public int getLogEntryCount() {
        return dbHelper.getLogEntryCount();
    }

    private static Set<Key<? extends Component>> dependencies;

    @Override
    public Set<Key<? extends Component>> dependencies() {
        if (dependencies == null) {
            dependencies = wrapSet(ContainerService.KEY_CONTEXT, EventManager.KEY);
        }
        return dependencies;
    }
}
