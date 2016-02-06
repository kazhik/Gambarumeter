package net.kazhik.gambarumeter;

import android.app.Activity;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import net.kazhik.gambarumeterlib.Util;
import net.kazhik.gambarumeterlib.entity.WorkoutInfo;
import net.kazhik.gambarumeterlib.storage.WorkoutTable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by kazhik on 16/01/31.
 */
public class ExternalFileFragment extends Fragment {
    private ExternalFile externalFile;
    private SimpleAdapter listAdapter;
    private ArrayList<HashMap<String, String>> mapList = new ArrayList<>();
    private static final String TAG = "ExternalFileFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.external_fragment, container, false);
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        this.initializeUI();
        this.loadList();
    }
    private void loadList() {
        Activity activity = this.getActivity();

        this.mapList.clear();
        /*
        for (WorkoutInfo workout: workoutList) {

            HashMap<String, String> map = new HashMap<>();
            long startTime = workout.getStartTime();
            map.put("startTime", String.valueOf(startTime));

            int flag = DateUtils.FORMAT_SHOW_YEAR |
                    DateUtils.FORMAT_SHOW_DATE |
                    DateUtils.FORMAT_SHOW_TIME;
            map.put("startTimeStr",
                    DateUtils.formatDateTime(activity,
                            startTime,
                            flag)
            );

            long stopTime = workout.getStopTime();
            int stepCount = workout.getStepCount();
            float distance = workout.getDistance();
            int heartRate = workout.getHeartRate();

            String resultStr = this.formatSplitTime(stopTime - startTime);
            if (distance > 0.0f) {
                distance /= Util.lapDistance(this.prefDistanceUnit);

                String distanceUnit =
                        Util.distanceUnitDisplayStr(this.prefDistanceUnit,
                                activity.getResources());

                String distanceStr = String.format("%.2f%s",
                        distance, distanceUnit);

                resultStr += "/" + distanceStr;
            } else if (heartRate > 0) {
                String heartRateStr = String.format("%d%s", heartRate,
                        activity.getResources().getString(R.string.bpm));
                resultStr += "/" + heartRateStr;
            }
            String stepCountStr = stepCount +
                    activity.getResources().getString(R.string.steps);
            resultStr += "/" + stepCountStr;

            map.put("result", resultStr);
            Log.d(TAG, "startTime: " + map.get("startTime") + ":" + map.get("startTimeStr"));
            this.mapList.add(map);

        }
        */

        this.listAdapter = new SimpleAdapter(activity,
                this.mapList,
                R.layout.external_item,
                new String[] { "startTimeStr" },
                new int[] { R.id.start_time }
        );
        ListView lv = (ListView)activity.findViewById(R.id.external_files);
        lv.setAdapter(this.listAdapter);
        lv.setSelection(0);
        this.listAdapter.notifyDataSetChanged();

    }
    private void initializeUI() {
        Activity activity = this.getActivity();

        ListView lv = (ListView) activity.findViewById(R.id.external_files);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent,
                                    View view,
                                    int position,
                                    long id) {

                ListView listView = (ListView) parent;
                Map<String, String> map = (Map<String, String>) listView
                        .getItemAtPosition(position);

                Log.d(TAG, "startTime: " + map.get("startTime") + ":" + map.get("startTimeStr"));

                ChartFragment chartFragment = new ChartFragment();
                Bundle chartParam = new Bundle();
                chartParam.putLong("startTime", Long.valueOf(map.get("startTime")));
                chartFragment.setArguments(chartParam);

                getFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, chartFragment)
                        .addToBackStack(null)
                        .commit();

            }
        });
        registerForContextMenu(lv);

    }


}
