package net.kazhik.gambarumeter.detail;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.database.SQLException;
import android.os.Bundle;
import android.support.wearable.view.WearableListView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.kazhik.gambarumeter.R;
import net.kazhik.gambarumeter.entity.HeartRateDetail;
import net.kazhik.gambarumeter.entity.SensorValue;
import net.kazhik.gambarumeter.storage.HeartRateTable;
import net.kazhik.gambarumeter.storage.StepCountTable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kazhik on 14/11/18.
 */
public class HeartRateDetailFragment extends Fragment
        implements WearableListView.ClickListener {

    private long startTime;
    private static final String TAG = "HeartRateDetailFragment";

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.heartrate_detail, container, false);
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Log.d(TAG, "onActivityCreated");

        this.refreshListItem();
    }
    public void setStartTime(long startTime) {
        this.startTime = startTime;

    }
    public void refreshListItem() {
        Activity activity = this.getActivity();
        List<SensorValue> heartRates = new ArrayList<SensorValue>();
        try {
            HeartRateTable heartRateTable = new HeartRateTable(activity);
            heartRateTable.open(true);
            heartRates = heartRateTable.selectAll(this.startTime);

            heartRateTable.close();

        } catch (SQLException e) {
            Log.e(TAG, e.getMessage(), e);
        }

        if (heartRates.isEmpty()) {
            return;
        }
        StepCountTable stepCountTable = new StepCountTable(activity);
        stepCountTable.open(true);
        List<HeartRateDetail> heartRateDetails = new ArrayList<HeartRateDetail>();
        int prevSteps = 0;
        long prevTimestamp = this.startTime;
        for (SensorValue heartRate: heartRates) {
            long timestamp = heartRate.getTimestamp();
            int steps = stepCountTable.select(timestamp);
            int stepsPerMinute = 0;
            if (timestamp - prevTimestamp > 0) {
                stepsPerMinute = (steps - prevSteps) /
                        (int)((timestamp - prevTimestamp) / 1000 / 60);
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
    @Override
    public void onClick(WearableListView.ViewHolder viewHolder) {
        Log.d(TAG, "onClick");

    }

    @Override
    public void onTopEmptyRegionClick() {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.remove(this);
        fragmentTransaction.commit();

    }

}