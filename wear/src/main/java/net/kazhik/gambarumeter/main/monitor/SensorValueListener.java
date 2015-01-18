package net.kazhik.gambarumeter.main.monitor;

/**
 * Created by kazhik on 14/10/15.
 */
public interface SensorValueListener {
    public void onStepCountChanged(long timestamp, int stepCount);

    public void onBatteryLow();
    public void onBatteryOkay();
}
