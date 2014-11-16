package net.kazhik.gambarumeter.main;

import android.app.Activity;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.SQLException;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.kazhik.gambarumeter.R;
import net.kazhik.gambarumeter.monitor.HeartRateMonitor;
import net.kazhik.gambarumeter.monitor.SensorValue;
import net.kazhik.gambarumeter.monitor.SensorValueListener;
import net.kazhik.gambarumeter.monitor.StepCountMonitor;
import net.kazhik.gambarumeter.monitor.Stopwatch;
import net.kazhik.gambarumeter.storage.HeartRateTable;
import net.kazhik.gambarumeter.storage.WorkoutTable;
import net.kazhik.gambarumeter.view.HeartRateView;
import net.kazhik.gambarumeter.view.NotificationView;
import net.kazhik.gambarumeter.view.SplitTimeView;
import net.kazhik.gambarumeter.view.StepCountView;

import java.util.List;

/**
 * Created by kazhik on 14/11/11.
 */
public class MainFragment extends Fragment
        implements Stopwatch.OnTickListener,
        SensorValueListener,
        ServiceConnection,
        UserInputManager.UserInputListener {
    private SensorManager sensorManager;

    private Stopwatch stopwatch;
    private HeartRateMonitor heartRateMonitor;
    private StepCountMonitor stepCountMonitor;
//    private UserProfileMonitor userProfileMonitor = new UserProfileMonitor();

    private SplitTimeView splitTimeView = new SplitTimeView();
    private HeartRateView heartRateView = new HeartRateView();
    private StepCountView stepCountView = new StepCountView();

    private NotificationView notificationView = new NotificationView();

    private UserInputManager userInputManager;

    private static final String TAG = "MainCardFragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate");

        this.initializeSensor();

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        this.initializeUI();
    }

    @Override
    public void onDestroy() {
        this.stopWorkout();
        if (this.heartRateMonitor != null) {
            this.getActivity().getApplicationContext().unbindService(this);
        }

        super.onDestroy();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        return inflater.inflate(R.layout.main, container, false);
    }

    @Override
    public void onUserStart() {
        this.startWorkout();
    }

    @Override
    public void onUserStop() {
        this.stopWorkout();

        this.saveResult();
        this.stopwatch.reset();
    }
    private void initializeSensor() {
        this.sensorManager = (SensorManager)this.getActivity().getSystemService(Activity.SENSOR_SERVICE);

        List<Sensor> sensorList = this.sensorManager.getSensorList(Sensor.TYPE_ALL);
        for (Sensor sensor: sensorList) {
            Log.i(TAG, "Sensor:" + sensor.getName() + "; " + sensor.getType());
            switch (sensor.getType()) {
                case Sensor.TYPE_HEART_RATE:
                    Intent intent = new Intent(this.getActivity(), HeartRateMonitor.class);
                    this.getActivity().getApplicationContext().bindService(intent, this, Context.BIND_AUTO_CREATE);
                    break;
                case Sensor.TYPE_STEP_COUNTER:
                    this.stepCountMonitor = new StepCountMonitor();
                    this.stepCountMonitor.init(this.sensorManager, this);
                    break;
                default:
                    break;
            }
        }

        this.stopwatch = new Stopwatch(1000L, this);

//        this.userProfileMonitor.init(sensorManager);
    }
    private void initializeUI() {
        Activity activity = this.getActivity();

        this.splitTimeView.initialize((TextView) activity.findViewById(R.id.split_time));
        this.heartRateView.initialize((TextView)activity.findViewById(R.id.bpm));
        this.stepCountView.initialize((TextView)activity.findViewById(R.id.stepcount_value));

        this.notificationView.initialize(activity);

        this.userInputManager = new UserInputManager(this)
                .initTouch(activity, (LinearLayout)activity.findViewById(R.id.main_layout))
                .initButtons(
                        (ImageButton)activity.findViewById(R.id.start),
                        (ImageButton)activity.findViewById(R.id.stop)
                );

    }
    private void startWorkout() {
        this.heartRateView.setCurrentRate(0)
                .refresh();
        this.stepCountView.setStepCount(0)
                .refresh();
        this.splitTimeView.setTime(0)
                .refresh();

        this.stopwatch.start();
        if (this.heartRateMonitor != null) {
            this.heartRateMonitor.start();
        }
        if (this.stepCountMonitor != null) {
            this.stepCountMonitor.start();
        }
//        this.userProfileMonitor.start();
    }
    private void stopWorkout() {

        this.stopwatch.stop();
        if (this.heartRateMonitor != null) {
            this.heartRateMonitor.stop();
        }
        if (this.stepCountMonitor != null) {
            this.stepCountMonitor.stop();
        }
//        this.userProfileMonitor.stop();

        this.notificationView.dismiss();

    }

    private void saveResult() {
        int ret;
        try {
            long startTime = this.stopwatch.getStartTime();
            int stepCount = 0;
            if (this.stepCountMonitor != null) {
                stepCount = this.stepCountMonitor.getStepCount();
            }

            WorkoutTable workoutTable = new WorkoutTable(this.getActivity());
            workoutTable.open(false);
            ret = workoutTable.insert(
                    startTime,
                    this.stopwatch.getStopTime(),
                    stepCount);
            workoutTable.close();

            Log.d(TAG, "insert: " + ret + "; " + startTime);

            if (this.heartRateMonitor != null) {
                HeartRateTable heartRateTable = new HeartRateTable(this.getActivity());
                heartRateTable.open(false);
                for (SensorValue sensorValue: this.heartRateMonitor.getDataList()) {
                    heartRateTable.insert(
                            sensorValue.getTimestamp(),
                            startTime,
                            (int)sensorValue.getValue());
                }
                heartRateTable.close();
            }

        } catch (SQLException e) {
            Log.e(TAG, e.getMessage(), e);
        }

    }

    @Override
    public void onHeartRateChanged(long timestamp, int rate) {
        this.heartRateView.setCurrentRate(rate);
        this.getActivity().runOnUiThread(this.heartRateView);

        Log.d(TAG, "new heart rate: " + rate);
        this.notificationView.updateHeartRate(rate);

    }

    @Override
    public void onStepCountChanged(long timestamp, int stepCount) {
        this.stepCountView.setStepCount(stepCount);
        this.getActivity().runOnUiThread(this.stepCountView);

        Log.d(TAG, "step count: " + stepCount);

        this.notificationView.updateStepCount(stepCount);
    }
    @Override
    public void onTick(long elapsed) {
        this.splitTimeView.setTime(elapsed);
        this.getActivity().runOnUiThread(this.splitTimeView);

        this.notificationView.show(elapsed);
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        Log.d(TAG, "onServiceConnected: " + componentName.toString());

        if (iBinder instanceof HeartRateMonitor.HeartRateBinder) {
            this.heartRateMonitor = ((HeartRateMonitor.HeartRateBinder)iBinder).getService();
            this.heartRateMonitor.init(sensorManager, this);
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        Log.d(TAG, "onServiceDisconnected: " + componentName.toString());

    }


}
