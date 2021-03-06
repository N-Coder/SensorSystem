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

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import de.ncoder.sensorsystem.Component;
import de.ncoder.sensorsystem.android.app.R;
import de.ncoder.sensorsystem.android.app.componentInfo.ComponentInfoActivity;
import de.ncoder.sensorsystem.android.app.componentInfo.ComponentInfoManager;
import de.ncoder.sensorsystem.events.EventListener;
import de.ncoder.sensorsystem.events.EventManager;
import de.ncoder.sensorsystem.events.event.ComponentEvent;
import de.ncoder.sensorsystem.events.event.Event;
import de.ncoder.typedmap.Key;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class ComponentsFragment extends BoundFragment {
    public static ComponentsFragment newInstance() {
        ComponentsFragment fragment = new ComponentsFragment();
        //Bundle args = new Bundle();
        //args.putInt(ARG_TAB_NUMBER, tabNumber);
        //args.putString(ARG_TITLE_TEXT, title.toString());
        //fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_components, container, false);
        ListView listView = (ListView) rootView.findViewById(R.id.components);
        listView.setAdapter(componentsAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Key key = componentsAdapter.index.get(position);
                Class<?> uiClass = ComponentInfoManager.getActivity(key);
                if (uiClass != null) {
                    Intent intent = new Intent(getActivity(), uiClass);
                    intent.putExtra(ComponentInfoActivity.EXTRA_KEY_CLASS, key.getValueClass().getName());
                    intent.putExtra(ComponentInfoActivity.EXTRA_KEY_IDENTIFIER, key.getIdentifier());
                    startActivity(intent);
                } else {
                    Toast.makeText(getActivity(), "No DetailActivity for this Component available",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
        return rootView;
    }

    // ADAPTER ----------------------------------------------------------------

    private final ComponentsAdapter componentsAdapter = new ComponentsAdapter();

    private class ComponentsAdapter extends BaseAdapter implements EventListener {
        private final List<Key> index = new ArrayList<>();

        @Override
        public int getCount() {
            return getContainer() != null ? index.size() : 0;
        }

        @Override
        public Component getItem(int position) {
            return getContainer() != null ? getContainer().get(index.get(position)) : null;
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
                convertView = inflater.inflate(R.layout.entry_component, parent, false);
            }

            Component component = getItem(position);
            if (component != null) {
                ((TextView) convertView.findViewById(R.id.component_title)).setText(
                        component.toString());
                ((TextView) convertView.findViewById(R.id.component_type)).setText(
                        component.getClass().getName());
            }

            return convertView;
        }

        private void updateIndex() {
            if (getContainer() != null) {
                index.clear();
                for (Key<? extends Component> key : getContainer().getKeys()) {
                    index.add(key);
                }
                sort();
                notifyDataSetChanged();
            }
        }

        private void sort() {
            Collections.sort(index, new Comparator<Key>() {
                @Override
                public int compare(Key lhs, Key rhs) {
                    int val = lhs.getValueClass().getName().compareTo(rhs.getValueClass().getName());
                    if (val == 0) {
                        val = lhs.getIdentifier().compareTo(rhs.getIdentifier());
                    }
                    return val;
                }
            });
        }

        @Override
        public void handle(final Event event) {
            if (event instanceof ComponentEvent) {
                if (((ComponentEvent) event).getType() == ComponentEvent.Type.ADDED) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            index.add(((ComponentEvent) event).getKey());
                            sort();
                            notifyDataSetChanged();
                        }
                    });
                } else if (((ComponentEvent) event).getType() == ComponentEvent.Type.REMOVED) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            index.remove(((ComponentEvent) event).getKey());
                            notifyDataSetChanged();
                        }
                    });
                }
            }
        }
    }

    // SERVICE ----------------------------------------------------------------

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        super.onServiceConnected(name, service);
        EventManager eventManager = getComponent(EventManager.KEY);
        if (eventManager != null) {
            eventManager.subscribe(componentsAdapter);
            componentsAdapter.updateIndex();
        } else {
            Log.w(getClass().getSimpleName(), "No EventManager available");
        }
    }

    @Override
    public void onPause() {
        EventManager eventManager = getComponent(EventManager.KEY);
        if (eventManager != null) {
            eventManager.unsubscribe(componentsAdapter);
        }
        super.onPause();
    }
}
