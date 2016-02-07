package net.kazhik.gambarumeter;

import android.app.Fragment;
import android.content.Context;

import net.kazhik.gambarumeterlib.entity.SplitTimeStepCount;
import net.kazhik.gambarumeterlib.storage.SplitTimeView;

import java.util.List;

/**
 * Created by kazhik on 16/02/07.
 */
public class DetailFragment extends Fragment {

    public List<SplitTimeStepCount> getSplitTimeList(Context context, long startTime) {
        SplitTimeView splitTimeView = new SplitTimeView(context);
        splitTimeView.openReadonly();
        List<SplitTimeStepCount> splits = splitTimeView.selectAll(startTime);
        splitTimeView.close();

        return splits;
    }
}
