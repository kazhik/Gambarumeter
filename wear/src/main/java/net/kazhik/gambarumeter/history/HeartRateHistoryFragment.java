package net.kazhik.gambarumeter.history;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.util.Log;

import net.kazhik.gambarumeter.R;
import net.kazhik.gambarumeter.detail.HeartRateDetailFragment;
import net.kazhik.gambarumeterlib.storage.HeartRateTable;
import net.kazhik.gambarumeterlib.storage.LocationTable;

/**
 * Created by kazhik on 14/11/11.
 */
public class HeartRateHistoryFragment extends HistoryFragment {

    private static final String TAG = "HeartRateHistFragment";

    public void openDetailFragment() {
        FragmentManager fragmentManager = getFragmentManager();
        HeartRateDetailFragment fragment = new HeartRateDetailFragment();
        fragment.setStartTime(this.getStartTime());

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.history_layout, fragment);
        fragmentTransaction.commit();

    }

    protected void deleteRecord(Context context, long startTime) {
        Log.d(TAG, "deleteRecord: " + startTime);

        super.deleteRecord(context, startTime);

        HeartRateTable heartRateTable = new HeartRateTable(context);
        heartRateTable.open(false);
        heartRateTable.delete(startTime);
        heartRateTable.close();

    }



}
