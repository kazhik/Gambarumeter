package net.kazhik.gambarumeter.history;

import android.app.Activity;
import android.app.Fragment;
import android.database.SQLException;
import android.os.Bundle;
import android.support.wearable.view.BoxInsetLayout;
import android.support.wearable.view.WearableListView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.kazhik.gambarumeter.R;
import net.kazhik.gambarumeter.storage.HeartRateTable;
import net.kazhik.gambarumeter.storage.WorkoutTable;

import java.util.List;

/**
 * Created by kazhik on 14/11/11.
 */
public class HistoryCardFragment extends Fragment implements WearableListView.ClickListener {
    private static final String TAG = "HistoryCardFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.workout_history, container, false);
    }

    private void readDatabase() {
        try {
            WorkoutTable workoutTable = new WorkoutTable(this.getActivity());
            workoutTable.open(true);
            List<String> startTimeList = workoutTable.selectAll(0);
            Log.d(TAG, "startTimeList: " + startTimeList.size());
            for (String startTime: startTimeList) {
                Log.d(TAG, "startTime: " + startTime);
            }
            workoutTable.close();

            HeartRateTable heartRateTable = new HeartRateTable(this.getActivity());
            heartRateTable.open(true);
            heartRateTable.close();


        } catch (SQLException e) {
            Log.e(TAG, e.getMessage(), e);
        }

    }

    public void refreshListItem() {
        WorkoutTable workoutTable = new WorkoutTable(this.getActivity());
        workoutTable.open(true);
        List<String> startTimeList = workoutTable.selectAll(0);
        workoutTable.close();

        Log.d(TAG, "listsize:" + startTimeList.size());
        for (String startTime: startTimeList) {
            Log.d(TAG, "startTime: " + startTime);
        }

        HistoryAdapter adapter = new HistoryAdapter(this.getActivity(), startTimeList);
        WearableListView listView = (WearableListView)this.getActivity().findViewById(R.id.history_list);
        listView.setAdapter(adapter);
        listView.setClickListener(this);
        adapter.notifyDataSetChanged();

    }

    @Override
    public void onStart() {
        super.onStart();

        BoxInsetLayout historyLayout = (BoxInsetLayout)this.getActivity().findViewById(R.id.history_layout);
        Log.d(TAG, "historyLayout:" + historyLayout.isRound());

        this.refreshListItem();

    }


    @Override
    public void onClick(WearableListView.ViewHolder viewHolder) {
        int position = (Integer)viewHolder.itemView.getTag();
        Log.d(TAG, "Click list item: " + position);

    }

    @Override
    public void onTopEmptyRegionClick() {

    }
}
