package net.kazhik.gambarumeter.main;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import net.kazhik.gambarumeter.R;
import net.kazhik.gambarumeter.main.monitor.HeartRateMonitor;
import net.kazhik.gambarumeter.main.monitor.HeartRateSensorValueListener;
import net.kazhik.gambarumeterlib.storage.DataStorage;
import net.kazhik.gambarumeterlib.storage.WorkoutTable;

import java.util.Date;

/**
 * Created by kazhik on 14/11/11.
 */
public class HeartRateMainFragment extends MainFragment
        implements HeartRateSensorValueListener {
    private HeartRateMonitor heartRateMonitor;
    private boolean isHeartRateAvailable = false;

    private static final String TAG = "HeartRateMainFragment";


    @Override
    public void refreshView() {
        Log.d(TAG, "refreshView");
    }


    @Override
    protected void terminateSensor() {
        if (this.heartRateMonitor != null) {
            this.heartRateMonitor.terminate();
        }

        super.terminateSensor();
    }

    @Override
    protected void initializeSensor() {
        super.initializeSensor();
        
        Activity activity = this.getActivity();

        if (isHeartRateAvailable(activity)) {
            Intent intent = new Intent(activity, HeartRateMonitor.class);
            boolean bound = activity.bindService(intent, this, Context.BIND_AUTO_CREATE);
            if (bound) {
                this.setBound();
            }
            this.isHeartRateAvailable = true;
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
    @Override
    protected void initializeUI(int flags) {

        if (this.isHeartRateAvailable) {
            flags |= MainViewController.HEARTRATE_AVAILABLE;
        }

        super.initializeUI(flags);
    }

    @Override
    protected void startWorkout() {
        this.mainViewController.clear();

        if (this.heartRateMonitor != null) {
            this.heartRateMonitor.start();
        }
        super.startWorkout();
    }
    @Override
    protected long stopWorkout() {
        long stopTime = super.stopWorkout();

        if (this.heartRateMonitor != null) {
            this.heartRateMonitor.stop(stopTime);
        }
        this.mainViewController.dismissNotification();

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

            int heartRate = 0;
            if (this.heartRateMonitor != null) {
                heartRate = this.heartRateMonitor.getAverageHeartRate();
                this.heartRateMonitor.saveResult(db, startTime);
            }

            // WorkoutTable
            int stepCount = this.getStepCount();

            WorkoutTable workoutTable = new WorkoutTable(context, db);
            workoutTable.insert(
                    startTime,
                    this.stopwatch.getStopTime(),
                    stepCount,
                    0,
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

        Log.d(TAG, "new heart rate: " + (new Date(timestamp)).toString() + " / " + rate);
        this.mainViewController.setHeartRate(rate);

    }

    // ServiceConnection
    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        if (iBinder instanceof HeartRateMonitor.HeartRateBinder) {
            this.heartRateMonitor =
                    ((HeartRateMonitor.HeartRateBinder)iBinder).getService();
            this.heartRateMonitor.init(this.getActivity(), this);
        }
        super.onServiceConnected(componentName, iBinder);

    }

    @Override
    protected boolean isServiceReady() {
        return (super.isServiceReady() && this.heartRateMonitor != null);
    }

}
