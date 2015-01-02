package net.kazhik.gambarumeter.history;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.wearable.view.WearableListView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.kazhik.gambarumeter.R;
import net.kazhik.gambarumeter.detail.HeartRateDetailFragment;
import net.kazhik.gambarumeter.detail.LocationDetailFragment;
import net.kazhik.gambarumeter.entity.WorkoutInfo;
import net.kazhik.gambarumeter.storage.WorkoutTable;

import java.util.List;

/**
 * Created by kazhik on 14/11/11.
 */
public class HistoryFragment extends Fragment
        implements WearableListView.ClickListener,
        View.OnLongClickListener,
        DialogInterface.OnClickListener {

    private enum DetailMode {
        NONE,
        HEART_RATE,
        LOCATION
    };

    private DetailMode detailMode = DetailMode.NONE;
    private long startTime;
    private boolean editMode = false;
    private static final String TAG = "HistoryFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.workout_history, container, false);
    }

    public long getStartTime() {
        return this.startTime;
    }

    public void refreshListItem() {
        WorkoutTable workoutTable = new WorkoutTable(this.getActivity());
        workoutTable.open(true);
        List<WorkoutInfo> workoutInfos = workoutTable.selectAll();
        workoutTable.close();

        HistoryAdapter adapter = new HistoryAdapter(this.getActivity(), workoutInfos);
        WearableListView listView = (WearableListView)this.getActivity().findViewById(R.id.history_list);
        if (listView == null) {
            Log.d(TAG, "historyList not found");
            return;
        }
        listView.setAdapter(adapter);
        listView.setClickListener(this);
        listView.setGreedyTouchMode(true);
        listView.setLongClickable(true);
        listView.setOnLongClickListener(this);
        adapter.notifyDataSetChanged();

        if (!workoutInfos.isEmpty()) {
            WorkoutInfo workoutInfo = workoutInfos.get(0);
            this.startTime = workoutInfo.getStartTime();
            if (workoutInfo.getHeartRate() > 0) {
                this.detailMode = DetailMode.HEART_RATE;
            } else if (workoutInfo.getDistance() > 0) {
                this.detailMode = DetailMode.LOCATION;
            }

        }
    }

    @Override
    public void onStart() {
        super.onStart();

        this.refreshListItem();

    }


    @Override
    public void onClick(WearableListView.ViewHolder viewHolder) {
        this.startTime = (Long)viewHolder.itemView.getTag();

        if (this.editMode) {
            AlertDialog confirmDelete =
                    new AlertDialog.Builder(this.getActivity())
                            .setMessage(R.string.confirm_delete)
                            .setPositiveButton(R.string.delete, this)
                            .setNegativeButton(R.string.cancel, this)
                            .create();

            confirmDelete.show();
        } else {
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            if (this.detailMode == DetailMode.HEART_RATE) {
                Log.d(TAG, "onClick, heart rate");
                HeartRateDetailFragment fragment = new HeartRateDetailFragment();
                fragment.setStartTime(startTime);
                fragmentTransaction.add(R.id.history_layout, fragment);
            } else if (this.detailMode == DetailMode.LOCATION) {
                Log.d(TAG, "onClick, location");
                LocationDetailFragment fragment = new LocationDetailFragment();
                fragment.setStartTime(startTime);
                fragmentTransaction.add(R.id.history_layout, fragment);
            }
            fragmentTransaction.commit();
        }

    }

    @Override
    public void onTopEmptyRegionClick() {
        this.editMode = !this.editMode;

        TextView modeText = (TextView)this.getActivity().findViewById(R.id.mode);
        int msgId = this.editMode? R.string.edit_mode: R.string.view_mode;
        modeText.setText(msgId);
    }
    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            Log.d(TAG, "Delete: " + this.startTime);

            WorkoutTable workoutTable = new WorkoutTable(this.getActivity());
            workoutTable.open(false);
            workoutTable.delete(this.startTime);
            workoutTable.close();

            this.refreshListItem();
        } else if (which == DialogInterface.BUTTON_NEGATIVE) {
            Log.d(TAG, "Cancel");
        }

    }


    @Override
    public boolean onLongClick(View v) {
        Log.d(TAG, "onLongClick");
        return false;
    }
}
