package net.kazhik.gambarumeter;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.SQLException;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import net.kazhik.gambarumeterlib.storage.DataStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MobileGambarumeter extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private ListView mDrawerList;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
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
                getSupportActionBar().setTitle(mTitle);
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getSupportActionBar().setTitle(mDrawerTitle);
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        List<Map<String, String>> drawerItems =
                new ArrayList<Map<String, String>>();

        Map<String, String> drawerItem;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String keyword = prefs.getString("Keyword", "");

        drawerItem = new HashMap<String, String>();
        drawerItem.put("id", String.valueOf(R.string.mobile_settings));
        drawerItem.put("text", getString(R.string.mobile_settings));
        drawerItem.put("icon", String.valueOf(R.drawable.settings));
        drawerItems.add(drawerItem);

        drawerItem = new HashMap<String, String>();
        drawerItem.put("id", String.valueOf(R.string.wear_settings));
        drawerItem.put("text", getString(R.string.wear_settings));
        drawerItem.put("icon", String.valueOf(R.drawable.settings));
        drawerItems.add(drawerItem);

        SimpleAdapter drawerAdapter = new SimpleAdapter (this.getBaseContext(),
                drawerItems, R.layout.drawer_list_item,
                new String[] {"text", "icon"},
                new int[] {R.id.drawer_text, R.id.drawer_icon});


        // Set the adapter for the list view
        mDrawerList.setAdapter(drawerAdapter);
        // Set the list's click listener
        mDrawerList.setOnItemClickListener(this);

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
        this.getFragmentManager().popBackStack();
        if(this.getFragmentManager().getBackStackEntryCount() == 0) {
            super.onBackPressed();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }
}
