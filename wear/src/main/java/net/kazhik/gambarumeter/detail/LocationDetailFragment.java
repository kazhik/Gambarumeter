package net.kazhik.gambarumeter.detail;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.database.SQLException;
import android.location.Location;
import android.os.Bundle;
import android.support.wearable.view.WearableListView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.kazhik.gambarumeter.R;
import net.kazhik.gambarumeter.entity.Lap;
import net.kazhik.gambarumeter.storage.LapTable;
import net.kazhik.gambarumeter.storage.LocationTable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kazhik on 14/11/18.
 */
public class LocationDetailFragment extends Fragment
        implements WearableListView.ClickListener {

    private long startTime;
    private static final String TAG = "LocationDetailFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.location_detail, container, false);
    }
    private void getLocations() {
        List<Location> locations = new ArrayList<Location>();
        try {
            LocationTable locTable = new LocationTable(this.getActivity());
            locTable.open(true);
            locations = locTable.selectAll(this.startTime);
            locTable.close();

        } catch (SQLException e) {
            Log.e(TAG, e.getMessage(), e);
        }

        if (locations.isEmpty()) {
            return;
        }

        for (Location loc: locations) {
            Log.d(TAG, loc.getTime() + "; Lat: " + loc.getLatitude()
                            + "; Lon: " + loc.getLongitude()
                            + "; Alt: " + loc.getAltitude()
                            + "; Accuracy: " + loc.getAccuracy()
            );
        }


    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        this.refreshListItem();
    }
    public void refreshListItem() {
        Activity activity = this.getActivity();
        List<Lap> laps = new ArrayList<Lap>();
        try {
            LapTable lapTable = new LapTable(activity);
            lapTable.open(true);
            laps = lapTable.selectAll(this.startTime);
            lapTable.close();

        } catch (SQLException e) {
            Log.e(TAG, e.getMessage(), e);
        }

        if (laps.isEmpty()) {
            return;
        }

        List<Lap> laptimes = new ArrayList<Lap>();
        long prevTimestamp = 0;
        for (Lap lap: laps) {
            long currentLap = (lap.getTimestamp() - prevTimestamp) / 1000;
            if (prevTimestamp != 0) {
                Log.d(TAG, lap.getDistance() + ": " + currentLap);

                laptimes.add(new Lap(lap.getTimestamp(), lap.getDistance(), currentLap));
            }
            prevTimestamp = lap.getTimestamp();
        }

        LocationDetailAdapter adapter =
                new LocationDetailAdapter(this.getActivity(), laptimes);

        WearableListView listView =
                (WearableListView)this.getActivity().findViewById(R.id.location_list);
        if (listView == null) {
            Log.d(TAG, "locationList not found");
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

    @Override
    public void onClick(WearableListView.ViewHolder viewHolder) {
        Log.d(TAG, "onClick");

    }

    @Override
    public void onTopEmptyRegionClick() {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.remove(this);
        fragmentTransaction.commit();

    }
}