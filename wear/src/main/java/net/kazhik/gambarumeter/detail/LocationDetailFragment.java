package net.kazhik.gambarumeter.detail;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.database.SQLException;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import net.kazhik.gambarumeter.R;
import net.kazhik.gambarumeter.entity.SensorValue;
import net.kazhik.gambarumeter.storage.HeartRateTable;
import net.kazhik.gambarumeter.storage.LocationTable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kazhik on 14/11/18.
 */
public class LocationDetailFragment extends Fragment implements View.OnClickListener {

    private long startTime;
    private static final String TAG = "LocationDetailFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.heartrate_detail, container, false);
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Activity activity = this.getActivity();

        List<Location> locations = new ArrayList<Location>();
        try {
            LocationTable locTable = new LocationTable(this.getActivity());
            locTable.open(true);
            locations = locTable.selectAll(this.startTime, 0);
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
    public void setStartTime(long startTime) {
        this.startTime = startTime;

    }

    @Override
    public void onClick(View v) {

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.remove(this);
        fragmentTransaction.commit();


    }
}