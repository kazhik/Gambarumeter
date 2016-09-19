package net.kazhik.gambarumeter.main;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.wearable.DataMap;

import net.kazhik.gambarumeter.R;
import net.kazhik.gambarumeter.main.monitor.HeartRateMonitor;
import net.kazhik.gambarumeter.main.monitor.HeartRateSensorValueListener;
import net.kazhik.gambarumeter.main.monitor.LocationMonitor;
import net.kazhik.gambarumeter.main.monitor.LocationSensorValueListener;
import net.kazhik.gambarumeter.main.notification.FullNotificationView;
import net.kazhik.gambarumeter.main.view.DistanceView;
import net.kazhik.gambarumeter.main.view.HeartRateView;
import net.kazhik.gambarumeterlib.storage.DataStorage;
import net.kazhik.gambarumeterlib.storage.WorkoutTable;

/**
 * Created by kazhik on 16/01/21.
 */
public class FullMainFragment extends MainFragment
        implements HeartRateSensorValueListener, LocationSensorValueListener {

    private SensorManager sensorManager;
    private HeartRateView heartRateView = new HeartRateView();
    private HeartRateMonitor heartRateMonitor;

    private DistanceView distanceView = new DistanceView();
    private LocationMonitor locationMonitor;

    private FullNotificationView notificationView = new FullNotificationView();

    private int connectedService = 0;

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
    public void onDestroy() {
        if (this.heartRateMonitor != null) {
            this.heartRateMonitor.terminate();
        }
        if (this.locationMonitor != null) {
            this.locationMonitor.terminate();
        }

        super.onDestroy();

    }
    protected void initializeSensor() {
        super.initializeSensor();

        Activity activity = this.getActivity();

        this.sensorManager =
                (SensorManager)activity.getSystemService(Activity.SENSOR_SERVICE);

        Sensor sensor = this.sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
        if (sensor != null) {
            Intent intent = new Intent(activity, HeartRateMonitor.class);
            activity.startService(intent);
            boolean bound = activity.bindService(intent, this, Context.BIND_AUTO_CREATE);
            if (bound) {
                this.setBound();
            }
            this.heartRateMonitor = new HeartRateMonitor(); // temporary
        }

        if (!this.isGpsEnabled(activity)) {
            Toast.makeText(activity,
                    R.string.gps_off,
                    Toast.LENGTH_LONG)
                    .show();
            return;
        }
        if (!this.isLocationEnabled(activity)) {
            Toast.makeText(activity,
                    R.string.location_disabled,
                    Toast.LENGTH_LONG)
                    .show();
            return;

        }
        Intent intent = new Intent(activity, LocationMonitor.class);
        boolean bound = activity.bindService(intent, this, Context.BIND_AUTO_CREATE);
        if (bound) {
            this.setBound();
        }
        this.locationMonitor = new LocationMonitor(); // temporary
    }

    private boolean isLocationEnabled(Activity activity) {
        int checkResult = ContextCompat.checkSelfPermission( activity,
                android.Manifest.permission.ACCESS_FINE_LOCATION );
        return ( checkResult == PackageManager.PERMISSION_GRANTED );
    }
    private boolean isGpsEnabled(Context context) {

        LocationManager lm =
                (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }
    protected void initializeUI() {
        super.initializeUI();

        Activity activity = this.getActivity();

        this.notificationView.initialize(activity);

        if (this.heartRateMonitor != null) {
            this.heartRateView.initialize((TextView) activity.findViewById(R.id.bpm));
            activity.findViewById(R.id.heart_rate).setVisibility(View.VISIBLE);
        }
        if (this.locationMonitor != null) {
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
    protected long stopWorkout() {
        long stopTime = super.stopWorkout();

        if (this.heartRateMonitor != null) {
            this.heartRateMonitor.stop(stopTime);
        }

        if (this.locationMonitor != null) {
            this.locationMonitor.stop(stopTime);
        }

        this.notificationView.dismiss();
        return stopTime;
    }
    @Override
    protected DataMap putData(DataMap dataMap) {
        dataMap = super.putData(dataMap);

        dataMap = this.heartRateMonitor.putData(dataMap);
        dataMap = this.locationMonitor.putData(dataMap);

        DataMap workoutDataMap = new DataMap();

        workoutDataMap.putLong(DataStorage.COL_START_TIME,
                this.stopwatch.getStartTime());
        workoutDataMap.putLong(DataStorage.COL_STOP_TIME,
                this.stopwatch.getStopTime());
        workoutDataMap.putInt(DataStorage.COL_STEP_COUNT,
                this.stepCountMonitor.getStepCount());
        workoutDataMap.putInt(DataStorage.COL_HEART_RATE,
                this.heartRateMonitor.getAverageHeartRate());
        workoutDataMap.putFloat(DataStorage.COL_DISTANCE,
                this.locationMonitor.getDistance());

        dataMap.putDataMap(DataStorage.TBL_WORKOUT, workoutDataMap);

        return dataMap;

    }
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

        this.notificationView.updateHeartRate(rate);

    }
    @Override
    protected void updateStepCount(int stepCount) {
        this.notificationView.updateStepCount(stepCount);

    }
    @Override
    protected void showNotification(long elapsed) {
        this.notificationView.show(elapsed);

    }

    // LocationSensorValueListener
    @Override
    public void onLocationChanged(long timestamp, float distance, float speed) {
        if (!this.stopwatch.isRunning()) {
            return;
        }
        this.distanceView.setDistance(distance);
        this.getActivity().runOnUiThread(this.distanceView);

        this.notificationView.updateDistance(distance);
    }

    // LocationSensorValueListener
    @Override
    public void onLocationAvailable() {
        this.distanceView.setAvailable(true);
    }

    // LocationSensorValueListener
    @Override
    public void onLap(long timestamp, float distance, long lap) {
        this.notificationView.updateLap(lap);
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
            this.heartRateMonitor.init(this.getActivity(), sensorManager, this);
            this.connectedService++;
        } else if (iBinder instanceof LocationMonitor.GeolocationBinder) {
            this.locationMonitor =
                    ((LocationMonitor.GeolocationBinder)iBinder).getService();
            this.locationMonitor.init(this.getActivity(), this);
            this.connectedService++;
        }
        super.onServiceConnected(componentName, iBinder);

    }

    protected boolean isServiceReady() {
        return (super.isServiceReady() && this.connectedService == 2);
    }

}
