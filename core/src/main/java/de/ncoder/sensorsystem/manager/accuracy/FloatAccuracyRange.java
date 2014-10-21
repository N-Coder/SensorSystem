package de.ncoder.sensorsystem.manager.accuracy;

public class FloatAccuracyRange<V> extends NumericAccuracyRange<Float, V> {
    public FloatAccuracyRange(Float leastAccurate, Float mostAccurate, V additional) {
        super(leastAccurate, mostAccurate, additional);
    }

    public FloatAccuracyRange(Float leastAccurate, Float mostAccurate) {
        super(leastAccurate, mostAccurate);
    }

    public FloatAccuracyRange(Float fixedValue, V additional) {
        super(fixedValue, additional);
    }

    public FloatAccuracyRange(Float fixedValue) {
        super(fixedValue);
    }

    @Override
    public Float scale(int accuracy) {
        return (getMostAccurate() - getLeastAccurate()) * (accuracy / (float) AccuracyManager.ACCURACY_MAX) + getLeastAccurate();
    }
}
