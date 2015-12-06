package net.kazhik.gambarumeter.detail;

import android.app.Activity;
import android.database.SQLException;
import android.os.Bundle;
import android.support.wearable.view.WearableListView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import net.kazhik.gambarumeter.R;
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

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.heartrate_detail, container, false);
    }
    public void refreshListItem() {
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

        if (heartRates.isEmpty()) {
            Toast.makeText(this.getActivity(),
                    R.string.no_detail,
                    Toast.LENGTH_SHORT)
                    .show();
            this.close();
            return;
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

        HeartRateDetailAdapter adapter =
                new HeartRateDetailAdapter(activity, heartRateDetails);

        WearableListView listView =
                (WearableListView)activity.findViewById(R.id.heartrate_list);
        if (listView == null) {
            Log.d(TAG, "heartrate_list not found");
            return;
        }
        listView.setAdapter(adapter);
        listView.setGreedyTouchMode(true);
        listView.setClickListener(this);
        adapter.notifyDataSetChanged();

    }

}