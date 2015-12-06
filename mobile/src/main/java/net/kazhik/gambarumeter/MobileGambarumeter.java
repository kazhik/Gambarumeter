package net.kazhik.gambarumeter;

import android.app.Activity;
import android.database.SQLException;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import net.kazhik.gambarumeterlib.storage.DataStorage;

import java.util.List;


public class MobileGambarumeter extends Activity {
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

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.gambarumeter, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        return id == R.id.action_settings || super.onOptionsItemSelected(item);
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

}
