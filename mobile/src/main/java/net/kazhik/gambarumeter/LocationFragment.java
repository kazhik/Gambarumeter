package net.kazhik.gambarumeter;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by kazhik on 16/02/07.
 */
public class LocationFragment extends Fragment {
    private TrackView trackView = new TrackView();
    private static final String TAG = "LocationFragment";

    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.location_fragment, container, false);

        this.trackView.initialize(view);
        return view;
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        this.trackView.onCreate(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        this.trackView.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        this.trackView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        this.trackView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.trackView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        this.trackView.onLowMemory();
    }

}
