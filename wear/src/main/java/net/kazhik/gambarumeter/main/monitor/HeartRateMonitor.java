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
import java.util.concurrent.LinkedBlockingQueue;

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
    private LinkedBlockingQueue<SensorValue> queue = new LinkedBlockingQueue<SensorValue>();
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
                SensorManager.SENSOR_DELAY_NORMAL);
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
        if (this.sensorManager == null) {
            return;
        }
        this.sensorManager.unregisterListener(this, this.heartRateSensor);

    }
    public void start() {
        this.dataList.clear();

        this.started = true;
    }
    public void stop() {
        this.started = false;
        this.calculateAverageHeartRateInQueue();

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

    private SensorValue calculateAverageHeartRateInQueue() {
        SensorValue average = new SensorValue(0, 0);
        if (this.queue.isEmpty()) {
            return average;
        }
        SensorValue sensor;
        int sumHeartRate = 0;
        int size = this.queue.size();
        long lastTimestamp = 0;
        while ((sensor = this.queue.poll()) != null) {
            lastTimestamp = sensor.getTimestamp();
            sumHeartRate += sensor.getValue();
        }
        average.setTimestamp(lastTimestamp);
        average.setValue(sumHeartRate / size);

        return average;
    }
    private int storeHeartRate(long timestamp, float heartRate) {
        // raw data in queue, rate per minute in dataList
        
        if (this.queue.isEmpty()) {
            this.queue.add(new SensorValue(timestamp, heartRate));
            return 0;
        }
        SensorValue average = new SensorValue(0, 0);
        long firstTimestamp = this.queue.peek().getTimestamp();
        if (timestamp > firstTimestamp + (1000 * 60)) {
            average = this.calculateAverageHeartRateInQueue();
            this.dataList.add(average);
        }
        this.queue.add(new SensorValue(timestamp, heartRate));
        return (int)average.getValue();
    }
    private void onSensorEvent(long timestamp, float sensorValue, int accuracy) {
        switch (accuracy) {
            case SensorManager.SENSOR_STATUS_ACCURACY_HIGH:
            case SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM:
                break;
            case SensorManager.SENSOR_STATUS_ACCURACY_LOW:
            default:
                return;
        }
        long newTimestamp = timestamp / (1000 * 1000);
        if (sensorValue != this.currentValue.getValue()) {
            this.listener.onHeartRateChanged(newTimestamp, (int)sensorValue);
            if (this.started) {
                this.storeHeartRate(newTimestamp, sensorValue);
            }
        }

        this.currentValue.setTimestamp(newTimestamp);
        this.currentValue.setValue(sensorValue);
    }
    
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        long newTimestamp = sensorEvent.timestamp / (1000 * 1000);
        
        this.onSensorEvent(newTimestamp, sensorEvent.values[0], sensorEvent.accuracy);

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public IBinder onBind(Intent intent) {
        return this.binder;
    }
}
