package net.kazhik.gambarumeterlib;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;

/**
 * Created by kazhik on 16/02/12.
 */
public class DistanceUtil {
    private static DistanceUtil distanceUtil;
    private String distanceUnitPref;
    private String distanceUnitStr;
    public static DistanceUtil getInstance(Context context) {
        if (distanceUtil == null) {
            distanceUtil = new DistanceUtil();
            SharedPreferences prefs =
                    PreferenceManager.getDefaultSharedPreferences(context);
            String prefStr = prefs.getString("distanceUnit", "metre");
            distanceUtil.distanceUnitPref = prefStr;
            int resId = prefStr.equals("mile")? R.string.mile: R.string.km;
            distanceUtil.distanceUnitStr = context.getString(resId);
        }
        return distanceUtil;
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

}
