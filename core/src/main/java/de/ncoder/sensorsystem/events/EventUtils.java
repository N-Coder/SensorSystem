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

package de.ncoder.sensorsystem.events;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EventUtils {
	private EventUtils() {
	}

	public static String toString(Object o) {
		if (o instanceof Object[])
			return Arrays.deepToString((Object[]) o);
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

	private static final Pattern FQN = Pattern.compile("([a-zA-Z_$][a-zA-Z\\d_$]*\\.)*([a-zA-Z_$][a-zA-Z\\d_$]*)");

	@Deprecated
	public static String abbreviatedClassNames(String name) {
		Matcher matcher = FQN.matcher(name);
		StringBuffer bob = new StringBuffer();
		StringBuilder replacement = new StringBuilder();
		while (matcher.find()) {
			String[] names = matcher.group().split("\\.");
			for (int i = 0; i < names.length; i++) {
				if (names[i] != null && !names[i].isEmpty()) {
					if (i < names.length - 1) {
						replacement.append(names[i].charAt(0));
						if (names[i].length() > 1) {
							replacement.append("\u2026");
						} else {
							replacement.append(".");
						}
					} else {
						replacement.append(names[i]);
					}
				}
			}
			matcher.appendReplacement(bob, replacement.toString());
			replacement.setLength(0);
		}
		matcher.appendTail(bob);
		return bob.toString();
	}

	public static String simpleClassNames(String name) {
		Pattern fqn = Pattern.compile("([a-zA-Z_$][a-zA-Z\\d_$]*\\.)*([a-zA-Z_$][a-zA-Z\\d_$]*)");
		Matcher matcher = fqn.matcher(name);
		StringBuffer bob = new StringBuffer();
		while (matcher.find()) {
			matcher.appendReplacement(bob, matcher.group(matcher.groupCount()));
		}
		matcher.appendTail(bob);
		return bob.toString();
	}
}
