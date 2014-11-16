package net.kazhik.gambarumeter.history;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.database.SQLException;
import android.os.Bundle;
import android.support.wearable.view.BoxInsetLayout;
import android.support.wearable.view.WearableListView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.kazhik.gambarumeter.R;
import net.kazhik.gambarumeter.entity.WorkoutInfo;
import net.kazhik.gambarumeter.storage.HeartRateTable;
import net.kazhik.gambarumeter.storage.WorkoutTable;

import java.util.List;

/**
 * Created by kazhik on 14/11/11.
 */
public class HistoryCardFragment extends Fragment
        implements WearableListView.ClickListener,DialogInterface.OnClickListener {
    private static final String TAG = "HistoryCardFragment";
    private long startTimeToDelete;

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
        this.startTimeToDelete = (Long)viewHolder.itemView.getTag();

        AlertDialog confirmDelete =
                new AlertDialog.Builder(this.getActivity(), AlertDialog.THEME_HOLO_DARK)
                .setMessage(R.string.confirm_delete)
                .setPositiveButton(R.string.delete, this)
                .setNegativeButton(R.string.cancel, this)
                .setInverseBackgroundForced(true)
                .create();

        confirmDelete.show();
    }

    @Override
    public void onTopEmptyRegionClick() {

    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            Log.d(TAG, "Delete: " + this.startTimeToDelete);

            WorkoutTable workoutTable = new WorkoutTable(this.getActivity());
            workoutTable.open(false);
            workoutTable.delete(this.startTimeToDelete);
            workoutTable.close();

            this.refreshListItem();
        } else if (which == DialogInterface.BUTTON_NEGATIVE) {
            Log.d(TAG, "Cancel");
        }

    }
}
