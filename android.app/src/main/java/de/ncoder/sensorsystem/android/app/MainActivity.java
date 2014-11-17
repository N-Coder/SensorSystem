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

package de.ncoder.sensorsystem.android.app;

import android.app.*;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import de.ncoder.sensorsystem.Component;
import de.ncoder.sensorsystem.Container;
import de.ncoder.sensorsystem.android.app.fragment.ComponentsFragment;
import de.ncoder.sensorsystem.android.app.fragment.EventLogFragment;
import de.ncoder.sensorsystem.android.app.fragment.PlaceholderFragment;
import de.ncoder.sensorsystem.android.app.fragment.SensorsFragment;
import de.ncoder.typedmap.Key;

import java.util.Locale;

public class MainActivity extends Activity implements ActionBar.TabListener {
    private static final String TAG = MainActivity.class.getName();

    private Intent serviceIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        onCreateTabs();

        serviceIntent = new Intent(this, SensorSystemService.class);
        startService(serviceIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_stop) {
            if (container != null) {
                container.shutdown();
                container = null;
            }
            return true;
        } else if (id == R.id.action_settings) {
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

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(TAG, "SensorSystem Service connected");
            container = (Container) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i(TAG, "SensorSystem Service disconnected");
            container = null;
            bound = false;
        }
    };

    @Override
    public void onResume() {
        super.onResume();

        Intent service = new Intent(this, SensorSystemService.class);
        if (bindService(service, connection, Context.BIND_ABOVE_CLIENT)) {
            bound = true;
        } else {
            Log.w(TAG, "SensorSystem Service bind failed");
        }
    }

    @Override
    public void onPause() {
        if (bound) {
            unbindService(connection);
            bound = false;
        }
        super.onPause();
    }

    // TABS -------------------------------------------------------------------

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    private void onCreateTabs() {
        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
        if (actionBar == null) {
            return;
        }
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the adapter that will return tab fragments
        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this)
            );
        }
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return ComponentsFragment.newInstance();
                case 1:
                    return SensorsFragment.newInstance();
                case 2:
                    return EventLogFragment.newInstance();
                default:
                    return PlaceholderFragment.newInstance(position, getPageTitle(position));
            }
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_components).toUpperCase(l);
                case 1:
                    return getString(R.string.title_sensors).toUpperCase(l);
                case 2:
                    return getString(R.string.title_event_log).toUpperCase(l);
            }
            return null;
        }
    }
}
