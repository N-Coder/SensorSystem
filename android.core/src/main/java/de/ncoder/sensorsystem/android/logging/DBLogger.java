package de.ncoder.sensorsystem.android.logging;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import de.ncoder.sensorsystem.AbstractComponent;
import de.ncoder.sensorsystem.Container;
import de.ncoder.sensorsystem.Key;
import de.ncoder.sensorsystem.manager.event.Event;
import de.ncoder.sensorsystem.manager.event.EventManager;
import de.ncoder.sensorsystem.manager.event.ValueChangedEvent;

public class DBLogger extends AbstractComponent implements EventManager.Listener {
    public static final Key<DBLogger> KEY = new Key<>(DBLogger.class);

    private final DBHelper dbHelper;
    private SQLiteDatabase database;

    public DBLogger(Context context) {
        dbHelper = new DBHelper(context);
    }

    @Override
    public void init(Container container) {
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
            values.put(DBHelper.COLUMN_VALUE, String.valueOf(((ValueChangedEvent) event).getNewValue()));
            long insertId = database.insert(DBHelper.TABLE_LOG, null, values);
            if (insertId < 0) {
                Log.w(getClass().getSimpleName(), "Logging event failed: " + insertId);
            }
        }
    }

    public int getLogEntryCount() {
        return dbHelper.getLogEntryCount();
    }
}
