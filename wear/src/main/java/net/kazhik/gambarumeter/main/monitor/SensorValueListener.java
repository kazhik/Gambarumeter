package net.kazhik.gambarumeter.main.monitor;

/**
 * Created by kazhik on 14/10/15.
 */
public interface SensorValueListener {
    void onStepCountChanged(long timestamp, int stepCount);

    void onRotation(long timestamp);

    void onBatteryLow();
    void onBatteryOkay();
}
