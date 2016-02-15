package net.kazhik.gambarumeterlib;

/**
 * Created by kazhik on 14/12/29.
 */
public class TimeUtil {

    public static String formatSec(long lapTime) {

        long hour = lapTime / 60 / 60;
        long min = lapTime / 60 - (hour * 60);
        long sec = lapTime % 60;

        return String.format("%d:%02d:%02d", hour, min, sec);
    }
    public static String formatMsec(long splitTime) {
        long splitTimeSec = splitTime / 1000;
        return formatSec(splitTimeSec);
    }

}
