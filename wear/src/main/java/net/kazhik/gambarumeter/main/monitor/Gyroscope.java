package net.kazhik.gambarumeter.main.monitor;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by kazhik on 15/04/21.
 */
public class Gyroscope extends SensorService {
    private float threshold = 30.0f;
    private int waitTime = 100;

    private GyroBinder binder = new GyroBinder();
    private SensorValueListener listener;
    private long lastTimestamp = 0;

    private static final String TAG = "Gyroscope";

    public class GyroBinder extends Binder {

        public Gyroscope getService() {
            return Gyroscope.this;
        }
    }

    public Gyroscope setThreshold(float threshold) {
        this.threshold = threshold;
        return this;
    }

    public Gyroscope setWaitTime(int waitTime) {
        this.waitTime = waitTime;
        return this;
    }

    public void initialize(SensorManager sensorManager,
                           SensorValueListener listener) {
        this.listener = listener;
        super.initialize(sensorManager, Sensor.TYPE_GYROSCOPE);
    }

    @Override
    protected void onSensorEvent(long timestamp, float[] sensorValues, int accuracy) {
        if (accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE ||
                accuracy == SensorManager.SENSOR_STATUS_NO_CONTACT) {
            return;
        }

        long newTimestamp = timestamp / (1000 * 1000);
        if((newTimestamp - lastTimestamp) > waitTime) {
            lastTimestamp = newTimestamp;

            if(Math.abs(sensorValues[0]) > threshold ||
                    Math.abs(sensorValues[1]) > threshold ||
                    Math.abs(sensorValues[2]) > threshold) {

                this.listener.onRotation(newTimestamp);
            }
        }

    }
    @Override
    public IBinder onBind(Intent intent) {
        return this.binder;
    }


}
