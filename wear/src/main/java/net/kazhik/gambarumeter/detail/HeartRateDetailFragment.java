package net.kazhik.gambarumeter.detail;

import android.app.Activity;
import android.database.SQLException;
import android.support.wearable.view.WearableListView;
import android.util.Log;

import net.kazhik.gambarumeterlib.entity.HeartRateDetail;
import net.kazhik.gambarumeterlib.entity.SensorValue;
import net.kazhik.gambarumeterlib.storage.HeartRateTable;
import net.kazhik.gambarumeterlib.storage.StepCountTable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kazhik on 14/11/18.
 */
public class HeartRateDetailFragment extends DetailFragment {

    private static final String TAG = "HeartRateDetailFragment";

    public WearableListView.Adapter getAdapter() {
        Activity activity = this.getActivity();
        long startTime = this.getStartTime();

        List<SensorValue> heartRates = new ArrayList<>();
        try {
            HeartRateTable heartRateTable = new HeartRateTable(activity);
            heartRateTable.open(true);
            heartRates = heartRateTable.selectAll(startTime);

            heartRateTable.close();

        } catch (SQLException e) {
            Log.e(TAG, e.getMessage(), e);
        }

        StepCountTable stepCountTable = new StepCountTable(activity);
        stepCountTable.open(true);
        List<HeartRateDetail> heartRateDetails = new ArrayList<>();
        int prevSteps = 0;
        long prevTimestamp = startTime;
        for (SensorValue heartRate: heartRates) {
            long timestamp = heartRate.getTimestamp();
            int steps = stepCountTable.select(timestamp);
            int stepsPerMinute = 0;
            int minutes = (int)((timestamp - prevTimestamp) / 1000 / 60);
            if (minutes > 0) {
                stepsPerMinute = (steps - prevSteps) / minutes;
            }
            prevSteps = steps;
            prevTimestamp = timestamp;

            HeartRateDetail heartRateDetail = new HeartRateDetail()
                    .setTimestamp(timestamp)
                    .setHeartRate((int) heartRate.getValue())
                    .setStepCount(stepsPerMinute);
            heartRateDetails.add(heartRateDetail);

        }
        stepCountTable.close();

        return new HeartRateDetailAdapter(activity, heartRateDetails);



    }

}