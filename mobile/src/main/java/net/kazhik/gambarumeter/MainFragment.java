package net.kazhik.gambarumeter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.CapabilityApi;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataItemBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import net.kazhik.gambarumeterlib.DistanceUtil;
import net.kazhik.gambarumeterlib.TimeUtil;
import net.kazhik.gambarumeterlib.entity.WorkoutInfo;
import net.kazhik.gambarumeterlib.storage.DataStorage;
import net.kazhik.gambarumeterlib.storage.WorkoutTable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by kazhik on 15/10/14.
 */
public class MainFragment extends DrawerFragment
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        DataApi.DataListener,
        ResultCallback<DataItemBuffer>,
        CapabilityApi.CapabilityListener {

    private GoogleApiClient mGoogleApiClient;
    private SimpleAdapter listAdapter;
    private ArrayList<HashMap<String, String>> mapList = new ArrayList<>();
    private DistanceUtil distanceUtil;

    private static final int CONTEXTMENU_DELETE = 1001;

    private static final String TAG = "MainFragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate");

        Context context = this.getActivity();

        this.distanceUtil = DistanceUtil.getInstance(context);

        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Wearable.API)
                .build();

    }
    @Override
    public void onStart() {
        super.onStart();
        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onStop() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            Wearable.DataApi.removeListener(mGoogleApiClient, this);
            Wearable.CapabilityApi
                    .removeCapabilityListener(mGoogleApiClient, this, getString(R.string.app_name));
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public void onDestroy() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.unregisterConnectionCallbacks(this);
        }
        super.onDestroy();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.main_fragment, container, false);
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        this.initializeUI();

        this.loadList();
    }

    private void initializeUI() {
        Activity activity = this.getActivity();

        ListView lv = (ListView) activity.findViewById(R.id.history);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent,
                                    View view,
                                    int position,
                                    long id) {

                ListView listView = (ListView) parent;
                Map<String, String> map = (Map<String, String>) listView
                        .getItemAtPosition(position);

                Log.d(TAG, "startTime: " + map.get("startTime")
                        + ":" + map.get("startTimeStr"));

                /*
                ChartFragment chartFragment = new ChartFragment();
                Bundle chartParam = new Bundle();
                chartParam.putLong("startTime", Long.valueOf(map.get("startTime")));
                chartFragment.setArguments(chartParam);

                getFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, chartFragment)
                        .addToBackStack(null)
                        .commit();
                */

                DetailFragment detailFragment = new DetailFragment();
                Bundle param = new Bundle();
                param.putLong("startTime", Long.valueOf(map.get("startTime")));
                detailFragment.setArguments(param);
                getFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, detailFragment)
                        .addToBackStack(null)
                        .commit();


            }
        });
        registerForContextMenu(lv);

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        AdapterView.AdapterContextMenuInfo info =
                (AdapterView.AdapterContextMenuInfo) menuInfo;

        HashMap<String, String> map = this.mapList.get(info.position);

        menu.setHeaderTitle(map.get("startTimeStr"));
        menu.add(Menu.NONE, CONTEXTMENU_DELETE, Menu.NONE,
                R.string.delete);

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        class ConfirmDeleteListener implements
                DialogInterface.OnClickListener {
            private int position;

            public ConfirmDeleteListener(int position) {
                this.position = position;
            }

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    MainFragment.this.deleteTransaction(this.position);
                }

            }
        }
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
                .getMenuInfo();
        if (item.getItemId() == CONTEXTMENU_DELETE) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
            // Add the buttons
            builder.setPositiveButton(android.R.string.ok,
                    new ConfirmDeleteListener(info.position));
            builder.setNegativeButton(android.R.string.cancel,
                    new ConfirmDeleteListener(info.position));
            builder.setTitle(R.string.confirm_delete);
            AlertDialog dialog = builder.create();
            dialog.show();

        }
        return true;
    }
    public void deleteTransaction(int position) {

        HashMap<String, String> removed = this.mapList.remove(position);
        this.listAdapter.notifyDataSetChanged();

        long startTime = Long.parseLong(removed.get("startTime"));

        Context context = this.getActivity();

        DataStorage storage = new DataStorage(context);
        storage.delete(startTime);
        storage.close();

    }
    // GoogleApiClient.ConnectionCallbacks
    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected");
        Wearable.DataApi.addListener(mGoogleApiClient, this);

        PendingResult<DataItemBuffer> results =
                Wearable.DataApi.getDataItems(mGoogleApiClient);
        results.setResultCallback(this);

        Wearable.CapabilityApi
                .addCapabilityListener(mGoogleApiClient, this,
                        getString(R.string.app_name));
        checkConnection();
    }

    // GoogleApiClient.ConnectionCallbacks
    @Override
    public void onConnectionSuspended(int i) {

    }

    // GoogleApiClient.OnConnectionFailedListener
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    // DataApi.DataListener
    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Log.d(TAG, "onDataChanged");
        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                this.handleDataItem(event.getDataItem());
            }
        }

    }

    // ResultCallback<DataItemBuffer>
    @Override
    public void onResult(DataItemBuffer dataItems) {
        Log.d(TAG, "onResult");
        for (DataItem dataItem : dataItems) {
            this.handleDataItem(dataItem);
        }
        dataItems.release();

    }
    private void handleDataItem(DataItem item) {
        if (item.getUri().getPath().equals("/database")) {
            DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
            Log.d(TAG, "database: " + dataMap.toString());
            this.saveData(dataMap);
        }

    }
    private void saveData(DataMap dataMap) {

        DataStorage dataStorage = new DataStorage(this.getActivity());
        dataStorage.save(dataMap);
        dataStorage.close();

    }
    private void loadList() {
        Activity activity = this.getActivity();

        WorkoutTable workoutTable = new WorkoutTable(activity);
        workoutTable.openReadonly();
        List<WorkoutInfo> workoutList = workoutTable.selectAll();
        workoutTable.close();

        Log.d(TAG, "WorkoutList size=" + workoutList.size());
        this.mapList.clear();
        for (WorkoutInfo workout: workoutList) {

            HashMap<String, String> map = new HashMap<>();
            long startTime = workout.getStartTime();
            map.put("startTime", String.valueOf(startTime));

            int flag = DateUtils.FORMAT_SHOW_YEAR |
                    DateUtils.FORMAT_SHOW_DATE |
                    DateUtils.FORMAT_SHOW_TIME;
            map.put("startTimeStr",
                    DateUtils.formatDateTime(activity,
                            startTime,
                            flag)
            );

            long stopTime = workout.getStopTime();
            int stepCount = workout.getStepCount();
            float distance = workout.getDistance();
            int heartRate = workout.getHeartRate();

            String resultStr = TimeUtil.formatMsec(stopTime - startTime);
            if (distance > 0.0f) {
                resultStr += "/" + this.distanceUtil.getDistanceAndUnitStr(distance);
            } else if (heartRate > 0) {
                String heartRateStr = String.format("%d%s", heartRate,
                        activity.getResources().getString(R.string.bpm));
                resultStr += "/" + heartRateStr;
            }
            String stepCountStr = stepCount +
                    activity.getResources().getString(R.string.steps);
            resultStr += "/" + stepCountStr;

            map.put("result", resultStr);
            Log.d(TAG, "startTime: " + map.get("startTime") + ":" + map.get("startTimeStr"));
            this.mapList.add(map);

        }

        this.listAdapter = new SimpleAdapter(activity,
                this.mapList,
                R.layout.history_item,
                new String[] { "startTimeStr", "result" },
                new int[] { R.id.start_time, R.id.result }
        );
        ListView lv = (ListView)activity.findViewById(R.id.history);
        lv.setAdapter(this.listAdapter);
        lv.setSelection(0);
        this.listAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClickDrawerItem(int resId) {

    }
    @Override
    public void onClickDrawerItem(int resId, long startTime) {

    }
    public List<Map<String, String>> makeDrawerItems() {
        List<Map<String, String>> drawerItems = new ArrayList<>();

        Map<String, String> drawerItem;

        drawerItem = makeDrawerItem(R.string.mobile_settings,
                R.drawable.settings);
        drawerItems.add(drawerItem);

        drawerItem = makeDrawerItem(R.string.wear_settings,
                R.drawable.settings);
        drawerItems.add(drawerItem);

        return drawerItems;
    }
    private void checkConnection() {
        AsyncTask.execute(new Runnable() {

            @Override
            public void run() {
                CapabilityApi.GetCapabilityResult result =
                        Wearable.CapabilityApi.getCapability(mGoogleApiClient,
                                getString(R.string.app_name), CapabilityApi.FILTER_ALL)
                                .await();

                Log.d(TAG, "check connection result: " +
                        result.getCapability().getNodes().size());
                onCapabilityChanged(result.getCapability());

            }
        });
/*
        Wearable.CapabilityApi.getCapability(mGoogleApiClient, getString(R.string.app_name),
                CapabilityApi.FILTER_ALL)
                .setResultCallback(new ResultCallback<CapabilityApi.GetCapabilityResult>() {
                    @Override
                    public void onResult(CapabilityApi.GetCapabilityResult result) {
                        if (!result.getStatus().isSuccess()) {
                            Log.e(TAG, "Failed to get capabilities, "
                                    + "status: " + result.getStatus().getStatusMessage());
                            return;
                        }
                        Log.d(TAG, "check result");
                        onCapabilityChanged(result.getCapability());
                    }
                });
        */
    }

    // CapabilityApi.CapabilityListener
    @Override
    public void onCapabilityChanged(CapabilityInfo capabilityInfo) {
        Set<Node> connectedNodes = capabilityInfo.getNodes();
        Log.d(TAG, "onCapabilityChanged:" + connectedNodes.size());
        for (Node node : connectedNodes) {
            // we are only considering those nodes that are directly connected
            if (node.isNearby()) {
                Log.d(TAG, "onCapabilityChanged:" + node.getDisplayName());

            }
        }
    }
}
