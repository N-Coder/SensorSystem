package de.ncoder.sensorsystem.android.app.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import de.ncoder.sensorsystem.android.app.R;

public class PlaceholderFragment extends Fragment {
    private static final String ARG_TAB_NUMBER = "tab_number";
    private static final String ARG_TITLE_TEXT = "title_text";

    public static PlaceholderFragment newInstance(int tabNumber, CharSequence title) {
        PlaceholderFragment fragment = new PlaceholderFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TAB_NUMBER, tabNumber);
        args.putString(ARG_TITLE_TEXT, title.toString());
        fragment.setArguments(args);
        return fragment;
    }

    public PlaceholderFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_placeholder, container, false);
        Bundle args = getArguments();
        ((TextView) rootView.findViewById(R.id.section_label)).setText(args.getString(ARG_TITLE_TEXT) + " [" + args.getInt(ARG_TAB_NUMBER) + "]");
        return rootView;
    }
}
