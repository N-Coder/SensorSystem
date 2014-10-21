package de.ncoder.sensorsystem.manager.accuracy;

public abstract class NumericAccuracyRange<V extends Number, T> extends AccuracyRange<V, T> {
    private V leastAccurate, mostAccurate;

    public NumericAccuracyRange(V leastAccurate, V mostAccurate, T additional) {
        super(additional);
        this.leastAccurate = leastAccurate;
        this.mostAccurate = mostAccurate;
    }

    public NumericAccuracyRange(V leastAccurate, V mostAccurate) {
        this(leastAccurate, mostAccurate, null);
    }

    public NumericAccuracyRange(V fixedValue, T additional) {
        this(fixedValue, fixedValue, additional);
    }

    public NumericAccuracyRange(V fixedValue) {
        this(fixedValue, (T) null);
    }

    public V getLeastAccurate() {
        return leastAccurate;
    }

    public void setLeastAccurate(V leastAccurate) {
        this.leastAccurate = leastAccurate;
    }

    public V getMostAccurate() {
        return mostAccurate;
    }

    public void setMostAccurate(V mostAccurate) {
        this.mostAccurate = mostAccurate;
    }

    public void setFixedAccuracy(V fixedAccuracy) {
        setLeastAccurate(fixedAccuracy);
        setMostAccurate(fixedAccuracy);
    }
}
