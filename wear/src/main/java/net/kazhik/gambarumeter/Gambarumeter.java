package net.kazhik.gambarumeter;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.SQLException;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.kazhik.gambarumeter.monitor.HeartRateMonitor;
import net.kazhik.gambarumeter.monitor.SensorValue;
import net.kazhik.gambarumeter.monitor.SensorValueListener;
import net.kazhik.gambarumeter.monitor.StepCountMonitor;
import net.kazhik.gambarumeter.monitor.Stopwatch;
import net.kazhik.gambarumeter.storage.DataStorage;
import net.kazhik.gambarumeter.storage.HeartRateTable;
import net.kazhik.gambarumeter.storage.WorkoutTable;

import java.util.List;

public class Gambarumeter extends Activity
        implements Stopwatch.OnTickListener,
        SensorValueListener,
        ServiceConnection,
        UserInputManager.UserInputListener {

    private SensorManager sensorManager;

    private Stopwatch stopwatch;
    private HeartRateMonitor heartRateMonitor;
    private StepCountMonitor stepCountMonitor = new StepCountMonitor();
//    private UserProfileMonitor userProfileMonitor = new UserProfileMonitor();

    private SplitTimeView splitTimeView = new SplitTimeView();
    private HeartRateView heartRateView = new HeartRateView();
    private StepCountView stepCountView = new StepCountView();

    private NotificationView notificationView = new NotificationView();

    private UserInputManager userInputManager;

    private static final String TAG = "Gambarumeter";

    @Override
    public void onUserStart() {
        this.startWorkout();
    }

    @Override
    public void onUserStop() {
        this.stopWorkout();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gambarumeter);

        this.initializeUI();

        this.initializeSensor();

        this.initializeDatabase();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.stopWorkout();
        this.unbindService(this);
    }

    private void initializeSensor() {
        this.sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);

        List<Sensor> sensorList = this.sensorManager.getSensorList(Sensor.TYPE_ALL);
        for (Sensor sensor: sensorList) {
            Log.i(TAG, "Sensor:" + sensor.getName() + "; " + sensor.getType());
            switch (sensor.getType()) {
                case Sensor.TYPE_HEART_RATE:
                    Intent intent = new Intent(this, HeartRateMonitor.class);
                    this.bindService(intent, this, Context.BIND_AUTO_CREATE);
                    break;
                case Sensor.TYPE_STEP_COUNTER:
                    this.stepCountMonitor.init(sensorManager, this);
                    break;
                default:
                    break;
            }
        }

        this.stopwatch = new Stopwatch(1000L, this);

//        this.userProfileMonitor.init(sensorManager);
    }
    private void initializeUI() {
        this.splitTimeView.initialize((TextView) this.findViewById(R.id.split_time));
        this.heartRateView.initialize((TextView)this.findViewById(R.id.bpm));
        this.stepCountView.initialize((TextView)this.findViewById(R.id.stepcount_value));

        this.notificationView.initialize(this);

        this.userInputManager = new UserInputManager(this)
                .initTouch(this, (LinearLayout)this.findViewById(R.id.main_layout))
                .initButtons(
                        (ImageButton)this.findViewById(R.id.start),
                        (ImageButton)this.findViewById(R.id.stop)
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

        long stopTime = this.stopwatch.stop();
        if (this.heartRateMonitor != null) {
            this.heartRateMonitor.stop();
        }
        if (this.stepCountMonitor != null) {
            this.stepCountMonitor.stop();
        }
//        this.userProfileMonitor.stop();

        this.notificationView.dismiss();

        this.saveResult();
        this.stopwatch.reset();
    }
    private void initializeDatabase() {
        try {
            DataStorage storage = new DataStorage(this);
            storage.open();
            storage.close();

            WorkoutTable workoutTable = new WorkoutTable(this);
            workoutTable.open(false);
            workoutTable.deleteAll();
            workoutTable.close();

            HeartRateTable heartRateTable = new HeartRateTable(this);
            heartRateTable.open(false);
            heartRateTable.deleteAll();
            heartRateTable.close();

        } catch (SQLException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }
    private void saveResult() {
        int ret;
        try {
            long startTime = this.stopwatch.getStartTime();

            WorkoutTable workoutTable = new WorkoutTable(this);
            workoutTable.open(false);
            ret = workoutTable.insert(
                    startTime,
                    this.stopwatch.getStopTime(),
                    this.stepCountMonitor.getStepCount());
            workoutTable.close();

            Log.d(TAG, "insert: " + ret);

            HeartRateTable heartRateTable = new HeartRateTable(this);
            heartRateTable.open(false);
            for (SensorValue sensorValue: this.heartRateMonitor.getDataList()) {
                heartRateTable.insert(
                        sensorValue.getTimestamp(),
                        startTime,
                        (int)sensorValue.getValue());
            }
            heartRateTable.close();

        } catch (SQLException e) {
            Log.e(TAG, e.getMessage(), e);
        }

        this.readDatabase();
    }

    private void readDatabase() {
        try {
            WorkoutTable workoutTable = new WorkoutTable(this);
            workoutTable.open(true);
            List<String> startTimeList = workoutTable.selectAll(0);
            Log.d(TAG, "startTimeList: " + startTimeList.size());
            for (String startTime: startTimeList) {
                Log.d(TAG, "startTime: " + startTime);
            }
            workoutTable.close();

            HeartRateTable heartRateTable = new HeartRateTable(this);
            heartRateTable.open(true);
            heartRateTable.close();


        } catch (SQLException e) {
            Log.e(TAG, e.getMessage(), e);
        }

    }

    @Override
    public void onHeartRateChanged(long timestamp, int rate) {
        this.heartRateView.setCurrentRate(rate);
        this.runOnUiThread(this.heartRateView);

        Log.d(TAG, "new heart rate: " + rate);
        this.notificationView.updateHeartRate(rate);

    }

    @Override
    public void onStepCountChanged(long timestamp, int stepCount) {
        this.stepCountView.setStepCount(stepCount);
        this.runOnUiThread(this.stepCountView);

        Log.d(TAG, "step count: " + stepCount);

        this.notificationView.updateStepCount(stepCount);
    }
    @Override
    public void onTick(long elapsed) {
        this.splitTimeView.setTime(elapsed);
        this.runOnUiThread(this.splitTimeView);

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
