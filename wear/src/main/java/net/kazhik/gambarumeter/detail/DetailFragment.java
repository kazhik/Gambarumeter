package net.kazhik.gambarumeter.detail;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.wearable.view.WearableListView;
import android.util.Log;

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
    public abstract void refreshListItem();

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