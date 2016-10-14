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

    private GyroBinder binder = new GyroBinder();
    private SensorValueListener listener;
    private WristRotationDetector wristRotationDetector = new WristRotationDetector();

    private static final String TAG = "Gyroscope";

    public class GyroBinder extends Binder {

        public Gyroscope getService() {
            return Gyroscope.this;
        }
    }

    public Gyroscope setThreshold(float threshold) {
        this.wristRotationDetector.setThreshold(threshold);
        return this;
    }

    public Gyroscope setWaitTime(int waitTime) {
        this.wristRotationDetector.setInterval(waitTime);
        return this;
    }

    public void initialize(SensorValueListener listener) {

        Log.d(TAG, "initialize");

        this.listener = listener;
        super.initialize(Sensor.TYPE_GYROSCOPE);
    }

    @Override
    protected void onSensorEvent(long timestamp, float[] sensorValues, int accuracy) {
        if (accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE ||
                accuracy == SensorManager.SENSOR_STATUS_NO_CONTACT) {
            return;
        }

        long newTimestamp = System.currentTimeMillis();
        boolean doubleRotated =
                this.wristRotationDetector.onSensorEvent(newTimestamp, sensorValues);
        if (doubleRotated) {
            this.listener.onRotation(newTimestamp);
        }

    }
    @Override
    public IBinder onBind(Intent intent) {
        return this.binder;
    }


}
