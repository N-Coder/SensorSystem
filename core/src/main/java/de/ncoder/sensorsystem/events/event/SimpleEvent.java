package de.ncoder.sensorsystem.events.event;

import de.ncoder.sensorsystem.Component;

public class SimpleEvent<T extends Component> implements Event {
    private final long when;
    private final T source;
    private final String name;

    public SimpleEvent(String name, T source) {
        this(name, source, System.currentTimeMillis());
    }

    public SimpleEvent(String name, T source, long when) {
        if (name == null && source != null) {
            name = source.getClass().toString();
        }
        this.name = name;
        this.when = when;
        this.source = source;
    }

    @Override
    public long getWhen() {
        return when;
    }

    @Override
    public T getSource() {
        return source;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "[" + getName() + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SimpleEvent)) return false;
        SimpleEvent that = (SimpleEvent) o;
        return when == that.when
                && (name != null ? name.equals(that.name) : that.name == null)
                && (source != null ? source.equals(that.source) : that.source == null);

    }

    @Override
    public int hashCode() {
        int result = (int) (when ^ (when >>> 32));
        result = 31 * result + (source != null ? source.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }
}
