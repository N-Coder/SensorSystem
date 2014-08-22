package de.ncoder.sensorsystem.sensor;

public abstract class CachedSensor<T> extends AbstractSensor<T> {
    private T cachedValue;
    private boolean cacheValid = false;

    @Override
    public T get() {
        if (!cacheValid) {
            updateCache();
        }
        return cachedValue;
    }

    public void invalidate() {
        cacheValid = false;
    }

    public boolean hasCachedValue() {
        return cacheValid;
    }

    private void updateCache() {
        T oldValue = cachedValue;
        cachedValue = fetch();
        cacheValid = true;
        changed(oldValue, cachedValue);
    }

    protected abstract T fetch();
}
