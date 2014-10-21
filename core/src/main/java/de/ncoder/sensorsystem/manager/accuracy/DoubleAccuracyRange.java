package de.ncoder.sensorsystem.manager.accuracy;

public class DoubleAccuracyRange<V> extends NumericAccuracyRange<Double, V> {
    public DoubleAccuracyRange(Double leastAccurate, Double mostAccurate, V additional) {
        super(leastAccurate, mostAccurate, additional);
    }

    public DoubleAccuracyRange(Double leastAccurate, Double mostAccurate) {
        super(leastAccurate, mostAccurate);
    }

    public DoubleAccuracyRange(Double fixedValue, V additional) {
        super(fixedValue, additional);
    }

    public DoubleAccuracyRange(Double fixedValue) {
        super(fixedValue);
    }

    @Override
    public Double scale(int accuracy) {
        return (getMostAccurate() - getLeastAccurate()) * (accuracy / (double) AccuracyManager.ACCURACY_MAX) + getLeastAccurate();
    }
}
