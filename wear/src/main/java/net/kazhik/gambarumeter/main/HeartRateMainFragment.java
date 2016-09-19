package net.kazhik.gambarumeter.main;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.wearable.DataMap;

import net.kazhik.gambarumeter.R;
import net.kazhik.gambarumeter.main.monitor.HeartRateMonitor;
import net.kazhik.gambarumeter.main.monitor.HeartRateSensorValueListener;
import net.kazhik.gambarumeter.main.notification.HeartRateNotificationView;
import net.kazhik.gambarumeter.main.view.HeartRateView;
import net.kazhik.gambarumeterlib.storage.DataStorage;
import net.kazhik.gambarumeterlib.storage.WorkoutTable;

import java.util.Date;

/**
 * Created by kazhik on 14/11/11.
 */
public class HeartRateMainFragment extends MainFragment
        implements HeartRateSensorValueListener {
    private SensorManager sensorManager;

    private HeartRateMonitor heartRateMonitor;

    private HeartRateView heartRateView = new HeartRateView();

    private HeartRateNotificationView notificationView = new HeartRateNotificationView();

    private int connectedService = 0;

    private static final String TAG = "HeartRateMainFragment";


    @Override
    public void refreshView() {
        Log.d(TAG, "refreshView");
    }


    @Override
    public void onDestroy() {
        if (this.heartRateMonitor != null) {
            this.heartRateMonitor.terminate();
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
            boolean bound = activity.bindService(intent, this, Context.BIND_AUTO_CREATE);
            if (bound) {
                this.setBound();
            }
            this.heartRateMonitor = new HeartRateMonitor(); // temporary
        }

    }
    protected void initializeUI() {
        super.initializeUI();
        
        Activity activity = this.getActivity();

        if (this.heartRateMonitor != null) {
            this.heartRateView.initialize((TextView)activity.findViewById(R.id.bpm));
            activity.findViewById(R.id.heart_rate).setVisibility(View.VISIBLE);
        } else {
            activity.findViewById(R.id.heart_rate).setVisibility(View.GONE);
        }
        activity.findViewById(R.id.separator).setVisibility(View.GONE);
        activity.findViewById(R.id.distance).setVisibility(View.GONE);

        this.notificationView.initialize(activity);

    }

    protected void startWorkout() {
        this.notificationView.clear();

        if (this.heartRateMonitor != null) {
            this.heartRateView.setCurrentRate(0)
                    .refresh();
            this.heartRateMonitor.start();
        }
        super.startWorkout();
    }
    protected long stopWorkout() {
        long stopTime = super.stopWorkout();

        if (this.heartRateMonitor != null) {
            this.heartRateMonitor.stop(stopTime);
        }
        this.notificationView.dismiss();

        return stopTime;
    }

    @Override
    protected DataMap putData(DataMap dataMap) {
        dataMap = super.putData(dataMap);

        dataMap = this.heartRateMonitor.putData(dataMap);

        DataMap workoutDataMap = new DataMap();
        workoutDataMap.putLong(DataStorage.COL_START_TIME,
                this.stopwatch.getStartTime());
        workoutDataMap.putLong(DataStorage.COL_STOP_TIME,
                this.stopwatch.getStopTime());
        workoutDataMap.putInt(DataStorage.COL_STEP_COUNT,
                this.stepCountMonitor.getStepCount());
        workoutDataMap.putInt(DataStorage.COL_HEART_RATE,
                this.heartRateMonitor.getAverageHeartRate());
        workoutDataMap.putFloat(DataStorage.COL_DISTANCE, 0);
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

            int heartRate = 0;
            if (this.heartRateMonitor != null) {
                heartRate = this.heartRateMonitor.getAverageHeartRate();
                this.heartRateMonitor.saveResult(db, startTime);
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
    public void onHeartRateChanged(long timestamp, int rate) {
        if (!this.stopwatch.isRunning()) {
            return;
        }
        this.heartRateView.setCurrentRate(rate);
        this.getActivity().runOnUiThread(this.heartRateView);

        Log.d(TAG, "new heart rate: " + (new Date(timestamp)).toString() + " / " + rate);
        this.notificationView.updateHeartRate(rate);

    }

    // ServiceConnection
    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        if (iBinder instanceof HeartRateMonitor.HeartRateBinder) {
            this.heartRateMonitor =
                    ((HeartRateMonitor.HeartRateBinder)iBinder).getService();
            this.heartRateMonitor.init(this.getActivity(), sensorManager, this);
            this.connectedService++;
        }
        super.onServiceConnected(componentName, iBinder);

    }

    protected boolean isServiceReady() {
        return (super.isServiceReady() && this.connectedService == 1);
    }

}
