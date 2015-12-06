package net.kazhik.gambarumeter.main.monitor;

/**
 * Created by kazhik on 14/10/15.
 */
public interface HeartRateSensorValueListener extends SensorValueListener {
    void onHeartRateChanged(long timestamp, int rate);
}
