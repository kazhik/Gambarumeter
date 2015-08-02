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
import net.kazhik.gambarumeterlib.storage.SplitTable;

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
        List<SplitTime> splits = new ArrayList<SplitTime>();
        try {
            SplitTable splitTable = new SplitTable(activity);
            splitTable.open(true);
            splits = splitTable.selectAll(this.getStartTime());
            splitTable.close();

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
        List<LapTime> laptimes = new ArrayList<LapTime>();
        long prevTimestamp = 0;
        for (SplitTime split: splits) {
            long currentLap = (split.getTimestamp() - prevTimestamp) / 1000;
            if (prevTimestamp != 0) {
                Log.d(TAG, split.getDistance() + ": " + currentLap);

                float distance = Util.convertMeter(split.getDistance(), prefDistanceUnit);
                
                laptimes.add(new LapTime(split.getTimestamp(),
                        distance, currentLap, distanceUnitStr));
            }
            prevTimestamp = split.getTimestamp();
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