package net.kazhik.gambarumeter.main.monitor;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.wearable.DataMap;

import net.kazhik.gambarumeterlib.entity.SensorValue;
import net.kazhik.gambarumeterlib.storage.DataStorage;
import net.kazhik.gambarumeterlib.storage.StepCountTable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kazhik on 14/10/12.
 */
public class StepCountMonitor extends SensorService {
    private Context context;
    private SensorValueListener listener;

    private float initialValue = 0;
    private float prevValue = 0;
    private SensorValue currentValue = new SensorValue(0, 0f);
    private List<SensorValue> dataList = new ArrayList<>();
    private StepCountBinder binder = new StepCountBinder();
    private static final String TAG = "StepCountMonitor";

    public class StepCountBinder extends Binder {
        public StepCountMonitor getService() {
            return StepCountMonitor.this;
        }
    }
    public void init(Context context,
                     SensorManager sensorManager,
                     SensorValueListener listener) {

        Log.d(TAG, "initialize");
        this.context = context;
        this.listener = listener;

        super.initialize(sensorManager, Sensor.TYPE_STEP_COUNTER);
    }

    public int getStepCount() {
        return (int)(this.currentValue.getValue() - this.initialValue);
    }

    @Override
    public void start() {
        super.start();

        this.initialValue = 0;
        this.prevValue = 0;
        this.dataList.clear();

    }
    @Override
    public void stop(long stopTime) {
        super.stop(stopTime);

    }

    public void saveResult(SQLiteDatabase db, long startTime) {
        StepCountTable stepCountTable = new StepCountTable(this.context, db);

        for (SensorValue sensorValue: this.dataList) {
            stepCountTable.insert(
                    sensorValue.getTimestamp(),
                    startTime,
                    (int) sensorValue.getValue());
        }

    }
    private long getLastTimestamp() {
        long lastTimestamp = 0;
        if (!this.dataList.isEmpty()) {
            lastTimestamp = this.dataList.get(this.dataList.size() - 1).getTimestamp();
        }
        return lastTimestamp;
    }

    @Override
    public void storeCurrentValue(long timestamp) {
        if (this.currentValue.getValue() > this.prevValue ) {
            float steps = this.currentValue.getValue() - this.initialValue;
            this.dataList.add(new SensorValue(timestamp, steps));
            this.prevValue = steps;
        }

    }

    @Override
    protected void onSensorEvent(long timestamp, float[] sensorValues, int accuracy) {
        switch (accuracy) {
            case SensorManager.SENSOR_STATUS_ACCURACY_HIGH:
            case SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM:
            case SensorManager.SENSOR_STATUS_ACCURACY_LOW:
                break;
            default:
                return;
        }

        long newTimestamp = System.currentTimeMillis();
        float newValue = sensorValues[0];
        if (!this.isStarted()) {
            return;
        }
        if (this.initialValue == 0) {
            this.initialValue = newValue;
        } else if (newValue != this.currentValue.getValue()) {
            float steps = newValue - this.initialValue;
            this.listener.onStepCountChanged(newTimestamp, (int)steps);
        }
        this.currentValue.setTimestamp(newTimestamp)
                .setValue(newValue);

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return this.binder;
    }
}
