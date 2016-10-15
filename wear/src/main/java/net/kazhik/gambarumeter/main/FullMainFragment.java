package net.kazhik.gambarumeter.main;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.location.LocationManager;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import net.kazhik.gambarumeter.R;
import net.kazhik.gambarumeter.main.monitor.HeartRateMonitor;
import net.kazhik.gambarumeter.main.monitor.HeartRateSensorValueListener;
import net.kazhik.gambarumeter.main.monitor.LocationMonitor;
import net.kazhik.gambarumeter.main.monitor.LocationMonitorImpl;
import net.kazhik.gambarumeter.main.monitor.LocationSensorValueListener;
import net.kazhik.gambarumeter.main.view.DistanceView;
import net.kazhik.gambarumeter.main.view.HeartRateView;
import net.kazhik.gambarumeterlib.storage.DataStorage;
import net.kazhik.gambarumeterlib.storage.WorkoutTable;

/**
 * Created by kazhik on 16/01/21.
 */
public class FullMainFragment extends MainFragment
        implements HeartRateSensorValueListener, LocationSensorValueListener {

    private HeartRateView heartRateView;
    private HeartRateMonitor heartRateMonitor;
    private boolean isHeartRateAvailable = false;

    private DistanceView distanceView;
    private LocationMonitor locationMonitor;
    private boolean isLocationAvailable = false;

    private static final String TAG = "FullMainFragment";

    @Override
    public void refreshView() {
        Context context = this.getActivity();
        if (context == null) {
            Log.d(TAG, "Context doesn't exist");
            return;
        }
        Log.d(TAG, "refreshView");
        if (this.locationMonitor != null) {
            this.getActivity().runOnUiThread(this.distanceView);
        }
    }
    @Override
    protected void terminateSensor() {
        if (this.heartRateMonitor != null) {
            this.heartRateMonitor.terminate();
        }
        if (this.locationMonitor != null) {
            this.locationMonitor.terminate();
        }

        super.terminateSensor();
    }
    @Override
    protected void initializeSensor() {
        super.initializeSensor();

        Activity activity = this.getActivity();

        if (this.isHeartRateAvailable(activity)) {
            Intent intent = new Intent(activity, HeartRateMonitor.class);
            activity.startService(intent);
            boolean bound = activity.bindService(intent, this, Context.BIND_AUTO_CREATE);
            if (bound) {
                this.setBound();
            }
            this.isHeartRateAvailable = true;
        }

        if (this.isLocationAvailable(activity)) {
            Intent intent = new Intent(activity, LocationMonitorImpl.class);
            boolean bound = activity.bindService(intent, this, Context.BIND_AUTO_CREATE);
            if (bound) {
                this.setBound();
            }
            this.isLocationAvailable = true;
        }
    }
    private boolean isHeartRateAvailable(Activity activity) {
        // Hardware doesn't have Heart rate sensor
        PackageManager pm = activity.getPackageManager();
        if (!pm.hasSystemFeature(PackageManager.FEATURE_SENSOR_HEART_RATE)) {
            return false;
        }
        // Settings/Permissions/Sensors is disabled
        int checkResult = ContextCompat.checkSelfPermission( activity,
                Manifest.permission.BODY_SENSORS );
        if ( checkResult != PackageManager.PERMISSION_GRANTED ) {
            Toast.makeText(activity,
                    R.string.sensors_disabled,
                    Toast.LENGTH_LONG)
                    .show();
            return false;
        }
        return true;
    }
    private boolean isLocationAvailable(Activity activity) {
        // Hardware doesn't have GPS sensor
        PackageManager pm = activity.getPackageManager();
        if (!pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)) {
            return false;
        }

        // Settings/Location is OFF
        LocationManager lm =
                (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(activity,
                    R.string.location_off,
                    Toast.LENGTH_LONG)
                    .show();
            return false;
        }
        // Settings/Permissions/Location is disabled
        int checkResult = ContextCompat.checkSelfPermission( activity,
                Manifest.permission.ACCESS_FINE_LOCATION );
        if ( checkResult != PackageManager.PERMISSION_GRANTED ) {
            Toast.makeText(activity,
                    R.string.location_disabled,
                    Toast.LENGTH_LONG)
                    .show();
            return false;
        }
        return true;

    }

    @Override
    protected void initializeUI() {
        super.initializeUI();

        Activity activity = this.getActivity();

        this.notificationController.initialize(activity);

        if (this.isHeartRateAvailable) {
            this.heartRateView = new HeartRateView();
            this.heartRateView.initialize((TextView) activity.findViewById(R.id.bpm));
            activity.findViewById(R.id.heart_rate).setVisibility(View.VISIBLE);
        }
        if (this.isLocationAvailable) {
            this.distanceView = new DistanceView();
            TextView distanceValue =
                    (TextView)activity.findViewById(R.id.distance_value);
            TextView distanceUnitLabel
                    = (TextView)activity.findViewById(R.id.distance_label);

            this.distanceView.initialize(activity, distanceValue, distanceUnitLabel);
            this.distanceView.refresh();
            activity.findViewById(R.id.distance).setVisibility(View.VISIBLE);
            activity.findViewById(R.id.separator).setVisibility(View.VISIBLE);
        } else {
            activity.findViewById(R.id.distance).setVisibility(View.GONE);
            activity.findViewById(R.id.separator).setVisibility(View.GONE);
        }


    }
    @Override
    protected void startWorkout() {
        if (this.heartRateMonitor != null) {
            this.heartRateView.setCurrentRate(0)
                    .refresh();
            this.heartRateMonitor.start();
        }

        if (this.locationMonitor != null) {
            this.distanceView.setDistance(0)
                    .refresh();
            this.locationMonitor.start();
        }
        super.startWorkout();
    }
    @Override
    protected long stopWorkout() {
        long stopTime = super.stopWorkout();

        if (this.heartRateMonitor != null) {
            this.heartRateMonitor.stop(stopTime);
        }

        if (this.locationMonitor != null) {
            this.locationMonitor.stop(stopTime);
        }

        this.notificationController.dismiss();
        return stopTime;
    }
    @Override
    protected void saveResult() {
        Context context = this.getActivity();

        DataStorage storage = new DataStorage(context);
        SQLiteDatabase db = storage.open();
        db.beginTransaction();
        try {
            long startTime = this.stopwatch.getStartTime();

            super.saveResult(db, startTime);

            // HeartRateTable
            int heartRate = 0;
            if (this.heartRateMonitor != null) {
                heartRate = this.heartRateMonitor.getAverageHeartRate();
                this.heartRateMonitor.saveResult(db, startTime);
            }
            // LocationTable, SplitTable
            float distance = 0;
            if (this.locationMonitor != null) {
                distance = this.locationMonitor.getDistance();
                this.locationMonitor.saveResult(db, startTime);
            }

            WorkoutTable workoutTable = new WorkoutTable(context, db);
            // WorkoutTable
            int stepCount = 0;
            if (this.stepCountMonitor != null) {
                stepCount = this.stepCountMonitor.getStepCount();
            }

            workoutTable.insert(
                    startTime,
                    this.stopwatch.getStopTime(),
                    stepCount,
                    distance,
                    heartRate);

            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        } finally {
            db.endTransaction();
        }

        storage.close();

    }

    // SensorValueListener
    @Override
    public void onHeartRateChanged(long timestamp, int rate) {
        if (!this.stopwatch.isRunning()) {
            return;
        }
        this.heartRateView.setCurrentRate(rate);
        this.getActivity().runOnUiThread(this.heartRateView);

        this.notificationController.updateHeartRate(rate);

    }

    // LocationSensorValueListener
    @Override
    public void onLocationChanged(long timestamp, float distance, float speed) {
        if (!this.stopwatch.isRunning()) {
            return;
        }
        this.distanceView.setDistance(distance);
        this.getActivity().runOnUiThread(this.distanceView);

        this.notificationController.updateDistance(distance);
    }

    // LocationSensorValueListener
    @Override
    public void onLocationAvailable() {
        this.distanceView.setAvailable(true);
    }

    // LocationSensorValueListener
    @Override
    public void onLap(long timestamp, float distance, long lap) {
        this.notificationController.updateLap(lap);
        this.stepCountMonitor.storeCurrentValue(timestamp);
    }
    // SensorValueListener
    @Override
    public void onBatteryLow() {
        this.locationMonitor.terminate();
    }

    // SensorValueListener
    @Override
    public void onBatteryOkay() {
        this.locationMonitor.init(this.getActivity(), this);
    }


    // ServiceConnection
    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        if (iBinder instanceof HeartRateMonitor.HeartRateBinder) {
            this.heartRateMonitor =
                    ((HeartRateMonitor.HeartRateBinder)iBinder).getService();
            this.heartRateMonitor.init(this.getActivity(), this);
        } else if (iBinder instanceof LocationMonitor.LocationBinder) {
            this.locationMonitor =
                    ((LocationMonitor.LocationBinder)iBinder).getService();
            this.locationMonitor.init(this.getActivity(), this);
        }
        super.onServiceConnected(componentName, iBinder);

    }

    @Override
    protected boolean isServiceReady() {
        return super.isServiceReady() &&
                (this.heartRateMonitor != null && this.locationMonitor != null);
    }

}
