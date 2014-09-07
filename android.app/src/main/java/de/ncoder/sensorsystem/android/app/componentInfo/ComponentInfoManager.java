package de.ncoder.sensorsystem.android.app.componentInfo;

import android.app.Activity;
import android.util.Log;
import de.ncoder.sensorsystem.Key;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ComponentInfoManager {
    private static final Map<Key<?>, Class<? extends Activity>> componentUIs = new HashMap<>();

    public static Class<? extends Activity> getActivity(Key<?> key) {
        return componentUIs.get(key);
    }

    public static Class<? extends Activity> setActivity(Key<?> key, Class<? extends Activity> value) {
        return componentUIs.put(key, value);
    }

    public static Class<? extends Activity> unsetActivity(Key<?> key) {
        return componentUIs.remove(key);
    }

    public static boolean hasActivity(Key<?> key) {
        return componentUIs.containsKey(key);
    }

    // ------------------------------------------------------------------------

    private static final String TAG_COMPONENT = "component";
    private static final String ATTR_CLASS = "class";
    private static final String ATTR_IDENTIFIER = "name";
    private static final String ATTR_UI = "ui";

    @SuppressWarnings("unchecked")
    public static void parseResources(XmlPullParser parser) throws XmlPullParserException, IOException {
        for (int event = parser.getEventType(); event != XmlPullParser.END_DOCUMENT; event = parser.next()) {
            if (event == XmlPullParser.START_TAG && TAG_COMPONENT.equals(parser.getName())) {
                String clazz = parser.getAttributeValue(null, ATTR_CLASS);
                String name = parser.getAttributeValue(null, ATTR_IDENTIFIER);
                String ui = parser.getAttributeValue(null, ATTR_UI);

                if (clazz != null && ui != null) {
                    clazz = clazz.trim();
                    ui = ui.trim();
                    if (name != null) {
                        name = name.trim();
                    }

                    try {
                        setActivity(Key.forName(clazz, name), (Class<? extends Activity>) Class.forName(ui));
                    } catch (ClassCastException | ClassNotFoundException e) {
                        Log.w(ComponentInfoManager.class.getSimpleName(), "Illegal class or ui attribute at " + parser.getPositionDescription());
                    }
                } else {
                    Log.w(ComponentInfoManager.class.getSimpleName(), "Tag at " + parser.getPositionDescription() + " missing class or ui attribute");
                }
            }
        }
    }
}
