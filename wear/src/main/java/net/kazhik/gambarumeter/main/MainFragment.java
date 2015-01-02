package net.kazhik.gambarumeter.main;

import android.app.Activity;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.SQLException;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import net.kazhik.gambarumeter.R;
import net.kazhik.gambarumeter.entity.Lap;
import net.kazhik.gambarumeter.entity.SensorValue;
import net.kazhik.gambarumeter.monitor.GeolocationMonitor;
import net.kazhik.gambarumeter.monitor.HeartRateMonitor;
import net.kazhik.gambarumeter.monitor.SensorValueListener;
import net.kazhik.gambarumeter.monitor.StepCountMonitor;
import net.kazhik.gambarumeter.monitor.Stopwatch;
import net.kazhik.gambarumeter.net.kazhik.gambarumeter.util.Util;
import net.kazhik.gambarumeter.storage.HeartRateTable;
import net.kazhik.gambarumeter.storage.LapTable;
import net.kazhik.gambarumeter.storage.LocationTable;
import net.kazhik.gambarumeter.storage.WorkoutTable;
import net.kazhik.gambarumeter.view.DistanceView;
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
    private GeolocationMonitor locationMonitor;

    private SplitTimeView splitTimeView = new SplitTimeView();
    private HeartRateView heartRateView = new HeartRateView();
    private DistanceView distanceView = new DistanceView();
    private StepCountView stepCountView = new StepCountView();

    private NotificationView notificationView = new NotificationView();

    private SharedPreferences prefs;

    private UserInputManager userInputManager;

    private static final String TAG = "MainFragment";

    @Override
    public void onSaveInstanceState(Bundle outState) {

        outState.putLong("start_time", this.stopwatch.getStartTime());

        super.onSaveInstanceState(outState);
        Log.d(TAG, "onSaveInstanceState: " + outState.getLong("start_time"));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate:" + savedInstanceState);

        this.initializeSensor();

        this.prefs =
                PreferenceManager.getDefaultSharedPreferences(this.getActivity());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        Log.d(TAG, "onDestroyView: ");
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Log.d(TAG, "onActivityCreated: ");

        this.initializeUI();

        this.voiceAction(savedInstanceState);
    }
    private void voiceAction(Bundle savedInstanceState) {
        String actionStatus = this.getActivity().getIntent().getStringExtra("actionStatus");
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
        if (this.heartRateMonitor != null || this.locationMonitor != null) {
            if (this.locationMonitor != null) {
                this.locationMonitor.terminate();
            }
            if (this.heartRateMonitor != null) {
                this.heartRateMonitor.terminate();
            }
            this.getActivity().getApplicationContext().unbindService(this);
        }

        super.onDestroy();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        return inflater.inflate(R.layout.main, container, false);
    }

    private void initializeSensor() {
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
                case Sensor.TYPE_STEP_COUNTER:
                    this.stepCountMonitor = new StepCountMonitor();
                    this.stepCountMonitor.init(this.sensorManager, this);
                    break;
                default:
                    break;
            }
        }

        if (activity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)) {

            if (this.isGpsEnabled(appContext)) {
                Intent intent = new Intent(activity, GeolocationMonitor.class);
                appContext.bindService(intent, this, Context.BIND_AUTO_CREATE);
                this.locationMonitor = new GeolocationMonitor(); // temporary
            } else {
                Toast.makeText(appContext, appContext.getString(R.string.gps_off), Toast.LENGTH_LONG)
                        .show();
            }

        }


        this.stopwatch = new Stopwatch(1000L, this);

    }
    private boolean isGpsEnabled(Context context) {

        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }
    private void initializeUI() {
        Activity activity = this.getActivity();

        this.splitTimeView.initialize((TextView)activity.findViewById(R.id.split_time));
        if (this.heartRateMonitor != null) {
            this.heartRateView.initialize((TextView)activity.findViewById(R.id.bpm));
            activity.findViewById(R.id.heart_rate).setVisibility(View.VISIBLE);
        } else {
            activity.findViewById(R.id.heart_rate).setVisibility(View.GONE);
        }
        if (this.locationMonitor != null) {
            this.distanceView.initialize((TextView)activity.findViewById(R.id.distance_value));
            activity.findViewById(R.id.distance).setVisibility(View.VISIBLE);
        } else {
            activity.findViewById(R.id.distance).setVisibility(View.GONE);
        }
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
        this.notificationView.clear();

        if (this.heartRateMonitor != null) {
            this.heartRateView.setCurrentRate(0)
                    .refresh();
            this.heartRateMonitor.start();
        }
        if (this.locationMonitor != null) {
            this.distanceView.setDistance(0)
                    .refresh();
            String distanceUnit = this.prefs.getString("distanceUnit", "metre");
            this.locationMonitor.start(Util.lapDistance(distanceUnit));
        }
        if (this.stepCountMonitor != null) {
            this.stepCountView.setStepCount(0)
                    .refresh();
            this.stepCountMonitor.start();
        }
        this.splitTimeView.setTime(0)
                .refresh();

        this.stopwatch.start();
    }
    private void stopWorkout() {

        this.stopwatch.stop();
        if (this.heartRateMonitor != null) {
            this.heartRateMonitor.stop();
        }
        if (this.stepCountMonitor != null) {
            this.stepCountMonitor.stop();
        }
        if (this.locationMonitor != null) {
            this.locationMonitor.stop();
        }

        this.notificationView.dismiss();

    }

    private void saveResult() {
        int ret;
        try {
            long startTime = this.stopwatch.getStartTime();

            // LocationTable, LapTable
            float distance = 0;
            if (this.locationMonitor != null) {
                distance = this.locationMonitor.getDistance();

                LocationTable locTable = new LocationTable(this.getActivity());
                locTable.open(false);
                for (Location loc: this.locationMonitor.getLocationList()) {
                    Log.d(TAG, "saveResult:" + loc.getTime());
                    locTable.insert(startTime, loc);
                }
                locTable.close();

                LapTable lapTable = new LapTable(this.getActivity());
                lapTable.open(false);
                for (Lap lap: this.locationMonitor.getLaps()) {
                    lapTable.insert(lap.getTimestamp(), startTime, lap.getDistance());
                }
                lapTable.close();

            }

            // HeartRateTable
            int heartRate = 0;
            if (this.heartRateMonitor != null) {
                heartRate = this.heartRateMonitor.getAverageHeartRate();

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
                    distance,
                    heartRate);
            workoutTable.close();

            Log.d(TAG, "insert: " + ret + "; " + startTime);

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
    public void onHeartRateChanged(long timestamp, int rate) {
        this.heartRateView.setCurrentRate(rate);
        this.getActivity().runOnUiThread(this.heartRateView);

        Log.d(TAG, "new heart rate: " + rate);
        this.notificationView.updateHeartRate(rate);

    }

    // SensorValueListener
    @Override
    public void onStepCountChanged(long timestamp, int stepCount) {
        this.stepCountView.setStepCount(stepCount);
        this.getActivity().runOnUiThread(this.stepCountView);

        this.notificationView.updateStepCount(stepCount);
    }

    // SensorValueListener
    @Override
    public void onLocationChanged(long timestamp, float distance, float speed) {
        String distanceUnit = this.prefs.getString("distanceUnit", "metre");
        distance = Util.convertMeter(distance, distanceUnit);

        this.distanceView.setDistance(distance);
        this.getActivity().runOnUiThread(this.distanceView);

        this.notificationView.updateDistance(distance);
    }

    // SensorValueListener
    @Override
    public void onLocationAvailable() {
        this.distanceView.setEnable(true);
    }

    // SensorValueListener
    @Override
    public void onLap(long timestamp, float distance, long lap) {

    }

    // Stopwatch.OnTickListener
    @Override
    public void onTick(long elapsed) {
        this.splitTimeView.setTime(elapsed);
        this.getActivity().runOnUiThread(this.splitTimeView);

        this.notificationView.show(elapsed);
    }

    // ServiceConnection
    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        Log.d(TAG, "onServiceConnected: " + componentName.toString());

        if (iBinder instanceof HeartRateMonitor.HeartRateBinder) {
            this.heartRateMonitor = ((HeartRateMonitor.HeartRateBinder)iBinder).getService();
            this.heartRateMonitor.init(sensorManager, this);
        } else if (iBinder instanceof GeolocationMonitor.GeolocationBinder) {
            this.locationMonitor = ((GeolocationMonitor.GeolocationBinder)iBinder).getService();
            this.locationMonitor.init(this.getActivity(), this);
        }

    }

    // ServiceConnection
    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        Log.d(TAG, "onServiceDisconnected: " + componentName.toString());

    }


}
