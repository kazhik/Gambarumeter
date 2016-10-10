package net.kazhik.gambarumeter;

import android.app.Fragment;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.SQLException;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import net.kazhik.gambarumeterlib.storage.DataStorage;

import java.util.List;


public class MobileGambarumeter extends AppCompatActivity
        implements android.app.FragmentManager.OnBackStackChangedListener {
    private DrawerLayout mDrawerLayout;
    public ActionBarDrawerToggle mDrawerToggle;
    private WearConnector mWearConnector = new WearConnector();
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
        this.initializeDatabase();
        this.initializeDrawer();
        this.mWearConnector.initialize(this);

        Intent intent = getIntent();
        Uri dataUri = intent.getData();
        if (dataUri != null) {
            this.importTcxFile(dataUri.getPath());
        }
    }

    private void importTcxFile(String filepath) {
        ExternalFile externalFile = new ExternalFile();
        externalFile.importTcxFile(this, filepath);

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
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                toolbar,
                R.string.drawer_open,
                R.string.drawer_close){
        };

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment f = getFragmentManager().findFragmentById(R.id.fragment_container);
                if (f instanceof SettingsFragment) {
                    onBackPressed();
                    return;
                }
                if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                    mDrawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    mDrawerLayout.openDrawer(GravityCompat.START);
                }
            }
        });

        //Setting the actionbarToggle to drawer layout
        mDrawerLayout.addDrawerListener(mDrawerToggle);

        getFragmentManager().addOnBackStackChangedListener(this);
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
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            this.getFragmentManager().popBackStack();
            if(this.getFragmentManager().getBackStackEntryCount() == 0) {
                super.onBackPressed();
            }
        }
    }
    @Override
    public void onBackStackChanged() {
        boolean showHomeAsUp = false;
        Fragment f = getFragmentManager().findFragmentById(R.id.fragment_container);
        if (f instanceof SettingsFragment) {
            Log.d(TAG, "SettingsFragment");
            showHomeAsUp = true;
        } else if (f instanceof DetailFragment) {
            Log.d(TAG, "DetailFragment");
        } else if (f instanceof MainFragment) {
            Log.d(TAG, "MainFragment");
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(showHomeAsUp);
        }
        if (showHomeAsUp) {
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        } else {
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            mDrawerToggle.syncState();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        this.mWearConnector.connect();
    }

    @Override
    public void onStop() {
        this.mWearConnector.disconnect();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        this.mWearConnector.terminate();
        super.onDestroy();
    }
    public void sendConfig(String key, String value) {
        this.mWearConnector.sendConfig(key, value);
    }

}
