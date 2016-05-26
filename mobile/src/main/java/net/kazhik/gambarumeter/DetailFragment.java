package net.kazhik.gambarumeter;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import net.kazhik.gambarumeter.detail.ChartView;
import net.kazhik.gambarumeter.detail.DetailView;
import net.kazhik.gambarumeter.detail.SplitTimeView;
import net.kazhik.gambarumeter.detail.TrackView;
import net.kazhik.gambarumeterlib.entity.SplitTimeStepCount;
import net.kazhik.gambarumeterlib.storage.LocationTable;
import net.kazhik.gambarumeterlib.storage.SplitTimeDataView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by kazhik on 16/02/07.
 */
public class DetailFragment extends Fragment implements NavigationView.OnNavigationItemSelectedListener {
    private long startTime;
    private DetailView detailView;
    private static final String TAG = "DetailFragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.detailView = new TrackView();

    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.track_view, container, false);

        this.detailView.setRootView(view);
        // http://stackoverflow.com/questions/13812988/android-maps-v2-mapview-inside-custom-fragment-npe
        this.detailView.onCreate(savedInstanceState);

        NavigationView navigationView =
                (NavigationView)this.getActivity().findViewById(R.id.navigation);

        navigationView.getMenu().clear();
        navigationView.inflateMenu(R.menu.detail_drawer);

        navigationView.setNavigationItemSelectedListener(this);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Context context = getActivity();
        this.detailView.setContext(context);

        if (this.detailView instanceof TrackView) {
            Bundle b = getArguments();
            if (b != null) {
                this.startTime = b.getLong("startTime");
                if (this.startTime != 0) {
                    TrackView trackView = (TrackView)this.detailView;
                    trackView.load(getLocations(context, this.startTime));
                }
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        this.detailView.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        this.detailView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        this.detailView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.detailView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        this.detailView.onLowMemory();
    }

//    @Override
    public void onClickDrawerItem(int resId, long startTime) {
        switch (resId) {
            case R.string.history:
                this.goBack();
                break;
            case R.string.map:
                this.openMapView(startTime);
                break;
            case R.string.chart:
                this.openChartView(startTime);
                break;
            case R.string.export_file:
                this.exportFile(startTime);
                break;
            case R.string.split_time:
                this.openSplitTimeView(startTime);
                break;
        }

    }
    private void goBack() {
        getFragmentManager().popBackStack();

    }

    private View replaceView(int resId) {
        //this.detailView.onDestroy();

        Activity activity = this.getActivity();
        ViewGroup container =
                (ViewGroup)activity.findViewById(R.id.fragment_container);

        container.removeAllViews();
        return activity.getLayoutInflater().inflate(resId, container);
    }
    private void openMapView(long startTime) {

        DetailFragment detailFragment = new DetailFragment();
        Bundle param = new Bundle();
        param.putLong("startTime", startTime);
        detailFragment.setArguments(param);
        getFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, detailFragment)
                .addToBackStack(null)
                .commit();

        /* This code doesn't work
        Context context = getActivity();
        View view = this.replaceView(R.layout.track_view);

        TrackView trackView = new TrackView();
        trackView.initialize(context, view);

        this.detailView = trackView;
        */
    }
    private List<Location> getLocations(Context context, long startTime) {
        LocationTable locTable = new LocationTable(context);
        locTable.openReadonly();
        List<Location> locations = locTable.selectAll(startTime);
        locTable.close();

        return locations;
    }

    private List<SplitTimeStepCount> getSplits(Context context, long startTime) {
        SplitTimeDataView splitTimeDataView = new SplitTimeDataView(context);
        splitTimeDataView.openReadonly();
        List<SplitTimeStepCount> splits = splitTimeDataView.selectAll(startTime);
        splitTimeDataView.close();

        return splits;
    }
    private void openChartView(long startTime) {
        View view = this.replaceView(R.layout.chart_view);
        Context context = getActivity();

        ChartView chartView = new ChartView();
        chartView.setContext(context);
        chartView.setRootView(view);

        List<SplitTimeStepCount> splits = this.getSplits(context, startTime);
        if (splits.isEmpty()) {
            Toast.makeText(context, context.getString(R.string.nodata),
                    Toast.LENGTH_LONG).show();
            return;
        }
        chartView.load(splits);

        this.detailView = chartView;

    }
    private void openSplitTimeView(long startTime) {
        Context activity = getActivity();
        View view = this.replaceView(R.layout.splittime_view);

        SplitTimeView splitTimeView = new SplitTimeView();
        splitTimeView.setContext(activity);
        splitTimeView.setRootView(view);

        List<SplitTimeStepCount> splits = this.getSplits(activity, startTime);
        if (splits.isEmpty()) {
            Toast.makeText(activity, activity.getString(R.string.nodata),
                    Toast.LENGTH_LONG).show();
            return;
        }
        splitTimeView.load(splits);
        this.detailView = new SplitTimeView();

    }
    private void exportFile(long startTime) {
        if (startTime == 0) {
            return;
        }

        ExternalFile externalFile = new ExternalFile();

        Context context = this.getActivity();

        String filepath = externalFile.exportTcxFile(context, startTime);

        Toast.makeText(context, context.getString(R.string.saved, filepath),
                Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        Log.d(TAG, "onNavigationItemSelected");
        int resId = item.getItemId();
        switch (resId) {
            case R.string.history:
                this.goBack();
                break;
            case R.string.map:
                this.openMapView(this.startTime);
                break;
            case R.string.chart:
                this.openChartView(this.startTime);
                break;
            case R.string.export_file:
                this.exportFile(this.startTime);
                break;
            case R.string.split_time:
                this.openSplitTimeView(this.startTime);
                break;
        }
        return false;
    }
}
