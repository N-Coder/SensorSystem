package de.ncoder.sensorsystem.android.app.componentInfo;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import de.ncoder.sensorsystem.Component;
import de.ncoder.sensorsystem.Container;
import de.ncoder.sensorsystem.android.app.R;
import de.ncoder.sensorsystem.android.app.SensorSystemService;
import de.ncoder.typedmap.Key;

public class ComponentInfoActivity extends Activity implements ServiceConnection {
    public static final String EXTRA_KEY_CLASS = Key.class.getName() + ".class";
    public static final String EXTRA_KEY_IDENTIFIER = Key.class.getName() + ".identifier";

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.component_info, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

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

        Intent service = new Intent(this, SensorSystemService.class);
        if (bindService(service, this, Context.BIND_ABOVE_CLIENT)) {
            bound = true;
        } else {
            Log.w(getClass().getSimpleName(), "SensorSystem Service bind failed");
        }
    }

    @Override
    public void onPause() {
        if (bound) {
            unbindService(this);
            bound = false;
        }
        super.onPause();
    }
}

