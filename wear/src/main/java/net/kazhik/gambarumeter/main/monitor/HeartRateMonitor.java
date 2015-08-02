package net.kazhik.gambarumeter.main.monitor;

import android.content.Context;
import android.content.Intent;
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
    private SensorManager sensorManager;
    private HeartRateSensorValueListener listener;
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
                     HeartRateSensorValueListener listener) {

        this.context = context;
        this.sensorManager = sensorManager;
        this.listener = listener;

        super.initialize(sensorManager, Sensor.TYPE_HEART_RATE);
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
    public void start() {
        this.dataList.clear();

        this.started = true;
    }
    public void stop() {
        this.started = false;
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
            if (average.getValue() != 0f) {
                this.dataList.add(average);
            }
        }
        this.queue.add(new SensorValue(timestamp, heartRate));
        return (int)average.getValue();
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
        long newTimestamp = timestamp / (1000 * 1000);
        if (sensorValue[0] != this.currentValue.getValue()) {
            this.listener.onHeartRateChanged(newTimestamp, (int)sensorValue[0]);
            if (this.started) {
                this.storeHeartRate(newTimestamp, sensorValue[0]);
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
