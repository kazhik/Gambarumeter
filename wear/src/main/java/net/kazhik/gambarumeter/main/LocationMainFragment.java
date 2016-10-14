package net.kazhik.gambarumeter.main;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.location.LocationManager;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import net.kazhik.gambarumeter.R;
import net.kazhik.gambarumeter.main.monitor.GeolocationMonitor;
import net.kazhik.gambarumeter.main.monitor.LocationSensorValueListener;
import net.kazhik.gambarumeter.main.view.DistanceView;
import net.kazhik.gambarumeterlib.storage.DataStorage;
import net.kazhik.gambarumeterlib.storage.WorkoutTable;

/**
 * Created by kazhik on 14/11/11.
 */
public class LocationMainFragment extends MainFragment
        implements LocationSensorValueListener {

    private GeolocationMonitor locationMonitor;

    private DistanceView distanceView = new DistanceView();

    private int connectedService = 0;

    private static final String TAG = "LocationMainFragment";

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
        if (this.locationMonitor != null) {
            this.locationMonitor.terminate();
        }

        super.terminateSensor();
    }

    @Override
    protected void initializeSensor() {
        
        super.initializeSensor();
        
        Activity activity = this.getActivity();

        if (activity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)) {

            if (this.isGpsEnabled(activity)) {
                Intent intent = new Intent(activity, GeolocationMonitor.class);
                boolean bound = activity.bindService(intent, this, Context.BIND_AUTO_CREATE);
                if (bound) {
                    this.setBound();
                }
                this.locationMonitor = new GeolocationMonitor(); // temporary
            } else {
                Toast.makeText(activity,
                        R.string.location_off,
                        Toast.LENGTH_LONG)
                        .show();
            }

        }

    }
    private boolean isGpsEnabled(Context context) {

        LocationManager lm =
                (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }
    protected void initializeUI() {
        super.initializeUI();
        Activity activity = this.getActivity();

        this.notificationController.initialize(activity);

        if (this.locationMonitor != null) {
            TextView distanceValue =
                    (TextView)activity.findViewById(R.id.distance_value);
            TextView distanceUnitLabel
                    = (TextView)activity.findViewById(R.id.distance_label);

            this.distanceView.initialize(activity, distanceValue, distanceUnitLabel);
            this.distanceView.refresh();
            activity.findViewById(R.id.distance).setVisibility(View.VISIBLE);
        } else {
            activity.findViewById(R.id.distance).setVisibility(View.GONE);
        }
        activity.findViewById(R.id.separator).setVisibility(View.GONE);
        activity.findViewById(R.id.heart_rate).setVisibility(View.GONE);


    }

    protected void startWorkout() {
        this.notificationController.clear();

        if (this.locationMonitor != null) {
            this.distanceView.setDistance(0)
                    .refresh();
            this.locationMonitor.start();
        }
        super.startWorkout();
    }
    protected long stopWorkout() {
        long stopTime = super.stopWorkout();

        if (this.locationMonitor != null) {
            this.locationMonitor.stop(stopTime);
        }

        this.notificationController.dismiss();
        
        return stopTime;
    }

    protected void saveResult() {
        Context context = this.getActivity();

        DataStorage storage = new DataStorage(context);
        SQLiteDatabase db = storage.open();
        db.beginTransaction();
        try {
            long startTime = this.stopwatch.getStartTime();

            super.saveResult(db, startTime);

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

        if (iBinder instanceof GeolocationMonitor.GeolocationBinder) {
            this.locationMonitor =
                    ((GeolocationMonitor.GeolocationBinder)iBinder).getService();
            this.locationMonitor.init(this.getActivity(), this);
            this.connectedService++;
        }

        super.onServiceConnected(componentName, iBinder);
    }

    protected boolean isServiceReady() {
        return (super.isServiceReady() && this.connectedService == 1);
    }

}
