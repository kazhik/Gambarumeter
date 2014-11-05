package net.kazhik.gambarumeter.monitor;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/** 
 * Created by kazhik on 14/10/12.
 */
public class UserProfileMonitor implements SensorEventListener {
    private SensorManager sensorManager;

    private Sensor userProfileSensor;

    public void init(SensorManager sensorManager) {

        this.sensorManager = sensorManager;
        this.userProfileSensor = this.sensorManager.getDefaultSensor(65539);

    }
    public void start() {
        this.sensorManager.registerListener(this, this.userProfileSensor, SensorManager.SENSOR_DELAY_NORMAL);

    }
    public void stop() {
        this.sensorManager.unregisterListener(this, this.userProfileSensor);

    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        for (int i = 0; i < sensorEvent.values.length; i++) {
            if (sensorEvent.values[i] != 0) {
                System.out.println("user profile value[" + i + "]: " + sensorEvent.values[i]);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
