package de.ncoder.sensorsystem.manager.accuracy;

public class BooleanAccuracyRange<V> extends AccuracyRange<Boolean, V> {
    private int valueThreshold;
    private boolean lowAccuracyValue;

    public BooleanAccuracyRange(int valueThreshold, boolean lowAccuracyValue, V additional) {
        super(additional);
        this.valueThreshold = valueThreshold;
        this.lowAccuracyValue = lowAccuracyValue;
    }

    public BooleanAccuracyRange(int valueThreshold, boolean lowAccuracyValue) {
        super(null);
        this.valueThreshold = valueThreshold;
        this.lowAccuracyValue = lowAccuracyValue;
    }

    public int getValueThreshold() {
        return valueThreshold;
    }

    public void setValueThreshold(int valueThreshold) {
        this.valueThreshold = valueThreshold;
    }

    public boolean getLowAccuracyValue() {
        return lowAccuracyValue;
    }

    public void setLowAccuracyValue(boolean lowAccuracyValue) {
        this.lowAccuracyValue = lowAccuracyValue;
    }

    public boolean getHighAccuracyValue() {
        return !lowAccuracyValue;
    }

    @Override
    public Boolean scale(int accuracy) {
        return accuracy <= getValueThreshold() ? getLowAccuracyValue() : getHighAccuracyValue();
    }
}
