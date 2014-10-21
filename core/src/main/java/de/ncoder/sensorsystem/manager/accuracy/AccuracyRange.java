package de.ncoder.sensorsystem.manager.accuracy;

public abstract class AccuracyRange<V, T> {
    private T additional;

    public AccuracyRange(T additional) {
        this.additional = additional;
    }

    public abstract V scale(int accuracy);

    public V scale(AccuracyManager accuracySource) {
        if (accuracySource == null) {
            return defaut();
        } else {
            return scale(accuracySource.getAccuracy());
        }
    }

    public V defaut() {
        return scale(AccuracyManager.DEFAULT_ACCURACY);
    }

    public T getAdditional() {
        return additional;
    }

    public void setAdditional(T additional) {
        this.additional = additional;
    }
}
