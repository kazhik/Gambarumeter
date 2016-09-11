package net.kazhik.gambarumeterlib;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Created by kazhik on 16/02/12.
 */
public class DistanceUtil implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static DistanceUtil distanceUtil;
    private Context context;
    private String distanceUnitPref;
    private String distanceUnitStr;
    private static final Object lock = new Object();
    private static final String TAG = "DistanceUtil";

    public static DistanceUtil getInstance(Context context) {
        synchronized (lock) {
            if (distanceUtil == null) {
                distanceUtil = new DistanceUtil();
                distanceUtil.initialize(context);
            }
        }
        return distanceUtil;
    }
    public void initialize(Context context) {
        this.context = context;

        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(context);
        prefs.registerOnSharedPreferenceChangeListener(this);

        String prefStr = prefs.getString("distanceUnit", "metre");

        this.refreshPrefs(context, prefStr);
    }
    private void refreshPrefs(Context context, String distanceUnitPref) {
        this.distanceUnitPref = distanceUnitPref;
        int resId = distanceUnitPref.equals("mile")? R.string.mile: R.string.km;
        this.distanceUnitStr = context.getString(resId);

    }

    public String getUnitStr() {
        return this.distanceUnitStr;
    }
    // meter -> km/mile
    public float convertMeter(float distance) {
        return distance / lapDistance();
    }
    public float lapDistance() {
        float lapDistance;
        if (distanceUnitPref.equals("mile")) {
            lapDistance = 1609.344f; //  lap/mile
        } else {
            lapDistance = 1000f;  // lap/km
        }
        return lapDistance;
    }
    public String getDistanceAndUnitStr(float distance) {
        distance = this.convertMeter(distance);
        return String.format("%.2f%s", distance, this.distanceUnitStr);
    }
    public String getDistanceStr(float distance) {
        distance = this.convertMeter(distance);
        return String.format("%.2f", distance);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPrefs, String key) {
        String value = sharedPrefs.getString(key, "");
        Log.d(TAG, "onSharedPreferenceChanged: " + key + "=" + value);
        if (key.equals("distanceUnit")) {
            this.refreshPrefs(this.context, value);
        }

    }
}
