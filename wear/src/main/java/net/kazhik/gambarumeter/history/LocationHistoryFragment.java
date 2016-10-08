package net.kazhik.gambarumeter.history;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.util.Log;

import net.kazhik.gambarumeter.R;
import net.kazhik.gambarumeter.detail.LocationDetailFragment;
import net.kazhik.gambarumeterlib.storage.LocationTable;

/**
 * Created by kazhik on 14/11/11.
 */
public class LocationHistoryFragment extends HistoryFragment {

    private static final String TAG = "LocationHistoryFragment";

    public void openDetailFragment() {
        FragmentManager fragmentManager = getFragmentManager();
        LocationDetailFragment fragment = new LocationDetailFragment();
        fragment.setStartTime(this.getStartTime());

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.history_layout, fragment);
        fragmentTransaction.commit();

    }

}
