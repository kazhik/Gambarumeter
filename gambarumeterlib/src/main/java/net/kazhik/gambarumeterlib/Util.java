package net.kazhik.gambarumeterlib;

import android.content.res.Resources;

/**
 * Created by kazhik on 14/12/29.
 */
public class Util {

    // meter -> km/mile
    public static float convertMeter(float distance, String distanceUnit) {
        return distance / lapDistance(distanceUnit);
    }
    public static float lapDistance(String distanceUnit) {
        float lapDistance;
        if (distanceUnit.equals("mile")) {
            lapDistance = 1609.344f; //  lap/mile
        } else {
            lapDistance = 1000f;  // lap/km
        }
        return lapDistance;
    }
    public static String distanceUnitDisplayStr(String distanceUnit, Resources res) {
        String displayStr;
        if (distanceUnit.equals("mile")) {
            displayStr = res.getString(R.string.mile);
        } else {
            displayStr = res.getString(R.string.km);
        }
        return displayStr;
    }
    public static String formatLapTime(long lapTime) {

        long hour = lapTime / 60 / 60;
        long min = lapTime / 60 - (hour * 60);
        long sec = lapTime % 60;

        return String.format("%d:%02d:%02d", hour, min, sec);
    }
}
