package de.ncoder.sensorsystem.events.event;

import de.ncoder.sensorsystem.Component;
import de.ncoder.typedmap.Key;

public class ComponentEvent extends SimpleEvent<Component> {
    public static enum Type {
        ADDED, STARTED, STOPPED, REMOVED;

        @Override
        public String toString() {
            return ComponentEvent.class.getName() + "." + super.toString();
        }
    }

    private final Type type;
    private final String action;
    private final Key<?> key;

    public ComponentEvent(Key<?> key, Component component, Type type, String action) {
        super(type.toString(), component);
        this.type = type;
        this.key = key;
        this.action = action;
    }

    public ComponentEvent(Key<?> key, Component component, Type type) {
        this(key, component, type, type.name().toLowerCase());
    }

    public ComponentEvent(Component component, Type type, String action) {
        this(null, component, type, action);
    }

    public ComponentEvent(Component component, Type type) {
        this(null, component, type);
    }

    public Type getType() {
        return type;
    }

    public Key<?> getKey() {
        return key;
    }

    public String getAction() {
        return action;
    }

    public String toString() {
        return "[" + (getKey() != null ? getKey() + " -> " : "") + getSource() + " " + getAction() + "]";
    }
}
