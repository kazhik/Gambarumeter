package net.kazhik.gambarumeter;

import android.app.Fragment;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.SQLException;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataItemBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;

import net.kazhik.gambarumeterlib.storage.DataStorage;
import net.kazhik.gambarumeterlib.storage.WorkoutTable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class MobileGambarumeter extends AppCompatActivity
        implements AdapterView.OnItemClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback<DataItemBuffer>, DataApi.DataListener {
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private ListView mDrawerList;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private GoogleApiClient mGoogleApiClient;
    private static final String TAG = "MobileGambarumeter";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gambarumeter);

        SensorManager sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        List<Sensor> sensorList = sensorManager.getSensorList(Sensor.TYPE_ALL);

        for (Sensor sensor: sensorList) {
            Log.d(TAG, "Sensor:" + sensor.getName() + "; " + sensor.getType());
        }
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, new MainFragment())
                    .commit();

        }
        mTitle = mDrawerTitle = getTitle();
        this.initializeDatabase();
        this.initializeDrawer();

        Intent intent = getIntent();
        Uri dataUri = intent.getData();
        if (dataUri != null) {
            this.importTcxFile(dataUri.getPath());
        }

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Wearable.API)
                .build();


    }
    private void importTcxFile(String filepath) {
        ExternalFile externalFile = new ExternalFile();
        externalFile.importTcxFile(this, filepath);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.gambarumeter, menu);
        return true;
    }
/*
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        return id == R.id.action_settings || super.onOptionsItemSelected(item);
    }
    */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle your other action bar items...

        return super.onOptionsItemSelected(item);
    }

    private void initializeDatabase() {
        try {
            DataStorage storage = new DataStorage(this);
            storage.open();
            storage.close();

        } catch (SQLException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }
    private void initializeDrawer() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.string.drawer_open,  /* "open drawer" description */
                R.string.drawer_close  /* "close drawer" description */
        ) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                setTitle(mTitle);
                clearDrawerItem();
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                setTitle(mDrawerTitle);
                setDrawerItem();
            }
            public void setTitle(CharSequence title) {
                ActionBar actionBar = getSupportActionBar();
                if (actionBar == null) {
                    return;
                }
                actionBar.setTitle(title);
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        // Set the list's click listener
        mDrawerList.setOnItemClickListener(this);

    }
    private void clearDrawerItem() {
        List<Map<String, String>> drawerItems = new ArrayList<>();

        SimpleAdapter drawerAdapter = new SimpleAdapter (this.getBaseContext(),
                drawerItems, R.layout.drawer_list_item,
                new String[] {"text", "icon"},
                new int[] {R.id.drawer_text, R.id.drawer_icon});


        // Set the adapter for the list view
        mDrawerList.setAdapter(drawerAdapter);

    }
    private void setDrawerItem() {
        Fragment fragment = getFragmentManager().findFragmentById(
                        R.id.fragment_container);
        if (fragment instanceof DrawerFragment == false) {
            //return;
        }

        DrawerFragment f = (DrawerFragment)fragment;
        List<Map<String, String>> drawerItems = f.makeDrawerItems();

        SimpleAdapter drawerAdapter = new SimpleAdapter (this.getBaseContext(),
                drawerItems, R.layout.drawer_list_item,
                new String[] {"text", "icon"},
                new int[] {R.id.drawer_text, R.id.drawer_icon});


        // Set the adapter for the list view
        mDrawerList.setAdapter(drawerAdapter);

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }
    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
            mDrawerLayout.closeDrawer(mDrawerList);
            return;
        }
        this.getFragmentManager().popBackStack();
        if(this.getFragmentManager().getBackStackEntryCount() == 0) {
            super.onBackPressed();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.d(TAG, "onNewIntent: " + intent);
//        super.onNewIntent(intent);

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        DrawerFragment f =
                (DrawerFragment)this.getFragmentManager().findFragmentById(
                        R.id.fragment_container);
        Bundle args = f.getArguments();
        long startTime = 0;
        if (args != null) {
            startTime = args.getLong("startTime");

        }
        Log.d(TAG, "startTime: " + startTime);


        Map<String, String> clickedItem;
        //noinspection unchecked
        clickedItem = (Map<String, String>) mDrawerList.getItemAtPosition(position);
        Integer resId = Integer.valueOf(clickedItem.get("id"));
        f.onClickDrawerItem(resId, startTime);
        mDrawerLayout.closeDrawer(mDrawerList);

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


    // GoogleApiClient.ConnectionCallbacks
    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected");
        Wearable.DataApi.addListener(mGoogleApiClient, this);

        PendingResult<DataItemBuffer> results =
                Wearable.DataApi.getDataItems(mGoogleApiClient);
        results.setResultCallback(this);

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
        String dataPath = item.getUri().getPath();
        DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
        switch (dataPath) {
            case "/newdata":
            case "/database":
                Log.d(TAG, "handleDataItem: " + dataPath);
                this.saveData(dataMap);
                break;
            case "/resend":
                Log.d(TAG, "handleDataItem: " + dataPath);
                this.saveData(dataMap);
                break;
            case "/notsynced":
                Log.d(TAG, "handleDataItem: " + dataPath);
                List<History> history = this.checkExists(dataMap);
                this.sendHistory(history);
                break;
            default:
                break;
        }

    }
    private List<History> checkExists(DataMap dataMap) {
        long[] received = dataMap.getLongArray(DataStorage.COL_START_TIME);
        List<History> history = new ArrayList<>();

        if (received == null || received.length == 0) {
            return history;
        }
        WorkoutTable workoutTable = new WorkoutTable(this);
        workoutTable.openReadonly();
        for (long startTime: received) {
            boolean exists = workoutTable.exists(startTime);
            history.add(new History(startTime, exists));
            Log.d(TAG, "startTime: " + startTime + ": exists=" + exists);
        }
        workoutTable.close();

        return history;
    }

    private List<Long> checkUnsaved(DataMap dataMap) {
        long[] unsaved = dataMap.getLongArray(DataStorage.COL_START_TIME);
        List<Long> unknownList = new ArrayList<>();

        WorkoutTable workoutTable = new WorkoutTable(this);
        workoutTable.openReadonly();
        for (long startTime: unsaved) {
            boolean exists = workoutTable.exists(startTime);
            if (!exists) {
                unknownList.add(startTime);
            }
            Log.d(TAG, "startTime: " + startTime + ": exists=" + exists);
        }
        workoutTable.close();

        return unknownList;
    }
    private void sendHistory(List<History> history) {
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create("/synced");

        DataMap dataMap = putDataMapReq.getDataMap();

        for (History data: history) {
            dataMap.putBoolean(String.valueOf(data.getStartTime()), data.exists());
        }

        PendingResult<DataApi.DataItemResult> pendingResult =
                Wearable.DataApi.putDataItem(this.mGoogleApiClient,
                        putDataMapReq.asPutDataRequest());

        pendingResult.setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
            @Override
            public void onResult(DataApi.DataItemResult dataItemResult) {
                Log.d(TAG, "putDataItem done: ");
            }
        });
    }
    private void sendUnknownStartTime(List<Long> unknownList) {

        long[] unknowns = new long[unknownList.size()];
        for (int i = 0; i < unknownList.size(); i++) {
            unknowns[i] = unknownList.get(i);
        }

        PutDataMapRequest putDataMapReq = PutDataMapRequest.create("/unknown");

        DataMap dataMap = putDataMapReq.getDataMap();
        dataMap.putLongArray(DataStorage.COL_START_TIME, unknowns);

        PendingResult<DataApi.DataItemResult> pendingResult =
                Wearable.DataApi.putDataItem(this.mGoogleApiClient,
                        putDataMapReq.asPutDataRequest());

        pendingResult.setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
            @Override
            public void onResult(DataApi.DataItemResult dataItemResult) {
                Log.d(TAG, "putDataItem done: " +
                        dataItemResult.getDataItem().getUri().getPath());
            }
        });
    }
    public void sendConfig(String key, String value) {
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create("/config");

        DataMap dataMap = putDataMapReq.getDataMap();
        dataMap.putString(key, value);

        PendingResult<DataApi.DataItemResult> pendingResult =
                Wearable.DataApi.putDataItem(this.mGoogleApiClient,
                        putDataMapReq.asPutDataRequest());

        pendingResult.setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
            @Override
            public void onResult(DataApi.DataItemResult dataItemResult) {
                Log.d(TAG, "putDataItem done: " + dataItemResult.getDataItem().getUri());
            }
        });

    }

    private void saveData(DataMap dataMap) {

        DataStorage dataStorage = new DataStorage(this);
        dataStorage.save(dataMap);
        dataStorage.close();

    }
}
