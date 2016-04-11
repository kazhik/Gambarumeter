package net.kazhik.gambarumeter.main;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.wearable.DataMap;

import net.kazhik.gambarumeter.R;
import net.kazhik.gambarumeter.main.monitor.HeartRateMonitor;
import net.kazhik.gambarumeter.main.monitor.HeartRateSensorValueListener;
import net.kazhik.gambarumeter.main.view.HeartRateView;
import net.kazhik.gambarumeterlib.storage.DataStorage;

import java.util.Date;

/**
 * Created by kazhik on 16/01/21.
 */
public class FullMainFragment extends GpsMainFragment
        implements HeartRateSensorValueListener {

    private SensorManager sensorManager;
    private HeartRateView heartRateView = new HeartRateView();
    private HeartRateMonitor heartRateMonitor;
    private static final String TAG = "FullMainFragment";

    @Override
    public void onDestroy() {
        if (this.heartRateMonitor != null) {
            this.heartRateMonitor.terminate();
        }
        super.onDestroy();

    }
    protected void initializeSensor() {
        super.initializeSensor();

        Activity activity = this.getActivity();
        Context appContext = activity.getApplicationContext();

        this.sensorManager =
                (SensorManager)activity.getSystemService(Activity.SENSOR_SERVICE);

        Sensor sensor = this.sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
        if (sensor != null) {
            Intent intent = new Intent(activity, HeartRateMonitor.class);
            boolean bound = appContext.bindService(intent, this, Context.BIND_AUTO_CREATE);
            if (bound) {
                this.setBound();
            }
            this.heartRateMonitor = new HeartRateMonitor(); // temporary
        }

    }
    protected void initializeUI() {
        super.initializeUI();

        Activity activity = this.getActivity();

        if (this.heartRateMonitor != null) {
            this.heartRateView.initialize((TextView)activity.findViewById(R.id.bpm));
            activity.findViewById(R.id.heart_rate).setVisibility(View.VISIBLE);
            activity.findViewById(R.id.separator).setVisibility(View.VISIBLE);
        }


    }
    protected void startWorkout() {
        if (this.heartRateMonitor != null) {
            this.heartRateView.setCurrentRate(0)
                    .refresh();
            this.heartRateMonitor.start();
        }
        super.startWorkout();
    }
    protected void stopWorkout() {

        if (this.heartRateMonitor != null) {
            this.heartRateMonitor.stop();
        }

        super.stopWorkout();
    }
    @Override
    protected DataMap putData(DataMap dataMap) {
        dataMap = super.putData(dataMap);

        dataMap = this.heartRateMonitor.putData(dataMap);

        DataMap workoutDataMap = dataMap.getDataMap(DataStorage.TBL_WORKOUT);
        workoutDataMap.putInt(DataStorage.COL_HEART_RATE,
                this.heartRateMonitor.getAverageHeartRate());
        dataMap.putDataMap(DataStorage.TBL_WORKOUT, workoutDataMap);

        return dataMap;

    }

    // SensorValueListener
    @Override
    public void onHeartRateChanged(long timestamp, int rate) {
        if (!this.stopwatch.isRunning()) {
            return;
        }
        this.heartRateView.setCurrentRate(rate);
        this.getActivity().runOnUiThread(this.heartRateView);

        Log.d(TAG, "new heart rate: " + (new Date(timestamp)).toString() + " / " + rate);
//        this.notificationView.updateHeartRate(rate);

    }

    // ServiceConnection
    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        Log.d(TAG, "onServiceConnected: " + componentName.toString());

        if (iBinder instanceof HeartRateMonitor.HeartRateBinder) {
            this.heartRateMonitor =
                    ((HeartRateMonitor.HeartRateBinder)iBinder).getService();
            this.heartRateMonitor.init(this.getActivity(), sensorManager, this);
        }
        super.onServiceConnected(componentName, iBinder);

    }


}
