package net.kazhik.gambarumeterlib;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.Locale;

import static android.R.attr.key;

/**
 * Created by kazhik on 16/02/12.
 */
public class DistanceUtil implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static DistanceUtil distanceUtil;
    private String distanceUnitPref;
    private String distanceUnitStr;
    private String kmStr;
    private String mileStr;
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

        this.kmStr = context.getString(R.string.km);
        this.mileStr = context.getString(R.string.mile);

        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(context);
        prefs.registerOnSharedPreferenceChangeListener(this);

        this.distanceUnitPref = prefs.getString("distanceUnit", "metre");
        this.distanceUnitStr = (distanceUnitPref.equals("mile"))?
                this.mileStr: this.kmStr;
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
        return getDistanceStr(distance) + this.distanceUnitStr;
    }
    public String getDistanceStr(float distance) {
        distance = this.convertMeter(distance);
        return String.format(Locale.getDefault(), "%.2f", distance);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPrefs, String key) {
        String value = sharedPrefs.getString(key, "");
        Log.d(TAG, "onSharedPreferenceChanged: " + key + "=" + value);
        if (key.equals("distanceUnit")) {
            this.distanceUnitPref = sharedPrefs.getString("distanceUnit", "metre");
            this.distanceUnitStr = (distanceUnitPref.equals("mile"))?
                    this.mileStr: this.kmStr;
        }

    }
}
