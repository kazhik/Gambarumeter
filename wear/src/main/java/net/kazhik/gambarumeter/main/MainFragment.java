package net.kazhik.gambarumeter.main;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.wearable.view.DismissOverlayView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import net.kazhik.gambarumeter.R;
import net.kazhik.gambarumeter.main.monitor.BatteryLevelReceiver;
import net.kazhik.gambarumeter.main.monitor.Gyroscope;
import net.kazhik.gambarumeter.main.monitor.SensorValueListener;
import net.kazhik.gambarumeter.main.monitor.StepCountMonitor;
import net.kazhik.gambarumeter.main.monitor.Stopwatch;
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

    protected Stopwatch stopwatch;
    private StepCountMonitor stepCountMonitor;
    private BatteryLevelReceiver batteryLevelReceiver;
    private Gyroscope gyroscope;

    private boolean isBound = false;

    private UserInputManager userInputManager;
    private Vibrator vibrator;
    private MobileConnector mobileConnector = new MobileConnector();

    protected MainViewController mainViewController = new MainViewController();

    private static final String TAG = "MainFragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate: savedInstanceState = " + savedInstanceState);

        this.initializeSensor();

        Activity activity = this.getActivity();

        this.mobileConnector.initialize(activity);

        this.vibrator = (Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.main, container, false);
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.d(TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);

        this.initializeUI(0);

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        outState.putLong("start_time", this.stopwatch.getStartTime());

        super.onSaveInstanceState(outState);
//        Log.d(TAG, "onSaveInstanceState: " + outState.getLong("start_time"));
    }

    private void voiceAction() {
        String actionStatus =
                this.getActivity().getIntent().getStringExtra("actionStatus");
        if (actionStatus == null) {
            Log.d(TAG, "No voice command");
            return;
        }
        if (actionStatus.equals("ActiveActionStatus")) {
            if (!this.stopwatch.isRunning()) {
                this.startWorkout();
                this.userInputManager.toggleVisibility(true);
                long[] pattern = {0, 200, 400, 200, 400};
                this.vibrator.vibrate(pattern, -1);
            }

        } else if (actionStatus.equals("CompletedActionStatus")) {
            if (this.stopwatch.isRunning()) {
                this.userInputManager.toggleVisibility(false);
                this.vibrator.vibrate(1000);
                this.stop();
            }
        }

    }
    public boolean isBound() {
        return this.isBound;
    }
    public void setBound() {
        this.isBound = true;
    }

    @Override
    public void onDestroy() {
        this.stopWorkout();
        this.mobileConnector.terminate();

        this.terminateSensor();

        super.onDestroy();
    }

    protected void terminateSensor() {
        if (this.gyroscope != null) {
            this.gyroscope.terminate();
        }
        if (this.stepCountMonitor != null) {
            this.stepCountMonitor.terminate();
        }
        Activity activity = this.getActivity();
        if (this.isBound()) {
            activity.unbindService(this);

        }
        activity.unregisterReceiver(this.batteryLevelReceiver);
    }

    protected void initializeSensor() {
        Activity activity = this.getActivity();

        this.batteryLevelReceiver = new BatteryLevelReceiver(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.BATTERY_LOW");
        intentFilter.addAction("android.intent.action.BATTERY_OKAY");
        activity.registerReceiver(this.batteryLevelReceiver, intentFilter);

        SensorManager sensorManager =
                (SensorManager)activity.getSystemService(Activity.SENSOR_SERVICE);

        List<Sensor> sensorList = sensorManager.getSensorList(Sensor.TYPE_ALL);
        Intent intent;
        boolean bound;
        for (Sensor sensor: sensorList) {
            intent = null;
            Log.i(TAG, "Sensor:" + sensor.getName() + "; " + sensor.getType());
            switch (sensor.getType()) {
                case Sensor.TYPE_STEP_COUNTER:
                    intent = new Intent(activity, StepCountMonitor.class);
                    break;
                case Sensor.TYPE_GYROSCOPE:
                    intent = new Intent(activity, Gyroscope.class);

                    break;
                default:
                    break;
            }
            if (intent != null) {
                bound = this.getActivity().bindService(intent,
                        this, Context.BIND_AUTO_CREATE);
                if (bound) {
                    this.isBound = true;
                }
            }
        }

        this.stopwatch = new Stopwatch(1000L, this);

    }
    protected void initializeUI(int flags) {
        Activity activity = this.getActivity();

        this.mainViewController.initialize(activity, flags);

        this.userInputManager = new UserInputManager(this)
                .initTouch(activity,
                        (FrameLayout)activity.findViewById(R.id.main_layout))
                .initButtons(
                        (ImageButton)activity.findViewById(R.id.start),
                        (ImageButton)activity.findViewById(R.id.stop)
                );

    }

    protected void startWorkout() {

        this.mainViewController.clear();

        if (this.stepCountMonitor != null) {
            this.stepCountMonitor.start();
        }

        this.gyroscope.start();

        this.stopwatch.start();

        this.mainViewController.refreshView(this.getActivity());
    }
    protected long stopWorkout() {
        long stopTime = this.stopwatch.stop();
        if (this.stepCountMonitor != null) {
            this.stepCountMonitor.stop(stopTime);
        }
        this.gyroscope.stop(stopTime);

        return stopTime;
    }
    protected abstract void saveResult();

    public void saveResult(SQLiteDatabase db, long startTime) {
        if (this.stepCountMonitor != null) {
            this.stepCountMonitor.saveResult(db, startTime);
        }
    }
    public int getStepCount() {
        int stepCount = 0;
        if (this.stepCountMonitor != null) {
            stepCount = this.stepCountMonitor.getStepCount();
        }
        return stepCount;
    }
    protected void storeCurrentStepCount(long timestamp) {
        this.stepCountMonitor.storeCurrentValue(timestamp);

    }

    private void stop() {

        this.stopWorkout();
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                saveResult();
                long startTime = stopwatch.getStartTime();
                mobileConnector.sync(startTime);
                stopwatch.reset();
            }
        });

    }

    // UserInputManager.UserInputListener
    @Override
    public void onUserStart() {
        this.startWorkout();
    }

    // UserInputManager.UserInputListener
    @Override
    public void onUserStop() {
        this.stop();
    }
    // UserInputManager.UserInputListener
    @Override
    public void onUserDismiss() {
        DismissOverlayView dismissOverlay =
                (DismissOverlayView) getActivity().findViewById(R.id.dismiss_overlay);

        dismissOverlay.show();
    }
    // SensorValueListener
    @Override
    public void onRotation(long timestamp) {
        if (this.stopwatch.isRunning()) {
            this.userInputManager.toggleVisibility(false);
            this.vibrator.vibrate(1000);

            this.onUserStop();
        } else {
            this.onUserStart();
        }
    }
    // SensorValueListener
    @Override
    public void onStepCountChanged(long timestamp, int stepCount) {
        if (!this.stopwatch.isRunning()) {
            return;
        }
        this.mainViewController.setStepCount(stepCount);
    }

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
        this.mainViewController.setSplitTime(elapsed);
        this.mainViewController.refreshView(this.getActivity());
    }

    // ServiceConnection
    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        Log.d(TAG, "onServiceConnected: " + componentName.toString());

        if (iBinder instanceof Gyroscope.GyroBinder) {
            this.gyroscope =
                    ((Gyroscope.GyroBinder) iBinder).getService();
            this.gyroscope.initialize(this);
        } else if (iBinder instanceof StepCountMonitor.StepCountBinder) {
            this.stepCountMonitor =
                    ((StepCountMonitor.StepCountBinder) iBinder).getService();
            this.stepCountMonitor.init(this.getActivity(), this);
        }

        if (isServiceReady()) {
            this.voiceAction();
        }
    }
    // ServiceConnection
    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        Log.d(TAG, "onServiceDisconnected: " + componentName.toString());

    }

    protected boolean isServiceReady() {
        return (this.gyroscope != null && this.stepCountMonitor != null);
    }

    @Override
    public void onStart() {
        Log.d(TAG, "onStart");
        super.onStart();
        this.mobileConnector.connect();
        this.voiceAction();
    }

    @Override
    public void onStop() {
        Log.d(TAG, "onStop");
        this.mobileConnector.disconnect();
        super.onStop();
    }

}
