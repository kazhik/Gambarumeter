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

import net.kazhik.gambarumeter.R;
import net.kazhik.gambarumeter.Util;
import net.kazhik.gambarumeterlib.entity.LapTime;
import net.kazhik.gambarumeterlib.storage.SplitTimeView;

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
            SplitTimeView splitTimeView = new SplitTimeView(activity);
            splitTimeView.open(true);
            laptimes = splitTimeView.selectLaps(this.getStartTime());
            splitTimeView.close();

        } catch (SQLException e) {
            Log.e(TAG, e.getMessage(), e);
        }

        return new FullDetailAdapter(activity, laptimes);

    }


}
