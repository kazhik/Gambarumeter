package net.kazhik.gambarumeter.main.monitor;

import android.app.Service;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * Created by kazhik on 15/04/21.
 */
public abstract class SensorService extends Service implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor sensor;
    private static final String TAG = "Gyroscope";

    public boolean initialize(SensorManager sensorManager,
                           int sensorType) {

        this.sensor = sensorManager.getDefaultSensor(sensorType);
        if (this.sensor == null) {
            return false;
        }
        this.sensorManager = sensorManager;

        this.sensorManager.registerListener(this,
                this.sensor,
                SensorManager.SENSOR_DELAY_NORMAL);

        return true;
    }

    public void terminate() {
        if (this.sensorManager == null) {
            return;
        }
        this.sensorManager.unregisterListener(this, this.sensor);

    }

    abstract protected void onSensorEvent(long timestamp, float[] sensorValues, int accuracy);

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        this.onSensorEvent(sensorEvent.timestamp,
                sensorEvent.values,
                sensorEvent.accuracy);

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

}
