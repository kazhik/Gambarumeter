package net.kazhik.gambarumeter.detail;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.wearable.view.WearableListView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import net.kazhik.gambarumeter.R;

/**
 * Created by kazhik on 14/11/18.
 */
public abstract class DetailFragment extends Fragment
        implements WearableListView.ClickListener {

    private long startTime;
    private static final String TAG = "DetailFragment";

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        this.refreshListItem();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.detail, container, false);
    }

    public abstract WearableListView.Adapter getAdapter();
    public void refreshListItem() {
        WearableListView.Adapter adapter = this.getAdapter();
        if (adapter.getItemCount() == 0) {
            Toast.makeText(this.getActivity(),
                    R.string.no_detail,
                    Toast.LENGTH_SHORT)
                    .show();
            this.close();
            return;
        }

        Activity activity = this.getActivity();

        WearableListView listView =
                (WearableListView)activity.findViewById(R.id.detail_list);
        if (listView == null) {
            Log.d(TAG, "heartrate_list not found");
            return;
        }
        listView.setAdapter(adapter);
        listView.setGreedyTouchMode(true);
        listView.setClickListener(this);
        adapter.notifyDataSetChanged();

    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;

    }
    public long getStartTime() {
        return this.startTime;
        
    }

    @Override
    public void onClick(WearableListView.ViewHolder viewHolder) {
        Log.d(TAG, "onClick");

    }
    public void close() {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.remove(this);
        fragmentTransaction.commit();
        
    }

    @Override
    public void onTopEmptyRegionClick() {
        close();
    }
}