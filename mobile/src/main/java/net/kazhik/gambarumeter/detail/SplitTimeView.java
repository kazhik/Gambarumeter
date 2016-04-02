package net.kazhik.gambarumeter.detail;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import net.kazhik.gambarumeter.R;
import net.kazhik.gambarumeterlib.DistanceUtil;
import net.kazhik.gambarumeterlib.TimeUtil;
import net.kazhik.gambarumeterlib.entity.SplitTimeStepCount;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by kazhik on 16/02/08.
 */
public class SplitTimeView implements DetailView {
    private Context context;
    private View root;
    private SimpleAdapter listAdapter;

    private List<HashMap<String, String>> mapList = new ArrayList<>();
    private static final String TAG = "SplitTimeView";

    @Override
    public void setContext(Context context) {
        this.context = context;
    }
    @Override
    public void setRootView(View root) {
        this.root = root;

    }
    public void load(List<SplitTimeStepCount> splits) {
        DistanceUtil distanceUtil = DistanceUtil.getInstance(this.context);

        SplitTimeStepCount first = splits.remove(0);
        long prevTimestamp = first.getTimestampSec();
        int prevStepCount = 0;

        for (SplitTimeStepCount splitTime: splits) {
            HashMap<String, String> map = new HashMap<>();

            String distanceStr =
                    distanceUtil.getDistanceAndUnitStr(splitTime.getDistance());
            map.put("distance", distanceStr);

            long timestamp = splitTime.getTimestampSec();
            long laptime = timestamp - prevTimestamp;
            String laptimeStr = TimeUtil.formatSec(laptime);
            map.put("laptime", laptimeStr);
            prevTimestamp = timestamp;

            int currStepCount = splitTime.getStepCount();
            map.put("stepcount", String.valueOf(currStepCount - prevStepCount));
            prevStepCount = currStepCount;

            //TODO: heartrate
            map.put("heartrate", "0");

            this.mapList.add(map);
        }
        this.listAdapter = new SimpleAdapter(this.context,
                this.mapList,
                R.layout.splittime_item,
                new String[] { "distance", "laptime", "stepcount", "heartrate" },
                new int[] { R.id.distance, R.id.laptime,
                        R.id.stepcount_value, R.id.heartrate_value }
        );
        ListView lv = (ListView)this.root.findViewById(R.id.splittime_list);
        lv.setAdapter(this.listAdapter);
        lv.setSelection(0);
        this.listAdapter.notifyDataSetChanged();

    }

    @Override
    public void onCreate(Bundle savedInstance) {

    }

    @Override
    public void onResume() {

    }

    @Override
    public void onPause() {

    }

    @Override
    public void onDestroy() {

    }

    @Override
    public void onLowMemory() {

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

    }
}

