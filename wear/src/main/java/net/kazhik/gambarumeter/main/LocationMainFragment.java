package net.kazhik.gambarumeter.main;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.SQLException;
import android.location.LocationManager;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import net.kazhik.gambarumeter.R;
import net.kazhik.gambarumeter.Util;
import net.kazhik.gambarumeter.main.monitor.GeolocationMonitor;
import net.kazhik.gambarumeter.main.monitor.LocationSensorValueListener;
import net.kazhik.gambarumeter.main.view.DistanceView;
import net.kazhik.gambarumeter.main.view.LocationNotificationView;
import net.kazhik.gambarumeter.storage.WorkoutTable;

/**
 * Created by kazhik on 14/11/11.
 */
public class LocationMainFragment extends MainFragment
        implements LocationSensorValueListener {

    private GeolocationMonitor locationMonitor;

    private DistanceView distanceView = new DistanceView();

    private LocationNotificationView notificationView = new LocationNotificationView();

    private static final String TAG = "MainFragment";

    @Override
    public void refreshView() {
        Log.d(TAG, "refreshView");
        if (this.locationMonitor != null) {
            this.setDistanceUnit();
            this.getActivity().runOnUiThread(this.distanceView);
        }
    }

    private void setDistanceUnit() {
        String distanceUnit = this.prefs.getString("distanceUnit", "metre");
        String distanceUnitStr =
                Util.distanceUnitDisplayStr(distanceUnit,
                        this.getActivity().getResources());

        this.distanceView
                .setDistanceUnit(distanceUnit)
                .setDistanceUnitStr(distanceUnitStr);

        this.notificationView.setDistanceUnit(distanceUnit);
        Log.d(TAG, "setDistanceUnit: " + distanceUnit);
    }

    @Override
    public void onDestroy() {
        if (this.locationMonitor != null) {
            this.locationMonitor.terminate();
            this.getActivity().getApplicationContext().unbindService(this);
        }

        super.onDestroy();
    }

    protected void initializeSensor() {
        
        super.initializeSensor();
        
        Activity activity = this.getActivity();
        Context appContext = activity.getApplicationContext();

        if (activity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)) {

            if (this.isGpsEnabled(appContext)) {
                Intent intent = new Intent(activity, GeolocationMonitor.class);
                appContext.bindService(intent, this, Context.BIND_AUTO_CREATE);
                this.locationMonitor = new GeolocationMonitor(); // temporary
            } else {
                Toast.makeText(appContext,
                        R.string.gps_off,
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

        if (this.locationMonitor != null) {
            TextView distanceValue =
                    (TextView)activity.findViewById(R.id.distance_value);
            TextView distanceUnitLabel
                    = (TextView)activity.findViewById(R.id.distance_label);

            this.distanceView.initialize(distanceValue, distanceUnitLabel);
            this.setDistanceUnit();
            this.distanceView.refresh();
            activity.findViewById(R.id.distance).setVisibility(View.VISIBLE);
        } else {
            activity.findViewById(R.id.distance).setVisibility(View.GONE);
        }

        this.notificationView.initialize(activity);

    }

    protected void startWorkout() {
        this.notificationView.clear();

        if (this.locationMonitor != null) {
            String distanceUnit = this.prefs.getString("distanceUnit", "metre");
            this.distanceView.setDistance(0)
                    .setDistanceUnit(distanceUnit)
                    .refresh();
            this.locationMonitor.start(Util.lapDistance(distanceUnit));
        }
        super.startWorkout();
    }
    protected void stopWorkout() {

        if (this.locationMonitor != null) {
            this.locationMonitor.stop();
        }

        this.notificationView.dismiss();

        super.stopWorkout();
    }

    protected void saveResult() {
        super.saveResult();
        int ret;
        try {
            long startTime = this.stopwatch.getStartTime();

            // LocationTable, LapTable
            float distance = 0;
            if (this.locationMonitor != null) {
                distance = this.locationMonitor.getDistance();
                this.locationMonitor.saveResult(startTime);
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
                    0);
            workoutTable.close();

            Log.d(TAG, "insert: " + ret + "; " + startTime);

        } catch (SQLException e) {
            Log.e(TAG, e.getMessage(), e);
        }

    }

    @Override
    protected void updateStepCount(int stepCount) {
        this.notificationView.updateStepCount(stepCount);

    }
    @Override
    protected void showNotification(long elapsed) {
        this.notificationView.show(elapsed);

    }


    // SensorValueListener
    @Override
    public void onLocationChanged(long timestamp, float distance, float speed) {
        if (!this.stopwatch.isRunning()) {
            return;
        }
        this.distanceView.setDistance(distance);
        this.getActivity().runOnUiThread(this.distanceView);

        this.notificationView.updateDistance(distance);
    }

    // SensorValueListener
    @Override
    public void onLocationAvailable() {
        this.distanceView.setAvailable(true);
    }

    // SensorValueListener
    @Override
    public void onLap(long timestamp, float distance, long lap) {
        this.notificationView.updateLap(lap);
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
        Log.d(TAG, "onServiceConnected: " + componentName.toString());

        if (iBinder instanceof GeolocationMonitor.GeolocationBinder) {
            this.locationMonitor =
                    ((GeolocationMonitor.GeolocationBinder)iBinder).getService();
            this.locationMonitor.init(this.getActivity(), this);
        }

    }


}
