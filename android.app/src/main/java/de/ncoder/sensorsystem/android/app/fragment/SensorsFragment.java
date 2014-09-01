package de.ncoder.sensorsystem.android.app.fragment;

import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import de.ncoder.sensorsystem.android.app.R;
import de.ncoder.sensorsystem.manager.event.Event;
import de.ncoder.sensorsystem.manager.event.EventManager;
import de.ncoder.sensorsystem.manager.event.EventUtils;
import de.ncoder.sensorsystem.manager.event.ValueChangedEvent;
import de.ncoder.sensorsystem.sensor.Sensor;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;


public class SensorsFragment extends BoundFragment {
    public static SensorsFragment newInstance() {
        SensorsFragment fragment = new SensorsFragment();
        //Bundle args = new Bundle();
        //args.putInt(ARG_TAB_NUMBER, tabNumber);
        //args.putString(ARG_TITLE_TEXT, title.toString());
        //fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_sensors, container, false);
        ((ListView) rootView.findViewById(R.id.event_log)).setAdapter(eventsAdapter);
        return rootView;
    }

    // ADAPTER ----------------------------------------------------------------

    private final EventsAdapter eventsAdapter = new EventsAdapter();

    private class EventsAdapter extends BaseAdapter implements EventManager.Listener {
        private final List<String> index = new ArrayList<>();
        private final Map<String, Event> eventSet = new HashMap<>();

        private final DateFormat whenFormat = new SimpleDateFormat("HH:mm:ss");

        @Override
        public int getCount() {
            return index.size();
        }

        @Override
        public Event getItem(int position) {
            return eventSet.get(index.get(position));
        }

        @Override
        public long getItemId(int position) {
            return index.get(position).hashCode();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) getActivity()
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.entry_log_event, parent, false);
            }

            Event event = getItem(position);
            ((TextView) convertView.findViewById(R.id.event_title)).setText(
                    EventUtils.shortenName(event.getName()));
            if (event instanceof ValueChangedEvent<?>) {
                ((TextView) convertView.findViewById(R.id.event_content)).setText(
                        EventUtils.toString(((ValueChangedEvent) event).getNewValue()));
            } else {
                ((TextView) convertView.findViewById(R.id.event_content)).setText(
                        event.toString());
            }
            ((TextView) convertView.findViewById(R.id.event_when)).setText(
                    whenFormat.format(new Date(event.getWhen())));
            ((TextView) convertView.findViewById(R.id.event_source)).setText(
                    String.valueOf(event.getSource()));

            return convertView;
        }

        @Override
        public void handle(final Event event) {
            if (event.getSource() instanceof Sensor) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Event old = eventSet.put(event.getName(), event);
                        if (old == null) {
                            index.add(event.getName());
                        }
                        notifyDataSetChanged();
                    }
                });
            }
        }
    }

    // SERVICE ----------------------------------------------------------------

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        super.onServiceConnected(name, service);
        EventManager eventManager = getComponent(EventManager.KEY);
        if (eventManager != null) {
            eventManager.subscribe(eventsAdapter);
        } else {
            Log.w(getClass().getSimpleName(), "No EventManager available");
        }
    }

    @Override
    public void onPause() {
        EventManager eventManager = getComponent(EventManager.KEY);
        if (eventManager != null) {
            eventManager.unsubscribe(eventsAdapter);
        }
        super.onPause();
    }
}
