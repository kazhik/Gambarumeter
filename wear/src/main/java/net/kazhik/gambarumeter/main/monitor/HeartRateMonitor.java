package net.kazhik.gambarumeter.main.monitor;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import net.kazhik.gambarumeterlib.entity.SensorValue;
import net.kazhik.gambarumeterlib.storage.HeartRateTable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by kazhik on 14/10/11.
 */
public class HeartRateMonitor extends SensorService {
    private Context context;
    private HeartRateSensorValueListener listener;
    private SensorValue currentValue = new SensorValue();
    private List<SensorValue> dataList = new ArrayList<>();
    private LinkedBlockingQueue<SensorValue> queue = new LinkedBlockingQueue<>();
    private HeartRateBinder binder = new HeartRateBinder();
    private static final String TAG = "HeartRateMonitor";

    public class HeartRateBinder extends Binder {

        public HeartRateMonitor getService() {
            return HeartRateMonitor.this;
        }
    }

    public void init(Context context,
                     SensorManager sensorManager,
                     HeartRateSensorValueListener listener) {

        Log.d(TAG, "initialize");
        this.context = context;
        this.listener = listener;

        super.initialize(sensorManager, Sensor.TYPE_HEART_RATE);
    }
    public void saveResult(SQLiteDatabase db, long startTime) {
        HeartRateTable heartRateTable = new HeartRateTable(this.context, db);
        for (SensorValue sensorValue: this.dataList) {
            heartRateTable.insert(
                    sensorValue.getTimestamp(),
                    startTime,
                    (int)sensorValue.getValue(),
                    sensorValue.getAccuracy());
        }

    }

    @Override
    public void start() {
        super.start();

        this.dataList.clear();

    }

    @Override
    public void stop(long stopTime) {
        super.stop(stopTime);

        SensorValue average = this.calculateAverageHeartRateInQueue();
        if (average.getValue() != 0f) {
            this.dataList.add(average);
        }
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

    private SensorValue calculateAverageHeartRateInQueue() {
        SensorValue average = new SensorValue(0, 0);
        if (this.queue.isEmpty()) {
            return average;
        }
        SensorValue sensor;
        int sumHeartRate = 0;
        int size = 0;
        long lastTimestamp = 0;
        while ((sensor = this.queue.poll()) != null) {
            lastTimestamp = sensor.getTimestamp();
            sumHeartRate += sensor.getValue();
            size++;
        }
        Log.d(TAG, "calculate average HR: size=" + size);
        average.setTimestamp(lastTimestamp);
        int averageValue = sumHeartRate / size;
        average.setValue(averageValue);

        return average;
    }

    @Override
    public void storeCurrentValue(long timestamp) {
        // raw data in queue, rate per minute in dataList

        SensorValue average = this.calculateAverageHeartRateInQueue();
        if (average.getValue() != 0f) {
            Log.d(TAG, "storeCurrentValue: " + average.getValue());
            this.dataList.add(average);
        }
    }

    @Override
    public void onSensorEvent(long timestamp, float[] sensorValue, int accuracy) {
        switch (accuracy) {
            case SensorManager.SENSOR_STATUS_ACCURACY_HIGH:
            case SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM:
            case SensorManager.SENSOR_STATUS_ACCURACY_LOW:
                break;
            default:
                return;
        }
        long newTimestamp = System.currentTimeMillis();
        if (sensorValue[0] != this.currentValue.getValue()) {
            this.listener.onHeartRateChanged(newTimestamp, (int)sensorValue[0]);
            if (this.isStarted()) {
                this.queue.add(new SensorValue(newTimestamp, sensorValue[0], accuracy));
            }
        }

        this.currentValue.setTimestamp(newTimestamp);
        this.currentValue.setValue(sensorValue[0]);
    }
    @Override
    public IBinder onBind(Intent intent) {
        return this.binder;
    }
}
