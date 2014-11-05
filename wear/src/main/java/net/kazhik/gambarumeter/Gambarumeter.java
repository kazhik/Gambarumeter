package net.kazhik.gambarumeter;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import net.kazhik.gambarumeter.monitor.ControllerOnGesture;
import net.kazhik.gambarumeter.monitor.HeartRateMonitor;
import net.kazhik.gambarumeter.monitor.SensorValueListener;
import net.kazhik.gambarumeter.monitor.StepCountMonitor;
import net.kazhik.gambarumeter.monitor.Stopwatch;
import net.kazhik.gambarumeter.monitor.UserProfileMonitor;

import java.util.List;

public class Gambarumeter extends Activity
        implements Stopwatch.OnTickListener,
        SensorValueListener,
        ServiceConnection,
        ControllerOnGesture.GestureListener,
        View.OnTouchListener {

    private SensorManager sensorManager;

    private Stopwatch stopwatch;
    private HeartRateMonitor heartRateMonitor;
    private StepCountMonitor stepCountMonitor = new StepCountMonitor();
    private UserProfileMonitor userProfileMonitor = new UserProfileMonitor();

    private SplitTimeView splitTimeView = new SplitTimeView();
    private HeartRateView heartRateView = new HeartRateView();
    private StepCountView stepCountView = new StepCountView();

    private NotificationView notificationView = new NotificationView();

    private ControllerOnGesture controllerOnGesture;

    private ImageButton startButton;
    private ImageButton stopButton;

    private static final String TAG = "Gambarumeter";

    @Override
    public void onGestureStart() {
        this.startWorkout();
    }

    @Override
    public void onGestureStop() {
        this.stopWorkout();

    }

    private class StartButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            startWorkout();
        }

    }
    private class StopButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            stopWorkout();
        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gambarumeter);

        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                initializeUI(stub);
            }
        });

        this.initializeSensor();
        this.controllerOnGesture = new ControllerOnGesture(this, this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.stopWorkout();
        this.unbindService(this);
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        return this.controllerOnGesture.onTouchEvent(motionEvent);

    }

    private void initializeSensor() {
        this.sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);

        List<Sensor> sensorList = this.sensorManager.getSensorList(Sensor.TYPE_ALL);
        for (Sensor sensor: sensorList) {
            Log.i(TAG, "Sensor:" + sensor.getName() + "; " + sensor.getType());
        }

        this.stopwatch = new Stopwatch(1000L, this);

        this.stepCountMonitor.init(sensorManager, this);
        this.userProfileMonitor.init(sensorManager);

        Intent intent = new Intent(this, HeartRateMonitor.class);

        this.bindService(intent, this, Context.BIND_AUTO_CREATE);


    }
    private void initializeUI(WatchViewStub stub) {
        this.splitTimeView.initialize(stub);
        this.heartRateView.initialize(stub);
        this.stepCountView.initialize(stub);

        this.notificationView.initialize(this);

        this.startButton = (ImageButton) stub.findViewById(R.id.start);
        this.startButton.setOnClickListener(new StartButtonListener());

        this.stopButton = (ImageButton) stub.findViewById(R.id.stop);
        this.stopButton.setOnClickListener(new StopButtonListener());
        this.stopButton.setVisibility(View.GONE);

        LinearLayout mainLayout = (LinearLayout)stub.findViewById(R.id.main_layout);
        mainLayout.setOnTouchListener(this);
    }
    private void startWorkout() {
        this.startButton.setVisibility(View.GONE);
        this.stopButton.setVisibility(View.VISIBLE);

        this.stopwatch.start();
        this.heartRateMonitor.start();
        this.stepCountMonitor.start();
        this.userProfileMonitor.start();
    }
    private void stopWorkout() {
        this.startButton.setVisibility(View.VISIBLE);
        this.stopButton.setVisibility(View.GONE);

        this.stopwatch.stop();
        this.heartRateMonitor.stop();
        this.stepCountMonitor.stop();
        this.userProfileMonitor.stop();
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

        this.notificationView.updateTime(elapsed);
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
