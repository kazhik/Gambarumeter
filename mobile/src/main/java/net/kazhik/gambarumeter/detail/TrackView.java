package net.kazhik.gambarumeter.detail;

import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import net.kazhik.gambarumeter.R;

import java.util.List;

/**
 * Created by kazhik on 16/02/07.
 */
public class TrackView implements DetailView, OnMapReadyCallback,
        GoogleMap.OnMarkerDragListener,
        GoogleMap.OnMapLongClickListener,
        GoogleMap.OnMapClickListener {
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

        this.googleMap.setOnMapLongClickListener(this);

        this.googleMap.setOnMarkerDragListener(this);

        this.googleMap.setOnMapClickListener(this);

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

    // GoogleMap.OnMarkerDragListener
    @Override
    public void onMarkerDragStart(Marker marker) {
        LatLng latLng = marker.getPosition();
        Log.d(TAG, "onMarkerDragStart " + latLng.latitude + "/" + latLng.longitude);
    }

    // GoogleMap.OnMarkerDragListener
    @Override
    public void onMarkerDrag(Marker marker) {
        LatLng latLng = marker.getPosition();
        Log.d(TAG, "onMarkerDrag " + latLng.latitude + "/" + latLng.longitude);

    }

    // GoogleMap.OnMarkerDragListener
    @Override
    public void onMarkerDragEnd(Marker marker) {
        LatLng latLng = marker.getPosition();
        Log.d(TAG, "onMarkerDragEnd " + latLng.latitude + "/" + latLng.longitude);
    }

    // GoogleMap.OnMapLongClickListener
    @Override
    public void onMapLongClick(LatLng latLng) {
        UiSettings settings = this.googleMap.getUiSettings();
        boolean isEnabled = settings.isScrollGesturesEnabled();
        Log.d(TAG, "isScrollGesturesEnabled: " + isEnabled);
        settings.setScrollGesturesEnabled(!isEnabled);
    }

    // GoogleMap.OnMapClickListener
    @Override
    public void onMapClick(LatLng latLng) {
        Log.d(TAG, "onMapClick " + latLng.latitude + "/" + latLng.longitude);
        Location clicked = new Location(LocationManager.GPS_PROVIDER);
        clicked.setLatitude(latLng.latitude);
        clicked.setLongitude(latLng.longitude);
        Location nearest = null;
        for (Location loc: this.locations) {
            if (nearest == null) {
                nearest = loc;
            } else if (nearest.distanceTo(clicked) > loc.distanceTo(clicked)) {
                nearest = loc;
            }
        }

        LatLng clickedPoint = new LatLng(nearest.getLatitude(), nearest.getLongitude());
        MarkerOptions marker = new MarkerOptions()
                .position(clickedPoint)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_directions_run))
                .draggable(true);
        this.googleMap.addMarker(marker);

    }
}
