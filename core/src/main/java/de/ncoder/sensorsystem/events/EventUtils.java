package de.ncoder.sensorsystem.events;

import java.util.Arrays;

public class EventUtils {
    private EventUtils() {
    }

    public static String toString(Object o) {
        if (o instanceof Object[])
            return Arrays.toString((Object[]) o);
        else if (o instanceof byte[])
            return Arrays.toString((byte[]) o);
        else if (o instanceof short[])
            return Arrays.toString((short[]) o);
        else if (o instanceof int[])
            return Arrays.toString((int[]) o);
        else if (o instanceof long[])
            return Arrays.toString((long[]) o);
        else if (o instanceof char[])
            return Arrays.toString((char[]) o);
        else if (o instanceof float[])
            return Arrays.toString((float[]) o);
        else if (o instanceof double[])
            return Arrays.toString((double[]) o);
        else if (o instanceof boolean[])
            return Arrays.toString((boolean[]) o);
        else
            return String.valueOf(o);
    }

    public static String shortenName(String name) {
        int i = name.lastIndexOf('.');
        if (i >= 0 && i <= name.length() - 1) {
            name = name.substring(i + 1);
        }
        return name;
    }
}
