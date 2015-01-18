package net.kazhik.gambarumeter.main.monitor;

/**
 * Created by kazhik on 14/10/15.
 */
public interface LocationSensorValueListener extends SensorValueListener {
    public void onLocationChanged(long timestamp, float distance, float speed);
    public void onLocationAvailable();
    public void onLap(long timestamp, float distance, long lap);
    
}
