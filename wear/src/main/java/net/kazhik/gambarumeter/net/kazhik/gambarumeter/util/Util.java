package net.kazhik.gambarumeter.net.kazhik.gambarumeter.util;

/**
 * Created by kazhik on 14/12/29.
 */
public class Util {
    public static float convertMeter(float distance, String distanceUnit) {
        float result;
        if (distanceUnit.equals("mile")) {
            result = distance / 1609.344f;
        } else if (distanceUnit.equals("metre")) {
            result = distance / 1000f;
        } else {
            result = distance / 1000f;
        }
        return result;
    }
}
