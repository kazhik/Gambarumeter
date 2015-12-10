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

import com.google.android.gms.wearable.DataMap;

import net.kazhik.gambarumeter.R;
import net.kazhik.gambarumeter.Util;
import net.kazhik.gambarumeter.main.monitor.GeolocationMonitor;
import net.kazhik.gambarumeter.main.monitor.LocationSensorValueListener;
import net.kazhik.gambarumeter.main.view.DistanceView;
import net.kazhik.gambarumeter.main.view.LocationNotificationView;
import net.kazhik.gambarumeterlib.storage.DataStorage;
import net.kazhik.gambarumeterlib.storage.WorkoutTable;

/**
 * Created by kazhik on 14/11/11.
 */
public class LocationMainFragment extends MainFragment
        implements LocationSensorValueListener {

    private GeolocationMonitor locationMonitor;

    private DistanceView distanceView = new DistanceView();

    private LocationNotificationView notificationView = new LocationNotificationView();

    private static final String TAG = "LocationMainFragment";

    @Override
    public void refreshView() {
        if (this.getActivity() == null) {
            Log.d(TAG, "Activity doesn't exist");
            return;
        }
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
                boolean bound = appContext.bindService(intent, this, Context.BIND_AUTO_CREATE);
                if (bound) {
                    this.setBound();
                }
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

        this.notificationView.initialize(activity);

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
        activity.findViewById(R.id.separator).setVisibility(View.GONE);
        activity.findViewById(R.id.heart_rate).setVisibility(View.GONE);


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
    @Override
    protected DataMap putData(DataMap dataMap) {
        dataMap = super.putData(dataMap);

        dataMap = this.locationMonitor.putData(dataMap);

        DataMap workoutDataMap = new DataMap();

        workoutDataMap.putLong(DataStorage.COL_START_TIME,
                this.stopwatch.getStartTime());
        workoutDataMap.putLong(DataStorage.COL_STOP_TIME,
                this.stopwatch.getStopTime());
        workoutDataMap.putInt(DataStorage.COL_STEP_COUNT,
                this.stepCountMonitor.getStepCount());
        workoutDataMap.putInt(DataStorage.COL_HEART_RATE, 0);
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
        super.onServiceConnected(componentName, iBinder);

        if (iBinder instanceof GeolocationMonitor.GeolocationBinder) {
            this.locationMonitor =
                    ((GeolocationMonitor.GeolocationBinder)iBinder).getService();
            this.locationMonitor.init(this.getActivity(), this);
        }

    }


}
