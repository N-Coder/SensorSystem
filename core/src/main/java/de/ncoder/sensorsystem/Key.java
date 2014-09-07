package de.ncoder.sensorsystem;

import java.io.Serializable;

public class Key<T> implements Serializable {
    private final Class<T> clazz;
    private final String identifier;

    public Key(Class<T> clazz) {
        this(clazz, clazz.getName());
    }

    public Key(Class<T> clazz, String identifier) {
        if (clazz == null) {
            throw new NullPointerException("class");
        }
        if (identifier == null) {
            throw new NullPointerException("identifier");
        }
        this.clazz = clazz;
        this.identifier = identifier;
    }

    @SuppressWarnings("unchecked")
    public static Key<?> forName(String clazz, String identifier) throws ClassNotFoundException {
        if (identifier == null || identifier.isEmpty()) {
            return new Key(Class.forName(clazz));
        } else {
            return new Key(Class.forName(clazz), identifier);
        }
    }

    public Class<T> getValueClass() {
        return clazz;
    }

    public String getIdentifier() {
        return identifier;
    }

    public boolean isPossibleValue(Object value) {
        return value == null || getValueClass().isInstance(value);
    }

    @Override
    public String toString() {
        return getIdentifier() + " [" + getClass() + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Key key = (Key) o;
        return clazz.equals(key.clazz) && identifier.equals(key.identifier);
    }

    @Override
    public int hashCode() {
        return 31 * clazz.hashCode() + identifier.hashCode();
    }
}
