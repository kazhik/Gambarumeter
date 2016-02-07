package net.kazhik.gambarumeter;

import android.app.Activity;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import net.kazhik.gambarumeterlib.Util;
import net.kazhik.gambarumeterlib.entity.SplitTimeStepCount;
import net.kazhik.gambarumeterlib.storage.SplitTimeView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by kazhik on 16/02/06.
 */
public class SplitTimeFragment extends Fragment {
    private SimpleAdapter listAdapter;
    private List<HashMap<String, String>> mapList = new ArrayList<>();
    private static final String TAG = "SplitTimeFragment";

    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.splittime_fragment, container, false);
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        long startTime = getArguments().getLong("startTime");

        this.loadData(startTime);
    }
    private void loadData(long startTime) {
        Activity activity = this.getActivity();


        SplitTimeView splitTimeView = new SplitTimeView(activity);
        splitTimeView.openReadonly();
        List<SplitTimeStepCount> splits = splitTimeView.selectAll(startTime);
        splitTimeView.close();

        if (splits.isEmpty()) {
            Toast.makeText(activity, activity.getString(R.string.nodata),
                    Toast.LENGTH_LONG).show();
            return;
        }

        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(activity);
        String prefDistanceUnit = prefs.getString("distanceUnit", "metre");
        String distanceUnit =
                Util.distanceUnitDisplayStr(prefDistanceUnit,
                        activity.getResources());

        SplitTimeStepCount first = splits.remove(0);
        long prevTimestamp = first.getTimestampSec();

        for (SplitTimeStepCount splitTime: splits) {
            HashMap<String, String> map = new HashMap<>();

            float distance = splitTime.getDistance();
            distance /= Util.lapDistance(prefDistanceUnit);
            String distanceStr = String.format("%.2f%s",
                    distance, distanceUnit);
            map.put("distance", distanceStr);

            long timestamp = splitTime.getTimestampSec();
            long laptime = timestamp - prevTimestamp;
            String laptimeStr = Util.formatLapTime(laptime);
            map.put("laptime", laptimeStr);
            prevTimestamp = timestamp;

            map.put("stepcount", String.valueOf(splitTime.getStepCount()));

            //TODO: heartrate
            map.put("heartrate", "0");

            this.mapList.add(map);
        }
        this.listAdapter = new SimpleAdapter(activity,
                this.mapList,
                R.layout.splittime_item,
                new String[] { "distance", "laptime", "stepcount", "heartrate" },
                new int[] { R.id.distance, R.id.laptime,
                        R.id.stepcount_value, R.id.heartrate_value }
        );
        ListView lv = (ListView)activity.findViewById(R.id.splittime_list);
        lv.setAdapter(this.listAdapter);
        lv.setSelection(0);
        this.listAdapter.notifyDataSetChanged();

    }

}
