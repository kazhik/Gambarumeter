package net.kazhik.gambarumeter.monitor;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by kazhik on 14/10/11.
 */
public class HeartRateMonitor extends Service implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor heartRateSensor;
    private SensorValueListener listener;
    private SensorValue currentValue = new SensorValue();
    private List<SensorValue> dataList = new ArrayList<SensorValue>();
    private HeartRateBinder binder = new HeartRateBinder();
    private static final String TAG = "HeartRateMonitor";

    public class HeartRateBinder extends Binder {

        public HeartRateMonitor getService() {
            return HeartRateMonitor.this;
        }
    }

    public void init(SensorManager sensorManager, SensorValueListener listener) {

        this.sensorManager = sensorManager;
        this.listener = listener;

        this.heartRateSensor = this.sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
        if (this.heartRateSensor == null) {
            Log.w(TAG, "no heart rate sensor");
        }
    }
    public void start() {
        this.dataList.clear();
        this.sensorManager.registerListener(this, this.heartRateSensor, SensorManager.SENSOR_DELAY_NORMAL);

    }
    public void stop() {
        this.sensorManager.unregisterListener(this, this.heartRateSensor);

        this.printDataList();
    }
    public List<SensorValue> getDataList() {
        return this.dataList;
    }
    public void printDataList() {
        for (SensorValue val: this.dataList) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(val.getTimestamp());
            Log.i(TAG, DateFormat.getDateTimeInstance().format(cal.getTime()) + " " + val.getValue());
        }
    }

    private void storeHeartRate(long timestamp, float heartRate) {
        long lastTimestamp = 0;
        if (!this.dataList.isEmpty()) {
            lastTimestamp = this.dataList.get(this.dataList.size() - 1).getTimestamp();
        }
        if (timestamp >= lastTimestamp + (1000 * 60)) {
            this.dataList.add(new SensorValue(timestamp, heartRate));
        }
    }
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        switch (sensorEvent.accuracy) {
            case SensorManager.SENSOR_STATUS_ACCURACY_HIGH:
            case SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM:
            case SensorManager.SENSOR_STATUS_ACCURACY_LOW:
                break;
            default:
                return;
        }
        long newTimestamp = sensorEvent.timestamp / (1000 * 1000);
        if (sensorEvent.values[0] != this.currentValue.getValue()) {
            this.listener.onHeartRateChanged(newTimestamp, (int)sensorEvent.values[0]);
            this.storeHeartRate(newTimestamp, sensorEvent.values[0]);
        }

        this.currentValue.setTimestamp(newTimestamp);
        this.currentValue.setValue(sensorEvent.values[0]);

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        String status = "";
        switch (i) {
            case SensorManager.SENSOR_STATUS_ACCURACY_HIGH:
                status = "high";
                break;
            case SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM:
                status = "medium";
                break;
            case SensorManager.SENSOR_STATUS_ACCURACY_LOW:
                status = "low";
                break;
            case SensorManager.SENSOR_STATUS_NO_CONTACT:
                status = "no contact";
                break;
            case SensorManager.SENSOR_STATUS_UNRELIABLE:
                status = "unreliable";
                break;
        }
        Log.d(TAG, "accuracy: " + status);

    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        return this.binder;
    }
}
