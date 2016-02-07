package net.kazhik.gambarumeter;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by kazhik on 16/02/07.
 */
public class TrackView implements OnMapReadyCallback {
    private MapView mapView;
    private GoogleMap googleMap;
    private static final String TAG = "TrackView";

    public void initialize(View view) {
        this.mapView = (MapView) view.findViewById(R.id.map);

        this.mapView.getMapAsync(this);
    }
    public void onCreate(Bundle savedInstance) {
        this.mapView.onCreate(savedInstance);
    }

    public void onResume() {
        this.mapView.onResume();
    }
    public void onPause() {
        this.mapView.onPause();
    }

    public void onDestroy() {
        this.mapView.onDestroy();
    }

    public void onLowMemory() {
        this.mapView.onLowMemory();
    }

    public void onSaveInstanceState(Bundle outState) {
        this.mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;

        double latitude = 35.721162;
        double longitude = 139.780134;
        MarkerOptions marker = new MarkerOptions().position(
                new LatLng(latitude, longitude)).title("Hello Maps");

        this.googleMap.addMarker(marker);

    }
}
