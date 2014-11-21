package net.kazhik.gambarumeter.history;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.database.SQLException;
import android.os.Bundle;
import android.support.wearable.view.BoxInsetLayout;
import android.support.wearable.view.CardFragment;
import android.support.wearable.view.WearableListView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.kazhik.gambarumeter.R;
import net.kazhik.gambarumeter.detail.DetailFragment;
import net.kazhik.gambarumeter.entity.WorkoutInfo;
import net.kazhik.gambarumeter.storage.HeartRateTable;
import net.kazhik.gambarumeter.storage.WorkoutTable;

import java.util.List;

/**
 * Created by kazhik on 14/11/11.
 */
public class HistoryFragment extends Fragment
        implements WearableListView.ClickListener, View.OnLongClickListener {
    private static final String TAG = "HistoryFragment";
    private long startTime;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.workout_history, container, false);
    }

    private void readDatabase() {
        try {
            WorkoutTable workoutTable = new WorkoutTable(this.getActivity());
            workoutTable.open(true);
            List<WorkoutInfo> workoutInfos = workoutTable.selectAll(0);
            Log.d(TAG, "workoutInfos: " + workoutInfos.size());
            workoutTable.close();

            HeartRateTable heartRateTable = new HeartRateTable(this.getActivity());
            heartRateTable.open(true);
            heartRateTable.close();


        } catch (SQLException e) {
            Log.e(TAG, e.getMessage(), e);
        }

    }
    public long getStartTime() {
        return this.startTime;
    }

    public void refreshListItem() {
        WorkoutTable workoutTable = new WorkoutTable(this.getActivity());
        workoutTable.open(true);
        List<WorkoutInfo> workoutInfos = workoutTable.selectAll(0);
        workoutTable.close();

        Log.d(TAG, "workoutInfos: " + workoutInfos.size());

        HistoryAdapter adapter = new HistoryAdapter(this.getActivity(), workoutInfos);
        WearableListView listView = (WearableListView)this.getActivity().findViewById(R.id.history_list);
        listView.setAdapter(adapter);
        listView.setClickListener(this);
        listView.setGreedyTouchMode(true);
        listView.setLongClickable(true);
        listView.setOnLongClickListener(this);
        adapter.notifyDataSetChanged();

        this.startTime = workoutInfos.get(0).getStartTime();
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
        long startTime = (Long)viewHolder.itemView.getTag();

        DetailFragment fragment = new DetailFragment();
        fragment.read(startTime);

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.frame_layout, fragment);
        fragmentTransaction.commit();

    }

    @Override
    public void onTopEmptyRegionClick() {
        Log.d(TAG, "onTopEmptyRegionClick");

    }

    @Override
    public boolean onLongClick(View v) {
        Log.d(TAG, "onLongClick");
        return false;
    }
}
