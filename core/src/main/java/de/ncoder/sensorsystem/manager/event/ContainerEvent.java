package de.ncoder.sensorsystem.manager.event;

import de.ncoder.sensorsystem.Component;
import de.ncoder.sensorsystem.Container;
import de.ncoder.sensorsystem.Key;

public class ContainerEvent extends SimpleEvent<Component> {
    protected ContainerEvent(String name) {
        super(name, null);
    }

    public static class ComponentAdded extends ContainerEvent {
        public static final String NAME = Container.class.getName()+".COMPONENT_ADDED";

        private final Key<?> key;
        private final Component component;

        public ComponentAdded(Key<?> key, Component component) {
            super(NAME);
            this.key = key;
            this.component = component;
        }

        public Key<?> getKey() {
            return key;
        }

        public Component getComponent() {
            return component;
        }
    }

    public static class ComponentRemoved extends ContainerEvent {
        public static final String NAME = Container.class.getName()+".COMPONENT_REMOVED";

        private final Key<?> key;
        private final Component component;

        public ComponentRemoved(Key<?> key, Component component) {
            super(NAME);
            this.key = key;
            this.component = component;
        }

        public Key<?> getKey() {
            return key;
        }

        public Component getComponent() {
            return component;
        }
    }

    public static class ShutdownRequested extends ContainerEvent {
        public static final String NAME = Container.class.getName()+".PRE_SHUTDOWN";

        public ShutdownRequested() {
            super(NAME);
        }
    }
}
