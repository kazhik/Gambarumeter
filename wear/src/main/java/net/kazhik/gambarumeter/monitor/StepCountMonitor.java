package net.kazhik.gambarumeter.monitor;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import net.kazhik.gambarumeter.entity.SensorValue;

/**
 * Created by kazhik on 14/10/12.
 */
public class StepCountMonitor implements SensorEventListener {

    private SensorManager sensorManager;
    private SensorValueListener listener;

    private Sensor stepCountSensor;
    private int initialValue = 0;
    private SensorValue currentValue = new SensorValue();

    public void init(SensorManager sensorManager, SensorValueListener listener) {

        this.sensorManager = sensorManager;
        this.listener = listener;
        this.stepCountSensor = this.sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

    }
    public int getStepCount() {
        return ((int)this.currentValue.getValue()) - this.initialValue;
    }
    public void start() {
        this.initialValue = 0;
        this.sensorManager.registerListener(this, this.stepCountSensor, SensorManager.SENSOR_DELAY_NORMAL);

    }
    public void stop() {
        this.sensorManager.unregisterListener(this, this.stepCountSensor);

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
        int newValue = (int)sensorEvent.values[0];
        if (this.initialValue == 0) {
            this.initialValue = newValue;
        } else if (newValue != this.currentValue.getValue()) {
            this.listener.onStepCountChanged(sensorEvent.timestamp, newValue - this.initialValue);
        }
        this.currentValue.setTimestamp(sensorEvent.timestamp);
        this.currentValue.setValue(newValue);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
