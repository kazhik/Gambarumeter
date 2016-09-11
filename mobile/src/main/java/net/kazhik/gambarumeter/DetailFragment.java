package net.kazhik.gambarumeter;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
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

import java.util.List;

/**
 * Created by kazhik on 16/02/07.
 */
public class DetailFragment extends Fragment
        implements NavigationView.OnNavigationItemSelectedListener {
    private long startTime = 0;
    private DetailView detailView;
    private static final int TYPE_MAP = 1;
    private static final int TYPE_CHART = 2;
    private static final int TYPE_SPLITTIME = 3;
    private static final String TAG = "DetailFragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int type = TYPE_MAP;
        Bundle b = getArguments();
        if (b != null) {
            type = b.getInt("type", TYPE_MAP);
            this.startTime = b.getLong("startTime", 0);
        }
        switch (type) {
            case TYPE_CHART:
                this.detailView = new ChartView();
                break;
            case TYPE_SPLITTIME:
                this.detailView = new SplitTimeView();
                break;
            case TYPE_MAP:
            default:
                this.detailView = new TrackView();
                break;

        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view;
        if (this.detailView instanceof ChartView) {
            view = inflater.inflate(R.layout.chart_view, container, false);
        } else if (this.detailView instanceof SplitTimeView) {
            view = inflater.inflate(R.layout.splittime_view, container, false);
        } else {
            view = inflater.inflate(R.layout.track_view, container, false);
        }

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
            TrackView trackView = (TrackView)this.detailView;
            trackView.load(getLocations(context, this.startTime));
        } else if (this.detailView instanceof ChartView ||
                this.detailView instanceof SplitTimeView) {
            List<SplitTimeStepCount> splits = this.getSplits(context, startTime);
            if (splits.isEmpty()) {
                Toast.makeText(context, R.string.nodata,
                        Toast.LENGTH_LONG).show();
                return;
            }
            if (this.detailView instanceof ChartView) {
                ((ChartView)this.detailView).load(splits);
            } else {
                ((SplitTimeView)this.detailView).load(splits);
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

    private void goBack() {
        FragmentManager fmgr = getFragmentManager();

        for (int i = 0; i < fmgr.getBackStackEntryCount(); i++) {
            fmgr.popBackStack();
        }

    }

    private View replaceView(int resId) {
        //this.detailView.onDestroy();

        Activity activity = this.getActivity();
        ViewGroup container =
                (ViewGroup)activity.findViewById(R.id.fragment_container);

        container.removeAllViews();
        return activity.getLayoutInflater().inflate(resId, container);
    }
    private void openDetailView(int type, long startTime) {
        DetailFragment detailFragment = new DetailFragment();
        Bundle param = new Bundle();
        param.putLong("startTime", startTime);
        param.putInt("type", type);
        detailFragment.setArguments(param);
        getFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, detailFragment)
                .addToBackStack(null)
                .commit();

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
        DrawerLayout drawerLayout =
                (DrawerLayout) this.getActivity().findViewById(R.id.drawer_layout);
        drawerLayout.closeDrawers();
        switch (item.getItemId()) {
            case R.id.action_history:
                this.goBack();
                break;
            case R.id.action_map:
                this.openDetailView(TYPE_MAP, this.startTime);
                break;
            case R.id.action_chart:
                this.openDetailView(TYPE_CHART, this.startTime);
                break;
            case R.id.action_splittime:
                this.openDetailView(TYPE_SPLITTIME, this.startTime);
                break;
            case R.id.action_transform:
                this.exportFile(this.startTime);
                break;
            default:
                break;
        }
        return false;
    }
}
