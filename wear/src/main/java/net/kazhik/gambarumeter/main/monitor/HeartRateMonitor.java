package net.kazhik.gambarumeter.main.monitor;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import net.kazhik.gambarumeter.entity.SensorValue;
import net.kazhik.gambarumeter.storage.HeartRateTable;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by kazhik on 14/10/11.
 */
public class HeartRateMonitor extends Service implements SensorEventListener {
    private Context context;
    private SensorManager sensorManager;
    private Sensor heartRateSensor;
    private SensorValueListener listener;
    private SensorValue currentValue = new SensorValue();
    private List<SensorValue> dataList = new ArrayList<SensorValue>();
    private HeartRateBinder binder = new HeartRateBinder();
    private static final String TAG = "HeartRateMonitor";
    private boolean started = false;

    public class HeartRateBinder extends Binder {

        public HeartRateMonitor getService() {
            return HeartRateMonitor.this;
        }
    }

    public void init(Context context,
                     SensorManager sensorManager,
                     SensorValueListener listener) {

        this.context = context;
        this.sensorManager = sensorManager;
        this.listener = listener;

        this.heartRateSensor =
                this.sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
        if (this.heartRateSensor == null) {
            Log.w(TAG, "no heart rate sensor");
            return;
        }
        this.sensorManager.registerListener(this,
                this.heartRateSensor,
                30 * 1000 * 1000); // delay: 30 seconds
    }
    public void saveResult(long startTime) {
        HeartRateTable heartRateTable = new HeartRateTable(this.context);
        heartRateTable.open(false);
        for (SensorValue sensorValue: this.dataList) {
            heartRateTable.insert(
                    sensorValue.getTimestamp(),
                    startTime,
                    (int)sensorValue.getValue());
        }
        heartRateTable.close();
        
    }
    public void terminate() {
        this.sensorManager.unregisterListener(this, this.heartRateSensor);

    }
    public void start() {
        this.dataList.clear();

        this.started = true;
    }
    public void stop() {
        this.started = false;

        this.printDataList();
    }
    public int getAverageHeartRate() {
        if (this.dataList.isEmpty()) {
            return 0;
        }
        long sum = 0;
        for (SensorValue val: this.dataList) {
            sum += val.getValue();
        }
        return (int)(sum / this.dataList.size());
    }
    private void printDataList() {
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
                break;
            case SensorManager.SENSOR_STATUS_ACCURACY_LOW:
            default:
                return;
        }
        long newTimestamp = sensorEvent.timestamp / (1000 * 1000);
        if (sensorEvent.values[0] != this.currentValue.getValue()) {
            this.listener.onHeartRateChanged(newTimestamp, (int)sensorEvent.values[0]);
            if (this.started) {
                this.storeHeartRate(newTimestamp, sensorEvent.values[0]);
            }
        }

        this.currentValue.setTimestamp(newTimestamp);
        this.currentValue.setValue(sensorEvent.values[0]);

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public IBinder onBind(Intent intent) {
        return this.binder;
    }
}
