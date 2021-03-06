package net.kazhik.gambarumeter.history;

import android.app.FragmentManager;
import android.app.FragmentTransaction;

import net.kazhik.gambarumeter.R;
import net.kazhik.gambarumeter.detail.FullDetailFragment;

/**
 * Created by kazhik on 16/01/21.
 */
public class FullHistoryFragment extends LocationHistoryFragment {
    private static final String TAG = "FullHistoryFragment";

    public void openDetailFragment() {
        FragmentManager fragmentManager = getFragmentManager();
        FullDetailFragment fragment = new FullDetailFragment();
        fragment.setStartTime(this.getStartTime());

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.history_layout, fragment);
        fragmentTransaction.commit();

    }

}
