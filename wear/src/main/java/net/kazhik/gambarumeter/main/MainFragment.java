package net.kazhik.gambarumeter.main;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.SQLException;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.kazhik.gambarumeter.R;
import net.kazhik.gambarumeter.main.monitor.BatteryLevelReceiver;
import net.kazhik.gambarumeter.main.monitor.SensorValueListener;
import net.kazhik.gambarumeter.main.monitor.StepCountMonitor;
import net.kazhik.gambarumeter.main.monitor.Stopwatch;
import net.kazhik.gambarumeter.main.view.SplitTimeView;
import net.kazhik.gambarumeter.main.view.StepCountView;
import net.kazhik.gambarumeter.pager.PagerFragment;

import java.util.List;

/**
 * Created by kazhik on 14/11/11.
 */
public abstract class MainFragment extends PagerFragment
        implements Stopwatch.OnTickListener,
        SensorValueListener,
        ServiceConnection,
        UserInputManager.UserInputListener {
    private SensorManager sensorManager;

    protected Stopwatch stopwatch;
    protected StepCountMonitor stepCountMonitor;
    private BatteryLevelReceiver batteryLevelReceiver;

    private SplitTimeView splitTimeView = new SplitTimeView();
    private StepCountView stepCountView = new StepCountView();

    protected SharedPreferences prefs;

    private UserInputManager userInputManager;

    private static final String TAG = "MainFragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate:" + savedInstanceState);

        this.initializeSensor();

        this.prefs =
                PreferenceManager.getDefaultSharedPreferences(this.getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        return inflater.inflate(R.layout.main, container, false);
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Log.d(TAG, "onActivityCreated: ");

        this.initializeUI();

        this.voiceAction(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        outState.putLong("start_time", this.stopwatch.getStartTime());

        super.onSaveInstanceState(outState);
        Log.d(TAG, "onSaveInstanceState: " + outState.getLong("start_time"));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        Log.d(TAG, "onDestroyView: ");
    }

    private void voiceAction(Bundle savedInstanceState) {
        String actionStatus =
                this.getActivity().getIntent().getStringExtra("actionStatus");
        if (actionStatus == null) {
            return;
        }

        if (actionStatus.equals("ActiveActionStatus")) {
            this.startWorkout();
        } else if (actionStatus.equals("CompletedActionStatus")) {
            if (savedInstanceState == null) {
                Log.d(TAG, "savedInstanceState is null");
                return;
            }
            if (savedInstanceState.getLong("start_time") == 0) {
                Log.d(TAG, "Not started:");
                return;
            }
            Log.d(TAG, "workout stop");
            this.stopWorkout();
        }

    }

    @Override
    public void onDestroy() {
        this.stopWorkout();

        super.onDestroy();
    }

    protected void initializeSensor() {
        Activity activity = this.getActivity();

        this.batteryLevelReceiver = new BatteryLevelReceiver(this);
        
        this.sensorManager =
                (SensorManager)activity.getSystemService(Activity.SENSOR_SERVICE);

        List<Sensor> sensorList = this.sensorManager.getSensorList(Sensor.TYPE_ALL);
        for (Sensor sensor: sensorList) {
            Log.i(TAG, "Sensor:" + sensor.getName() + "; " + sensor.getType());
            switch (sensor.getType()) {
                case Sensor.TYPE_STEP_COUNTER:
                    this.stepCountMonitor = new StepCountMonitor();
                    this.stepCountMonitor.init(activity, this.sensorManager, this);
                    break;
                default:
                    break;
            }
        }


        this.stopwatch = new Stopwatch(1000L, this);

    }
    protected void initializeUI() {
        Activity activity = this.getActivity();

        this.splitTimeView.initialize((TextView)activity.findViewById(R.id.split_time));
        this.stepCountView.initialize((TextView)activity.findViewById(R.id.stepcount_value));

        this.userInputManager = new UserInputManager(this)
                .initTouch(activity,
                        (LinearLayout)activity.findViewById(R.id.main_layout))
                .initButtons(
                        (ImageButton)activity.findViewById(R.id.start),
                        (ImageButton)activity.findViewById(R.id.stop)
                );

    }

    protected void startWorkout() {

        if (this.stepCountMonitor != null) {
            this.stepCountView.setStepCount(0)
                    .refresh();
            this.stepCountMonitor.start();
        }
        this.splitTimeView.setTime(0)
                .refresh();

        this.stopwatch.start();
    }
    protected void stopWorkout() {

        this.stopwatch.stop();
        if (this.stepCountMonitor != null) {
            this.stepCountMonitor.stop();
        }

    }

    protected void saveResult() {
        try {
            long startTime = this.stopwatch.getStartTime();

            // StepCountTable
            if (this.stepCountMonitor != null) {
                this.stepCountMonitor.saveResult(startTime);
            }

        } catch (SQLException e) {
            Log.e(TAG, e.getMessage(), e);
        }

    }

    // UserInputManager.UserInputListener
    @Override
    public void onUserStart() {
        this.startWorkout();
    }

    // UserInputManager.UserInputListener
    @Override
    public void onUserStop() {
        this.stopWorkout();

        this.saveResult();
        this.stopwatch.reset();
    }
    // SensorValueListener
    @Override
    public void onStepCountChanged(long timestamp, int stepCount) {
        if (!this.stopwatch.isRunning()) {
            return;
        }
        this.stepCountView.setStepCount(stepCount);
        this.getActivity().runOnUiThread(this.stepCountView);

        this.updateStepCount(stepCount);
    }
    protected abstract void updateStepCount(int stepCount);

    // SensorValueListener
    @Override
    public void onBatteryLow() {
    }

    // SensorValueListener
    @Override
    public void onBatteryOkay() {
    }

    // Stopwatch.OnTickListener
    @Override
    public void onTick(long elapsed) {
        this.splitTimeView.setTime(elapsed);
        this.getActivity().runOnUiThread(this.splitTimeView);

        this.showNotification(elapsed);
    }
    protected abstract void showNotification(long elapsed);

    // ServiceConnection
    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        Log.d(TAG, "onServiceDisconnected: " + componentName.toString());

    }


}
