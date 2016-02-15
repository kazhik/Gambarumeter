package net.kazhik.gambarumeter.detail;

import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import net.kazhik.gambarumeter.R;
import net.kazhik.gambarumeter.detail.DetailView;

import java.util.List;

/**
 * Created by kazhik on 16/02/07.
 */
public class TrackView implements DetailView, OnMapReadyCallback {
    private Context context;
    private MapView mapView;
    private GoogleMap googleMap;
    private List<Location> locations;
    private static final String TAG = "TrackView";

    @Override
    public void setContext(Context context) {
        this.context = context;
    }
    @Override
    public void setRootView(View root) {
        this.mapView = (MapView) root.findViewById(R.id.map);

        this.mapView.getMapAsync(this);

    }
    @Override
    public void onCreate(Bundle savedInstance) {
        if (this.mapView != null) {
            this.mapView.onCreate(savedInstance);
        }
    }

    @Override
    public void onResume() {
        this.mapView.onResume();
    }

    @Override
    public void onPause() {
        this.mapView.onPause();
    }

    @Override
    public void onDestroy() {
        this.mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        this.mapView.onLowMemory();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        this.mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;

        if (this.locations != null) {
            this.drawTracks();
        }


    }
    public void load(List<Location> locations) {
        this.locations = locations;
        if (this.googleMap != null) {
            this.drawTracks();
        }
    }
    private PolylineOptions getPolylineOptions() {
        return new PolylineOptions()
                .width(5)
                .color(Color.BLUE)
                .geodesic(true);

    }
    private LatLng convertLocation(Location loc) {
        return new LatLng(loc.getLatitude(), loc.getLongitude());
    }
    private void drawTracks() {
        if (this.googleMap == null) {
            return;
        }
        if (this.locations == null || this.locations.isEmpty()) {
            return;
        }
        // polyline
        PolylineOptions options = this.getPolylineOptions();
        for (Location loc: this.locations) {
            LatLng latLng = this.convertLocation(loc);
            options.add(latLng);
        }
        this.googleMap.addPolyline(options);

        LatLng startPoint = this.convertLocation(this.locations.get(0));

        // marker
        MarkerOptions marker = new MarkerOptions().position(startPoint);
        this.googleMap.addMarker(marker);

        // camera
        CameraPosition camPos = CameraPosition.fromLatLngZoom(startPoint, 15);
        CameraUpdate camUpdate = CameraUpdateFactory.newCameraPosition(camPos);
        this.googleMap.moveCamera(camUpdate);

    }
}
