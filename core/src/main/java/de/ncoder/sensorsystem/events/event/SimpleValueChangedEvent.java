package de.ncoder.sensorsystem.events.event;

import de.ncoder.sensorsystem.Component;
import de.ncoder.sensorsystem.events.EventUtils;

public class SimpleValueChangedEvent<ValueT, SourceT extends Component> extends SimpleEvent<SourceT> implements ValueChangedEvent<ValueT> {
    private final ValueT oldValue, newValue;

    public SimpleValueChangedEvent(String name, ValueT oldValue, ValueT newValue) {
        this(name, null, oldValue, newValue);
    }

    public SimpleValueChangedEvent(SourceT source, ValueT oldValue, ValueT newValue) {
        this(null, source, oldValue, newValue);
    }

    public SimpleValueChangedEvent(String name, SourceT source, ValueT oldValue, ValueT newValue) {
        super(name, source);
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public SimpleValueChangedEvent(String name, SourceT source, long when, ValueT oldValue, ValueT newValue) {
        super(name, source, when);
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    @Override
    public ValueT getOldValue() {
        return oldValue;
    }

    @Override
    public ValueT getNewValue() {
        return newValue;
    }

    @Override
    public String toString() {
        return "[" + getName() + ": " + EventUtils.toString(getOldValue()) + "->" + EventUtils.toString(getNewValue()) + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ValueChangedEvent)) return false;
        if (!super.equals(o)) return false;
        ValueChangedEvent that = (ValueChangedEvent) o;
        return !(newValue != null ? !newValue.equals(that.getNewValue()) : that.getNewValue() != null)
                && !(oldValue != null ? !oldValue.equals(that.getOldValue()) : that.getOldValue() != null);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (oldValue != null ? oldValue.hashCode() : 0);
        result = 31 * result + (newValue != null ? newValue.hashCode() : 0);
        return result;
    }
}
