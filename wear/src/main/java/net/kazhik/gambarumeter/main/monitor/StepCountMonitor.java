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

    private SensorManager sensorManager;
    private SensorValueListener listener;

    private Sensor stepCountSensor;
    private float initialValue = 0;
    private float prevValue = 0;
    private boolean started = false;
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
        this.prevValue = 0;
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

    public void saveResult(SQLiteDatabase db, long startTime) {
        StepCountTable stepCountTable = new StepCountTable(this.context, db);

        for (SensorValue sensorValue: this.dataList) {
            stepCountTable.insert(
                    sensorValue.getTimestamp(),
                    startTime,
                    (int) sensorValue.getValue());
        }

    }
    public DataMap putDataMap(DataMap dataMap) {
        ArrayList<DataMap> stepCountDataMapList = new ArrayList<>();
        for (SensorValue sensorValue: this.dataList) {
            DataMap stepCountMap = new DataMap();
            stepCountMap.putLong(DataStorage.COL_TIMESTAMP, sensorValue.getTimestamp());
            stepCountMap.putLong(DataStorage.COL_STEP_COUNT, (int)sensorValue.getValue());
            stepCountDataMapList.add(stepCountMap);
        }
        dataMap.putDataMapArrayList(DataStorage.TBL_STEPCOUNT, stepCountDataMapList);
        return dataMap;
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
        if (!this.started) {
            return;
        }
        if (this.initialValue == 0) {
            this.initialValue = newValue;
        } else if (newValue != this.currentValue.getValue()) {
            float steps = newValue - this.initialValue;
            Log.d(TAG, "onSensorChanged: " + newTimestamp);
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
