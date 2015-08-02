package net.kazhik.gambarumeter.main.monitor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import net.kazhik.gambarumeterlib.entity.SensorValue;
import net.kazhik.gambarumeterlib.storage.StepCountTable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kazhik on 14/10/12.
 */
public class StepCountMonitor implements SensorEventListener {
    private Context context;

    private SensorManager sensorManager;
    private SensorValueListener listener;

    private Sensor stepCountSensor;
    private float initialValue = 0;
    private boolean started = false;
    private SensorValue currentValue = new SensorValue(0, 0f);
    private List<SensorValue> dataList = new ArrayList<SensorValue>();
    private static final String TAG = "StepCountMonitor";

    public void init(Context context,
                     SensorManager sensorManager,
                     SensorValueListener listener) {

        this.context = context;
        this.sensorManager = sensorManager;
        this.listener = listener;
        this.stepCountSensor =
                this.sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

    }
    public int getStepCount() {
        return (int)(this.currentValue.getValue() - this.initialValue);
    }
    public void start() {
        this.initialValue = 0;
        this.dataList.clear();
        this.sensorManager.registerListener(this,
                this.stepCountSensor,
                SensorManager.SENSOR_DELAY_NORMAL);

        this.started = true;
    }
    public void stop() {
        this.started = false;
        this.sensorManager.unregisterListener(this, this.stepCountSensor);

    }
    public void saveResult(long startTime) {
        StepCountTable stepCountTable = new StepCountTable(this.context);
        stepCountTable.open(false);
        
        for (SensorValue sensorValue: this.dataList) {
            stepCountTable.insert(
                    sensorValue.getTimestamp(),
                    startTime,
                    (int)sensorValue.getValue());
        }
        stepCountTable.close();

    }
    private long getLastTimestamp() {
        long lastTimestamp = 0;
        if (!this.dataList.isEmpty()) {
            lastTimestamp = this.dataList.get(this.dataList.size() - 1).getTimestamp();
        }
        return lastTimestamp;
    }
    public void storeCurrentValue(long timestamp) {
        long lastTimestamp = this.getLastTimestamp();
        if (lastTimestamp != timestamp && this.currentValue.getValue() != 0f) {
            float steps = this.currentValue.getValue() - this.initialValue;
            this.dataList.add(new SensorValue(timestamp, steps));
        }

    }
    private void storeStepCount(long timestamp, float stepCount) {
        long lastTimestamp = this.getLastTimestamp();
        if (timestamp >= lastTimestamp + (1000 * 60)) {
            this.dataList.add(new SensorValue(timestamp, stepCount));
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
        float newValue = sensorEvent.values[0];
        Log.d(TAG, "Steps: " + newValue);
        if (!this.started) {
            return;
        }
        if (this.initialValue == 0) {
            this.initialValue = newValue;
        } else if (newValue != this.currentValue.getValue()) {
            float steps = newValue - this.initialValue;
            this.listener.onStepCountChanged(newTimestamp, (int)steps);
            this.storeStepCount(newTimestamp, steps);
        }
        this.currentValue.setTimestamp(newTimestamp)
            .setValue(newValue);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
