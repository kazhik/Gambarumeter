package net.kazhik.gambarumeter.history;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.support.wearable.view.WearableListView;

import net.kazhik.gambarumeter.R;
import net.kazhik.gambarumeter.detail.HeartRateDetailFragment;

/**
 * Created by kazhik on 14/11/11.
 */
public class HeartRateHistoryFragment extends HistoryFragment {

    private static final String TAG = "HeartRateHistoryFragment";

    public void openDetailFragment() {
        FragmentManager fragmentManager = getFragmentManager();
        HeartRateDetailFragment fragment = new HeartRateDetailFragment();
        fragment.setStartTime(this.getStartTime());

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.history_layout, fragment);
        fragmentTransaction.commit();

    }



}
