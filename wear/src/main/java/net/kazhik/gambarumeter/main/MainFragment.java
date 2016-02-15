package net.kazhik.gambarumeter.main;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import net.kazhik.gambarumeter.R;
import net.kazhik.gambarumeter.main.monitor.BatteryLevelReceiver;
import net.kazhik.gambarumeter.main.monitor.Gyroscope;
import net.kazhik.gambarumeter.main.monitor.SensorValueListener;
import net.kazhik.gambarumeter.main.monitor.StepCountMonitor;
import net.kazhik.gambarumeter.main.monitor.Stopwatch;
import net.kazhik.gambarumeter.main.view.SplitTimeView;
import net.kazhik.gambarumeter.main.view.StepCountView;
import net.kazhik.gambarumeter.pager.PagerFragment;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by kazhik on 14/11/11.
 */
public abstract class MainFragment extends PagerFragment
        implements Stopwatch.OnTickListener,
        SensorValueListener,
        ServiceConnection,
        UserInputManager.UserInputListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        DataApi.DataListener {
    private SensorManager sensorManager;

    protected Stopwatch stopwatch;
    protected StepCountMonitor stepCountMonitor;
    private BatteryLevelReceiver batteryLevelReceiver;
    private Gyroscope gyroscope;
    private boolean isBound = false;

    private SplitTimeView splitTimeView = new SplitTimeView();
    private StepCountView stepCountView = new StepCountView();

    protected SharedPreferences prefs;

    private UserInputManager userInputManager;
    private Vibrator vibrator;

    private Handler handler;
    private HandlerThread saveDataThread = new HandlerThread("SaveDataThread");
    private Runnable saveDataRunnable = new Runnable() {
        @Override
        public void run() {
            storeCurrentValue(System.currentTimeMillis());
            if (stopwatch.isRunning()) {
                handler.postDelayed(saveDataRunnable, TimeUnit.SECONDS.toMillis(60));
            }
        }
    };

    private GoogleApiClient googleApiClient;

    private static final String TAG = "MainFragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate:" + savedInstanceState);

        this.initializeSensor();

        Activity activity = this.getActivity();

        this.prefs =
                PreferenceManager.getDefaultSharedPreferences(activity);


        this.googleApiClient = new GoogleApiClient.Builder(activity)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Wearable.API)
                .build();

        this.vibrator = (Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE);

        this.saveDataThread.start();
        this.handler = new Handler(this.saveDataThread.getLooper());

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
    public boolean isBound() {
        return this.isBound;
    }
    public void setBound() {
        this.isBound = true;
    }

    @Override
    public void onDestroy() {
        this.stopWorkout();
        if (this.googleApiClient != null) {
            this.googleApiClient.unregisterConnectionCallbacks(this);
        }
        if (this.gyroscope != null) {
            this.gyroscope.terminate();
        }
        Activity activity = this.getActivity();
        if (this.isBound) {
            activity.getApplicationContext().unbindService(this);

        }
        activity.unregisterReceiver(this.batteryLevelReceiver);

        super.onDestroy();
    }

    protected void initializeSensor() {
        Activity activity = this.getActivity();

        this.batteryLevelReceiver = new BatteryLevelReceiver(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.BATTERY_LOW");
        intentFilter.addAction("android.intent.action.BATTERY_OKAY");
        activity.registerReceiver(this.batteryLevelReceiver, intentFilter);

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
                case Sensor.TYPE_GYROSCOPE:
                    Intent intent = new Intent(activity, Gyroscope.class);
                    boolean bound = this.getActivity().getApplicationContext().bindService(intent,
                            this, Context.BIND_AUTO_CREATE);
                    this.gyroscope = new Gyroscope(); // temporary
                    if (bound) {
                        this.isBound = true;
                    }

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
        this.stepCountView.initialize((TextView) activity.findViewById(R.id.stepcount_value));

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

        this.handler.postDelayed(this.saveDataRunnable,
                TimeUnit.SECONDS.toMillis(60));

    }
    protected void stopWorkout() {
        this.stopwatch.stop();
        if (this.stepCountMonitor != null) {
            this.stepCountMonitor.stop();
        }

    }
    protected abstract void saveResult();

    public void saveResult(SQLiteDatabase db, long startTime) {
        if (this.stepCountMonitor != null) {
            this.stepCountMonitor.saveResult(db, startTime);
        }
    }
    private void sendDataToMobile() {
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create("/database");

        // put data on datamap
        DataMap dataMap = putData(putDataMapReq.getDataMap());
        Log.d(TAG, "newData: " + dataMap.toString());

        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        PendingResult<DataApi.DataItemResult> pendingResult =
                Wearable.DataApi.putDataItem(this.googleApiClient, putDataReq);

        pendingResult.setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
            @Override
            public void onResult(DataApi.DataItemResult dataItemResult) {
                Log.d(TAG, "putDataItem done: ");
            }
        });

    }
    protected DataMap putData(DataMap dataMap) {
        this.stepCountMonitor.putDataMap(dataMap);

        return dataMap;

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
        this.sendDataToMobile();
        this.stopwatch.reset();
    }
    // SensorValueListener
    @Override
    public void onRotation(long timestamp) {
        if (!this.stopwatch.isRunning()) {
            return;
        }
        this.userInputManager.toggleVisibility(false);
        this.stopWorkout();
        this.saveResult();
        this.sendDataToMobile();
        this.stopwatch.reset();
        this.vibrator.vibrate(1000);
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
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        Log.d(TAG, "onServiceConnected: " + componentName.toString());

        if (iBinder instanceof Gyroscope.GyroBinder) {

            this.gyroscope =
                    ((Gyroscope.GyroBinder) iBinder).getService();

            this.gyroscope.initialize(this.sensorManager, this);
        }

    }
    // ServiceConnection
    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        Log.d(TAG, "onServiceDisconnected: " + componentName.toString());

    }
    // GoogleApiClient.ConnectionCallbacks
    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected");
        Wearable.DataApi.addListener(this.googleApiClient, this);
    }

    // GoogleApiClient.ConnectionCallbacks
    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended");

    }

    // GoogleApiClient.OnConnectionFailedListener
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed");

    }

    // DataApi.DataListener
    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Log.d(TAG, "onDataChanged");
        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                // DataItem changed
                DataItem item = event.getDataItem();
                if (item.getUri().getPath().compareTo("/config") == 0) {
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
//                    updateConfig(dataMap);
                }
            }
        }
    }
    @Override
    public void onStart() {
        super.onStart();
        if (!this.googleApiClient.isConnected()) {
            this.googleApiClient.connect();
        }
    }

    @Override
    public void onStop() {
        if (this.googleApiClient != null && this.googleApiClient.isConnected()) {
            this.googleApiClient.disconnect();
        }
        super.onStop();
    }

    protected void storeCurrentValue(long timestamp) {
        this.stepCountMonitor.storeCurrentValue(timestamp);

    }

}
