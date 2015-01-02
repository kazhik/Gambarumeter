package net.kazhik.gambarumeter.net.kazhik.gambarumeter.util;

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
}
