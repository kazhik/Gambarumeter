package net.kazhik.gambarumeter.monitor;

/**
 * Created by kazhik on 14/10/15.
 */
public interface SensorValueListener {
    public void onHeartRateChanged(long timestamp, int rate);
    public void onStepCountChanged(long timestamp, int stepCount);
}
