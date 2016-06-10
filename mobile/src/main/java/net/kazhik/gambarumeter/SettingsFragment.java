package net.kazhik.gambarumeter;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

/**
 * Created by kazhik on 16/02/17.
 */
public class SettingsFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = "SettingsFragment";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
    }
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d(TAG, "onSharedPreferenceChanged: key=" + key);

        ((MobileGambarumeter)getActivity()).sendConfig(key, sharedPreferences.getString(key, ""));
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        PreferenceManager.getDefaultSharedPreferences(this.getActivity())
                .registerOnSharedPreferenceChangeListener(this);

    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause");
        PreferenceManager.getDefaultSharedPreferences(this.getActivity())
                .unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }
}