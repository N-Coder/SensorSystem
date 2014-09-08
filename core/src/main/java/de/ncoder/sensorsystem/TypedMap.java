package de.ncoder.sensorsystem;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TypedMap<V> implements Map<Key<? extends V>, V> {
    private final Map<Key<? extends V>, V> delegate;

    public TypedMap() {
        this(new HashMap<Key<? extends V>, V>());
    }

    public TypedMap(Map<Key<? extends V>, V> delegate) {
        this.delegate = delegate;
    }

    public TypedMap(TypedMap<V> copyFrom) {
        this();
        putAll(copyFrom);
    }

    /**
     * Returns the value to which the specified key is mapped,
     * or {@code null} if this map contains no mapping for the key.
     * <p/>
     * <p>More formally, if this map contains a mapping from a key
     * {@code k} to a value {@code v} such that {@code (key==null ? k==null :
     * key.equals(k))}, then this method returns {@code v}; otherwise
     * it returns {@code null}.  (There can be at most one such mapping.)
     * <p/>
     * <p>If the underlying map permits null values, then a return value of
     * {@code null} does not <i>necessarily</i> indicate that the map
     * contains no mapping for the key; it's also possible that the map
     * explicitly maps the key to {@code null}.  The {@link #containsKey
     * containsKey} operation may be used to distinguish these two cases.
     *
     * @param key the key whose associated value is to be returned
     * @return the value to which the specified key is mapped, or
     * {@code null} if this map contains no mapping for the key
     * @throws NullPointerException if the specified key is null and the underlying map
     *                              does not permit null keys
     *                              (<a href="Collection.html#optional-restrictions">optional</a>)
     */
    @SuppressWarnings("unchecked")
    public <T extends V> T get(Key<T> key) {
        return (T) delegate.get(key);
    }


    /**
     * Returns the value to which the specified key is mapped,
     * or {@code null} if this map contains no mapping for the key.
     * <p/>
     * <p>More formally, if this map contains a mapping from a key
     * {@code k} to a value {@code v} such that {@code (key==null ? k==null :
     * key.equals(k))}, then this method returns {@code v}; otherwise
     * it returns {@code null}.  (There can be at most one such mapping.)
     * <p/>
     * <p>If this map permits null values, then a return value of
     * {@code null} does not <i>necessarily</i> indicate that the map
     * contains no mapping for the key; it's also possible that the map
     * explicitly maps the key to {@code null}.  The {@link #containsKey
     * containsKey} operation may be used to distinguish these two cases.
     *
     * @param key the key whose associated value is to be returned
     * @return the value to which the specified key is mapped, or
     * {@code null} if this map contains no mapping for the key
     * @throws ClassCastException   if the key is of an inappropriate type for
     *                              this map
     *                              (<a href="Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException if the specified key is null and this map
     *                              does not permit null keys
     *                              (<a href="Collection.html#optional-restrictions">optional</a>)
     * @deprecated use the strongly-typed version {@link #get(Key)} instead.
     */
    @Override
    @Deprecated
    public V get(Object key) {
        return delegate.get(key);
    }

    /**
     * Associates the specified value with the specified key in this map.
     * If the map previously contained a mapping for
     * the key, the old value is replaced by the specified value.  (A map
     * <tt>m</tt> is said to contain a mapping for a key <tt>k</tt> if and only
     * if {@link #containsKey(Object) m.containsKey(k)} would return
     * <tt>true</tt>.)
     *
     * @param key   key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return the previous value associated with <tt>key</tt>, or
     * <tt>null</tt> if there was no mapping for <tt>key</tt>.
     * (A <tt>null</tt> return can also indicate that the map
     * previously associated <tt>null</tt> with <tt>key</tt>)
     * @throws ClassCastException       if the class of the specified key or value
     *                                  prevents it from being stored in this map, usually the value
     *                                  not being assignable to the key.
     *                                  See {@link de.ncoder.sensorsystem.Key#isPossibleValue(Object)}.
     * @throws NullPointerException     if the specified key or value is null
     *                                  and this map does not permit null keys or values
     * @throws IllegalArgumentException if some property of the specified key
     *                                  or value prevents it from being stored in this map
     */
    @SuppressWarnings("unchecked")
    public <T extends V> T putTyped(Key<T> key, T value) {
        return (T) put(key, value);
    }


    /**
     * Associates the specified value with the specified key in this map.
     * If the map previously contained a mapping for
     * the key, the old value is replaced by the specified value.  (A map
     * <tt>m</tt> is said to contain a mapping for a key <tt>k</tt> if and only
     * if {@link #containsKey(Object) m.containsKey(k)} would return
     * <tt>true</tt>.)
     *
     * @param key   key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return the previous value associated with <tt>key</tt>, or
     * <tt>null</tt> if there was no mapping for <tt>key</tt>.
     * (A <tt>null</tt> return can also indicate that the map
     * previously associated <tt>null</tt> with <tt>key</tt>)
     * @throws ClassCastException       if the class of the specified key or value
     *                                  prevents it from being stored in this map, usually the value
     *                                  not being assignable to the key.
     *                                  See {@link de.ncoder.sensorsystem.Key#isPossibleValue(Object)}.
     * @throws NullPointerException     if the specified key or value is null
     *                                  and this map does not permit null keys or values
     * @throws IllegalArgumentException if some property of the specified key
     *                                  or value prevents it from being stored in this map
     * @deprecated use the strongly-typed version {@link #putTyped(Key, Object)} instead.
     */
    @Override
    @Deprecated
    public V put(Key<? extends V> key, V value) {
        if (key != null && !key.isPossibleValue(value)) {
            throw new ClassCastException("Value " + value + " is not assignable to key " + key);
        }
        return delegate.put(key, value);
    }

    /**
     * Removes the mapping for a key from this map if it is present
     * (optional operation).   More formally, if this map contains a mapping
     * from key <tt>k</tt> to value <tt>v</tt> such that
     * <code>(key==null ?  k==null : key.equals(k))</code>, that mapping
     * is removed.  (The map can contain at most one such mapping.)
     * <p/>
     * <p>Returns the value to which this map previously associated the key,
     * or <tt>null</tt> if the map contained no mapping for the key.
     * <p/>
     * <p>If the underlying map permits null values, then a return value of
     * <tt>null</tt> does not <i>necessarily</i> indicate that the map
     * contained no mapping for the key; it's also possible that the map
     * explicitly mapped the key to <tt>null</tt>.
     * <p/>
     * <p>The map will not contain a mapping for the specified key once the
     * call returns.
     *
     * @param key key whose mapping is to be removed from the map
     * @return the previous value associated with <tt>key</tt>, or
     * <tt>null</tt> if there was no mapping for <tt>key</tt>.
     * @throws ClassCastException   if the key is of an inappropriate type for
     *                              this map
     *                              (<a href="Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException if the specified key is null and this
     *                              map does not permit null keys
     *                              (<a href="Collection.html#optional-restrictions">optional</a>)
     */
    @SuppressWarnings("unchecked")
    public <T extends V> T remove(Key<T> key) {
        return (T) delegate.remove(key);
    }

    /**
     * Removes the mapping for a key from this map if it is present
     * (optional operation).   More formally, if this map contains a mapping
     * from key <tt>k</tt> to value <tt>v</tt> such that
     * <code>(key==null ?  k==null : key.equals(k))</code>, that mapping
     * is removed.  (The map can contain at most one such mapping.)
     * <p/>
     * <p>Returns the value to which this map previously associated the key,
     * or <tt>null</tt> if the map contained no mapping for the key.
     * <p/>
     * <p>If the underlying map permits null values, then a return value of
     * <tt>null</tt> does not <i>necessarily</i> indicate that the map
     * contained no mapping for the key; it's also possible that the map
     * explicitly mapped the key to <tt>null</tt>.
     * <p/>
     * <p>The map will not contain a mapping for the specified key once the
     * call returns.
     *
     * @param key key whose mapping is to be removed from the map
     * @return the previous value associated with <tt>key</tt>, or
     * <tt>null</tt> if there was no mapping for <tt>key</tt>.
     * @throws ClassCastException   if the key is of an inappropriate type for
     *                              this map
     *                              (<a href="Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException if the specified key is null and this
     *                              map does not permit null keys
     *                              (<a href="Collection.html#optional-restrictions">optional</a>)
     * @deprecated use the strongly-typed version {@link #remove(Key)} instead.
     */
    @Override
    @Deprecated
    public V remove(Object key) {
        return delegate.remove(key);
    }

    /**
     * Copies all of the mappings from the specified map to this map
     * (optional operation).  The effect of this call is equivalent to that
     * of calling {@link #put(Object, Object) put(k, v)} on this map once
     * for each mapping from key <tt>k</tt> to value <tt>v</tt> in the
     * specified map.  The behavior of this operation is undefined if the
     * specified map is modified while the operation is in progress.
     * <p/>
     * <p>Note: If the specified map is a TypedMap, this will simply delegate
     * to the underlying Map. Otherwise, every Entry of the specified Map will
     * be added to this Map using {@link #put(Key, Object)} to ensure type safety.
     *
     * @param m mappings to be stored in this map
     * @throws ClassCastException       if the class of a key or value in the
     *                                  specified map prevents it from being stored in this map, usually the value
     *                                  not being assignable to the key.
     *                                  See {@link de.ncoder.sensorsystem.Key#isPossibleValue(Object)}.
     * @throws NullPointerException     if the specified map is null, or if
     *                                  this map does not permit null keys or values, and the
     *                                  specified map contains null keys or values
     * @throws IllegalArgumentException if some property of a key or value in
     *                                  the specified map prevents it from being stored in this map
     */
    @Override
    public void putAll(Map<? extends Key<? extends V>, ? extends V> m) {
        if (m instanceof TypedMap) {
            delegate.putAll(m);
        } else {
            for (Entry<? extends Key<? extends V>, ? extends V> e : m.entrySet()) {
                put(e.getKey(), e.getValue());
            }
        }
    }

    // DELEGATES --------------------------------------------------------------

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return delegate.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return delegate.containsValue(value);
    }

    @Override
    public Set<Key<? extends V>> keySet() {
        return delegate.keySet();
    }

    @Override
    public Collection<V> values() {
        return delegate.values();
    }

    @Override
    public Set<Entry<Key<? extends V>, V>> entrySet() {
        return delegate.entrySet();
    }

    @Override
    public void clear() {
        delegate.clear();
    }

    @Override
    public boolean equals(Object o) {
        return delegate.equals(o);
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    @Override
    public String toString() {
        return delegate.toString();
    }
}
