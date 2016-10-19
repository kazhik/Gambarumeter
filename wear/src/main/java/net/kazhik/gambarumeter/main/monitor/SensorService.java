package net.kazhik.gambarumeter.main.monitor;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.PowerManager;
import android.util.Log;

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
            handler.postDelayed(saveDataRunnable, TimeUnit.SECONDS.toMillis(60));
            }
    };
    private PowerManager.WakeLock wakeLock;
    private Handler handler;
    private HandlerThread saveDataThread = null;
    private static final String TAG = "SensorService";

    public boolean initialize(int sensorType) {

        this.sensorManager = (SensorManager)this.getSystemService(SENSOR_SERVICE);
        if (this.sensorManager == null) {
            Log.w(TAG, "Failed to get service: SENSOR_SERVICE");
            return false;
        }
        this.sensor = this.sensorManager.getDefaultSensor(sensorType);
        if (this.sensor == null) {
            Log.w(TAG, "Failed to get sensor: " + sensorType);
            return false;
        }
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        this.wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "MyWakelockTag");
        this.wakeLock.acquire();


        return true;
    }

    public void terminate() {
        if (this.wakeLock != null) {
            this.wakeLock.release();
        }

        if (this.sensorManager == null) {
            return;
        }

        this.stopSelf();
    }

    public void start() {
        boolean ret = this.sensorManager.registerListener(this,
                this.sensor,
                SensorManager.SENSOR_DELAY_NORMAL);
        if (!ret) {
            Log.d(TAG, "Failed to registerListener: " + this.sensor);
            return;
        }
        if (!this.isStarted()) {
            this.saveDataThread = new HandlerThread("SaveDataThread");
            this.saveDataThread.start();
        }

        this.handler = new Handler(this.saveDataThread.getLooper());
        this.handler.postDelayed(this.saveDataRunnable,
                TimeUnit.SECONDS.toMillis(60));

    }
    public void stop(long stopTime) {
        this.sensorManager.unregisterListener(this, this.sensor);

        if (this.isStarted()) {
            this.saveDataThread.quit();
            this.saveDataThread = null;
        }
    }
    public boolean isStarted() {
        return (this.saveDataThread != null &&
                this.saveDataThread.getState() == Thread.State.RUNNABLE);
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

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }
}
