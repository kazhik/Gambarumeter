package net.kazhik.gambarumeterlib;

import android.content.Context;
import android.text.format.DateUtils;
import android.util.Log;

import java.util.Locale;

/**
 * Created by kazhik on 14/12/29.
 */
public class TimeUtil {

    public static String formatSec(long lapTime) {

        long hour = lapTime / 60 / 60;
        long min = lapTime / 60 - (hour * 60);
        long sec = lapTime % 60;

        return String.format(Locale.getDefault(), "%d:%02d:%02d", hour, min, sec);
    }
    public static String formatMsec(long splitTime) {
        long splitTimeSec = splitTime / 1000;
        return formatSec(splitTimeSec);
    }
    public static String formatDateTime(Context context, long millis) {
        int flag = DateUtils.FORMAT_SHOW_YEAR |
                DateUtils.FORMAT_SHOW_DATE |
                DateUtils.FORMAT_SHOW_TIME;

        return DateUtils.formatDateTime(context, millis, flag);
    }

}
