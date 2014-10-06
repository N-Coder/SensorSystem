package de.ncoder.sensorsystem.android.app.componentInfo;

import android.content.ComponentName;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Html;
import android.util.Log;
import android.widget.TextView;
import de.ncoder.sensorsystem.android.app.R;
import de.ncoder.sensorsystem.android.logging.DBLogger;
import de.ncoder.sensorsystem.events.EventManager;
import de.ncoder.sensorsystem.events.event.Event;

public class DBLoggerInfoActivity extends ComponentInfoActivity implements EventManager.Listener {
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
