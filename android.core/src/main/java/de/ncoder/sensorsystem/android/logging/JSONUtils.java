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

import android.location.Location;
import android.os.Build;
import android.os.Bundle;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class JSONUtils {
	public static String toJSONString(Object o) {
		return wrap(o).toString();
	}

	public static Object wrap(Object o) {
		if (o == null) {
			return JSONObject.NULL;
		} else if (o instanceof JSONArray || o instanceof JSONObject) {
			return o;
		} else if (o.equals(JSONObject.NULL)) {
			return o;
		} else if (o instanceof Boolean ||
				o instanceof Byte ||
				o instanceof Character ||
				o instanceof Double ||
				o instanceof Float ||
				o instanceof Integer ||
				o instanceof Long ||
				o instanceof Short) {
			return o;
		} else if (o instanceof CharSequence) {
			return JSONObject.quote(o.toString());
		} else if (o instanceof Collection) {
			return new JSONArray((Collection) o);
		} else if (o.getClass().isArray()) {
			final int length = Array.getLength(o);
			List<Object> values = new ArrayList<>(length);
			for (int i = 0; i < length; ++i) {
				values.add(wrap(Array.get(o, i)));
			}
			return new JSONArray(values);
		} else if (o instanceof Map) {
			return new JSONObject((Map) o);
		} else if (o instanceof Location) {
			return wrapLocation((Location) o);
		} else if (o instanceof Bundle) {
			return wrapBundle((Bundle) o);
		}
		return o.toString();
	}

	private static Object wrapLocation(Location loc) {
		try {
			JSONObject json = new JSONObject();
			json.put("provider", loc.getProvider());
			json.put("latitude", loc.getLatitude());
			json.put("longitude", loc.getLongitude());
			if (loc.hasAccuracy()) json.put("accuracy", loc.getAccuracy());
			json.put("time", loc.getTime());
			if (loc.hasAltitude()) json.put("alt", loc.getAltitude());
			if (loc.hasSpeed()) json.put("vel", loc.getSpeed());
			if (loc.hasBearing()) json.put("bear", loc.getBearing());
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
				if (loc.isFromMockProvider()) json.put("mock", true);
			if (loc.getExtras() != null) {
				json.put("extra", wrap(loc.getExtras()));
			}
			return json;
		} catch (JSONException e) {
			return loc.toString() + " threw " + e.toString();
		}
	}

	private static Object wrapBundle(Bundle b) {
		try {
			JSONObject json = new JSONObject();
			for (String key : b.keySet()) {
				json.put(key, wrap(b.get(key)));
			}
			return json;
		} catch (JSONException e) {
			return b.toString() + " threw " + e.toString();
		}
	}
}
