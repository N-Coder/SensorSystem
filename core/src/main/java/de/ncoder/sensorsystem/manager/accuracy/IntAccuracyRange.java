package de.ncoder.sensorsystem.manager.accuracy;

public class IntAccuracyRange<V> extends NumericAccuracyRange<Integer, V> {
    public IntAccuracyRange(Integer leastAccurate, Integer mostAccurate, V additional) {
        super(leastAccurate, mostAccurate, additional);
    }

    public IntAccuracyRange(Integer leastAccurate, Integer mostAccurate) {
        super(leastAccurate, mostAccurate);
    }

    public IntAccuracyRange(Integer fixedValue, V additional) {
        super(fixedValue, additional);
    }

    public IntAccuracyRange(Integer fixedValue) {
        super(fixedValue);
    }

    @Override
    public Integer scale(int accuracy) {
        return Math.round((getMostAccurate() - getLeastAccurate()) * (accuracy / (float) AccuracyManager.ACCURACY_MAX)) + getLeastAccurate();
    }
}
