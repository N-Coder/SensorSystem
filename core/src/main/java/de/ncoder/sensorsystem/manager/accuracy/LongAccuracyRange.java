package de.ncoder.sensorsystem.manager.accuracy;

public class LongAccuracyRange<V> extends NumericAccuracyRange<Long, V> {
    public LongAccuracyRange(Long leastAccurate, Long mostAccurate, V additional) {
        super(leastAccurate, mostAccurate, additional);
    }

    public LongAccuracyRange(Long leastAccurate, Long mostAccurate) {
        super(leastAccurate, mostAccurate);
    }

    public LongAccuracyRange(Long fixedValue, V additional) {
        super(fixedValue, additional);
    }

    public LongAccuracyRange(Long fixedValue) {
        super(fixedValue);
    }

    @Override
    public Long scale(int accuracy) {
        return Math.round((getMostAccurate() - getLeastAccurate()) * (accuracy / (double) AccuracyManager.ACCURACY_MAX)) + getLeastAccurate();
    }
}
