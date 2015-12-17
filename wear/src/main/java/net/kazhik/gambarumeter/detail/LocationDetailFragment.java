package net.kazhik.gambarumeter.detail;

import android.app.Activity;
import android.content.SharedPreferences;
import android.database.SQLException;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.wearable.view.WearableListView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import net.kazhik.gambarumeter.R;
import net.kazhik.gambarumeter.Util;
import net.kazhik.gambarumeterlib.entity.LapTime;
import net.kazhik.gambarumeterlib.entity.SplitTime;
import net.kazhik.gambarumeterlib.entity.SplitTimeStepCount;
import net.kazhik.gambarumeterlib.storage.SplitTable;
import net.kazhik.gambarumeterlib.storage.SplitTimeView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kazhik on 14/11/18.
 */
public class LocationDetailFragment extends DetailFragment {

    private static final String TAG = "LocationDetailFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.location_detail, container, false);
    }
    public void refreshListItem() {
        Activity activity = this.getActivity();
        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(activity);
        String prefDistanceUnit = prefs.getString("distanceUnit", "metre");

        String distanceUnitStr =
                Util.distanceUnitDisplayStr(prefDistanceUnit, activity.getResources());
        
        // read data from database
        List<SplitTimeStepCount> splits = new ArrayList<>();
        try {
            SplitTimeView splitTimeView = new SplitTimeView(activity);
            splitTimeView.open(true);
            splits = splitTimeView.selectAll(this.getStartTime());
            splitTimeView.close();

        } catch (SQLException e) {
            Log.e(TAG, e.getMessage(), e);
        }

        if (splits.isEmpty()) {
            Toast.makeText(this.getActivity(),
                    R.string.no_detail,
                    Toast.LENGTH_SHORT)
                    .show();
            this.close();
            return;
        }

        // calculate laptimes
        List<LapTime> laptimes = new ArrayList<>();
        long prevTimestamp = 0;
        int prevStepCount = 0;
        for (SplitTimeStepCount split: splits) {
            long currentLap = (split.getTimestamp() - prevTimestamp) / 1000;
            if (prevTimestamp != 0) {
                Log.d(TAG, split.getDistance() + ": " + currentLap);

                float distance = Util.convertMeter(split.getDistance(), prefDistanceUnit);
                
                laptimes.add(new LapTime(split.getTimestamp(),
                        distance, currentLap, split.getStepCount() - prevStepCount,
                        distanceUnitStr));
            }
            prevTimestamp = split.getTimestamp();
            prevStepCount = split.getStepCount();
        }

        // update listview
        LocationDetailAdapter adapter =
                new LocationDetailAdapter(this.getActivity(), laptimes);

        WearableListView listView =
                (WearableListView)this.getActivity().findViewById(R.id.location_list);
        if (listView == null) {
            Log.d(TAG, "locationList not found");
            return;
        }
        listView.setAdapter(adapter);
        listView.setGreedyTouchMode(true);
        listView.setClickListener(this);
        adapter.notifyDataSetChanged();

    }


}