package net.kazhik.gambarumeter.main;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.SQLException;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import net.kazhik.gambarumeter.R;
import net.kazhik.gambarumeter.main.monitor.HeartRateMonitor;
import net.kazhik.gambarumeter.main.monitor.HeartRateSensorValueListener;
import net.kazhik.gambarumeter.main.view.HeartRateView;
import net.kazhik.gambarumeter.storage.WorkoutTable;

import java.util.Date;
import java.util.List;

/**
 * Created by kazhik on 14/11/11.
 */
public class HeartRateMainFragment extends MainFragment
        implements HeartRateSensorValueListener {
    private SensorManager sensorManager;

    private HeartRateMonitor heartRateMonitor;

    private HeartRateView heartRateView = new HeartRateView();

    private HeartRateNotificationView notificationView = new HeartRateNotificationView();

    private static final String TAG = "HeartRateMainFragment";


    @Override
    public void refreshView() {
        Log.d(TAG, "refreshView");
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (this.heartRateMonitor != null) {
            this.heartRateMonitor.terminate();
            this.getActivity().getApplicationContext().unbindService(this);
        }

    }

    protected void initializeSensor() {
        super.initializeSensor();
        
        Activity activity = this.getActivity();
        Context appContext = activity.getApplicationContext();

        this.sensorManager =
                (SensorManager)activity.getSystemService(Activity.SENSOR_SERVICE);

        List<Sensor> sensorList = this.sensorManager.getSensorList(Sensor.TYPE_ALL);
        for (Sensor sensor: sensorList) {
            Log.i(TAG, "Sensor:" + sensor.getName() + "; " + sensor.getType());
            switch (sensor.getType()) {
                case Sensor.TYPE_HEART_RATE:
                    Intent intent = new Intent(activity, HeartRateMonitor.class);
                    appContext.bindService(intent, this, Context.BIND_AUTO_CREATE);
                    this.heartRateMonitor = new HeartRateMonitor(); // temporary
                    break;
                default:
                    break;
            }
        }

    }
    protected void initializeUI() {
        super.initializeUI();
        
        Activity activity = this.getActivity();

        if (this.heartRateMonitor != null) {
            this.heartRateView.initialize((TextView)activity.findViewById(R.id.bpm));
            activity.findViewById(R.id.heart_rate).setVisibility(View.VISIBLE);
        } else {
            activity.findViewById(R.id.heart_rate).setVisibility(View.GONE);
        }

        this.notificationView.initialize(activity);

    }

    protected void startWorkout() {
        this.notificationView.clear();

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
        this.notificationView.dismiss();

        super.stopWorkout();
    }

    protected void saveResult() {
        super.saveResult();
        int ret;
        try {
            long startTime = this.stopwatch.getStartTime();

            // HeartRateTable
            int heartRate = 0;
            if (this.heartRateMonitor != null) {
                heartRate = this.heartRateMonitor.getAverageHeartRate();

                this.heartRateMonitor.saveResult(startTime);
            }

            // WorkoutTable
            int stepCount = 0;
            if (this.stepCountMonitor != null) {
                stepCount = this.stepCountMonitor.getStepCount();
            }

            WorkoutTable workoutTable = new WorkoutTable(this.getActivity());
            workoutTable.open(false);
            ret = workoutTable.insert(
                    startTime,
                    this.stopwatch.getStopTime(),
                    stepCount,
                    0,
                    heartRate);
            workoutTable.close();

            Log.d(TAG, "insert: " + ret + "; " + startTime);

        } catch (SQLException e) {
            Log.e(TAG, e.getMessage(), e);
        }

    }
    @Override
    protected void updateStepCount(int stepCount) {
        this.notificationView.updateStepCount(stepCount);

    }
    @Override
    protected void showNotification(long elapsed) {
        this.notificationView.show(elapsed);

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
        this.notificationView.updateHeartRate(rate);

        if (this.stepCountMonitor != null) {
            this.stepCountMonitor.storeCurrentValue(timestamp);
        }

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

    }


}
