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

package de.ncoder.sensorsystem;

import java.util.Collection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import de.ncoder.typedmap.Key;
import de.ncoder.typedmap.TypedMap;

public interface Container {
	<T extends Component, V extends T> void register(@Nonnull Key<T> key, @Nonnull V component);

	void unregister(@Nonnull Key<?> key);

	void unregister(@Nonnull Component component);

	@Nullable
	<T extends Component> T get(@Nonnull Key<T> key);

	@Nonnull
	<T extends Component> T require(@Nonnull Key<T> key) throws SimpleContainer.DependencyException;

	boolean isRegistered(@Nonnull Key<?> key);

	@Nonnull
	TypedMap<? extends Component> getData();

	@Nonnull
	Collection<Key<? extends Component>> getKeys();

	void shutdown();
}
