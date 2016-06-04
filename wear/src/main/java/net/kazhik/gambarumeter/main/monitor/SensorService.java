package net.kazhik.gambarumeter.main.monitor;

import android.app.Service;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by kazhik on 15/04/21.
 */
public abstract class SensorService extends Service implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor sensor;
    private Runnable saveDataRunnable = new Runnable() {
        @Override
        public void run() {
            storeCurrentValue(System.currentTimeMillis());
        }
    };

    private ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor();

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

        this.scheduler.scheduleAtFixedRate(this.saveDataRunnable,0,
                60, TimeUnit.SECONDS);

        return true;
    }

    public void terminate() {
        this.scheduler.shutdown();

        if (this.sensorManager == null) {
            return;
        }
        this.sensorManager.unregisterListener(this, this.sensor);

    }

    abstract protected void onSensorEvent(long timestamp, float[] sensorValues, int accuracy);

    public void storeCurrentValue(long timestamp) {

    }

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
