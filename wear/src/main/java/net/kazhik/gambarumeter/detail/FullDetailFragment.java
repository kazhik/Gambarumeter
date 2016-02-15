package net.kazhik.gambarumeter.detail;

import android.app.Activity;
import android.database.SQLException;
import android.support.wearable.view.WearableListView;
import android.util.Log;

import net.kazhik.gambarumeterlib.entity.LapTime;
import net.kazhik.gambarumeterlib.storage.SplitTimeDataView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kazhik on 16/01/21.
 */
public class FullDetailFragment extends DetailFragment {
    private static final String TAG = "FullDetailFragment";

    public WearableListView.Adapter getAdapter() {
        Activity activity = this.getActivity();

        // read data from database
        List<LapTime> laptimes = new ArrayList<>();
        try {
            SplitTimeDataView splitTimeDataView = new SplitTimeDataView(activity);
            splitTimeDataView.open(true);
            laptimes = splitTimeDataView.selectLaps(this.getStartTime());
            splitTimeDataView.close();

        } catch (SQLException e) {
            Log.e(TAG, e.getMessage(), e);
        }

        return new FullDetailAdapter(activity, laptimes);

    }


}
