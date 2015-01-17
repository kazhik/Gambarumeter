package net.kazhik.gambarumeter.main.monitor;

/**
 * Created by kazhik on 14/10/15.
 */
public interface SensorValueListener {
    public void onHeartRateChanged(long timestamp, int rate);
    public void onStepCountChanged(long timestamp, int stepCount);
    public void onLocationChanged(long timestamp, float distance, float speed);
    public void onLocationAvailable();
    public void onLap(long timestamp, float distance, long lap);
    
    public void onBatteryLow();
    public void onBatteryOkay();
}
