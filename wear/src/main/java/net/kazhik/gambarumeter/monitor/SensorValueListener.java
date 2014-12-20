package net.kazhik.gambarumeter.monitor;

import android.location.Location;

/**
 * Created by kazhik on 14/10/15.
 */
public interface SensorValueListener {
    public void onHeartRateChanged(long timestamp, int rate);
    public void onStepCountChanged(long timestamp, int stepCount);
    public void onLocationChanged(long timestamp, float distance, float speed);
    public void onLocationAvailable();
    public void onLap(long timestamp, float distance, long lap);
}
