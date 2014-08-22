package de.ncoder.sensorsystem;

public class AbstractComponent implements Component {
    private Container container;

    @Override
    public void init(Container container) {
        this.container = container;
    }

    @Override
    public void destroy() {
        container = null;
    }

    protected boolean isActive() {
        return container != null;
    }

    protected Container getContainer() {
        return container;
    }

    protected <T extends Component> T getOtherComponent(Container.Key<T> key) {
        if (getContainer() != null) {
            return getContainer().get(key);
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        String name = getClass().getSimpleName();
        if (name == null || name.isEmpty()) {
            name = getClass().getName();
            int index = name.lastIndexOf(".");
            if (index >= 0 && index + 1 < name.length()) {
                name = name.substring(index + 1);
            }
        }
        return name;
    }
}
