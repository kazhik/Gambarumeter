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
import android.widget.Toast;

import net.kazhik.gambarumeter.R;
import net.kazhik.gambarumeter.main.monitor.GeolocationMonitorImpl;
import net.kazhik.gambarumeter.main.monitor.LocationMonitor;
import net.kazhik.gambarumeter.main.monitor.LocationSensorValueListener;
import net.kazhik.gambarumeterlib.storage.DataStorage;
import net.kazhik.gambarumeterlib.storage.WorkoutTable;

/**
 * Created by kazhik on 14/11/11.
 */
public class LocationMainFragment extends MainFragment
        implements LocationSensorValueListener {

    private LocationMonitor locationMonitor;
    private boolean isLocationAvailable = false;

    private static final String TAG = "LocationMainFragment";

    @Override
    public void refreshView() {
        Context context = this.getActivity();
        if (context == null) {
            Log.d(TAG, "Context doesn't exist");
            return;
        }
        Log.d(TAG, "refreshView");
    }

    @Override
    protected void terminateSensor() {
        if (this.locationMonitor != null) {
            this.locationMonitor.terminate();
        }

        super.terminateSensor();
    }
    protected Class<?> getServiceClass() {
        return GeolocationMonitorImpl.class;
    }

    @Override
    protected void initializeSensor() {
        
        super.initializeSensor();
        
        Activity activity = this.getActivity();

        if (this.isLocationAvailable(activity)) {
            Intent intent = new Intent(activity, this.getServiceClass());
            boolean bound = activity.bindService(intent, this, Context.BIND_AUTO_CREATE);
            if (bound) {
                this.setBound();
            }
            this.isLocationAvailable = true;
        }

    }
    public boolean isLocationAvailable() {
        return this.isLocationAvailable;
    }
    public float getDistance() {
        float distance = 0;
        if (this.locationMonitor != null) {
            distance = this.locationMonitor.getDistance();
        }
        return distance;
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
    protected void initializeUI(int flags) {
        Log.d(TAG, "initializeUI");

        if (this.isLocationAvailable) {
            flags |= MainViewController.LOCATION_AVAILABLE;
        }

        super.initializeUI(flags);
    }

    @Override
    protected void startWorkout() {
        this.mainViewController.clear();

        if (this.locationMonitor != null) {
            this.locationMonitor.start();
        }
        super.startWorkout();
    }
    @Override
    protected long stopWorkout() {
        long stopTime = super.stopWorkout();

        if (this.locationMonitor != null) {
            this.locationMonitor.stop(stopTime);
        }

        this.mainViewController.dismissNotification();
        
        return stopTime;
    }

    @Override
    public void saveResult(SQLiteDatabase db, long startTime) {
        super.saveResult(db, startTime);

        if (this.locationMonitor != null) {
            this.locationMonitor.saveResult(db, startTime);
        }

    }

    @Override
    protected void saveResult() {
        Context context = this.getActivity();

        DataStorage storage = new DataStorage(context);
        SQLiteDatabase db = storage.open();
        db.beginTransaction();
        try {
            long startTime = this.stopwatch.getStartTime();

            this.saveResult(db, startTime);

            float distance = this.getDistance();

            int stepCount = this.getStepCount();

            WorkoutTable workoutTable = new WorkoutTable(context, db);
            workoutTable.insert(
                    startTime,
                    this.stopwatch.getStopTime(),
                    stepCount,
                    distance,
                    0);

            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        } finally {
            db.endTransaction();
        }

        storage.close();

    }

    // LocationSensorValueListener
    @Override
    public void onLocationChanged(long timestamp, float distance, float speed) {
        if (!this.stopwatch.isRunning()) {
            return;
        }

        this.mainViewController.setDistance(distance);
    }

    // LocationSensorValueListener
    @Override
    public void onLocationAvailable() {
        this.mainViewController.setDistance(0);
        this.mainViewController.refreshView(this.getActivity());
    }

    // LocationSensorValueListener
    @Override
    public void onLap(long timestamp, float distance, long lap) {
        this.mainViewController.setSplitTime(lap);
        this.storeCurrentStepCount(timestamp);
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

        if (iBinder instanceof LocationMonitor.LocationBinder) {
            this.locationMonitor =
                    ((LocationMonitor.LocationBinder)iBinder).getService();
            this.locationMonitor.init(this.getActivity(), this);
        }

        super.onServiceConnected(componentName, iBinder);
    }

    @Override
    protected boolean isServiceReady() {
        return (super.isServiceReady() && this.locationMonitor != null);
    }

}
