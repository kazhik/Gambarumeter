package net.kazhik.gambarumeter.main.view;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.text.format.DateUtils;

import net.kazhik.gambarumeter.Gambarumeter;
import net.kazhik.gambarumeter.R;
import net.kazhik.gambarumeter.Util;

/**
 * Created by kazhik on 14/10/25.
 */
public class LocationNotificationView extends NotificationView {
    private float distance = -1.0f;
    private long lapTime = 0;
    private String distanceUnit;

    public void clear() {
        super.clear();
        this.distance = -1.0f;
    }
    public LocationNotificationView setDistanceUnit(String distanceUnit) {
        this.distanceUnit = distanceUnit;

        return this;
    }
    public void updateDistance(float distance) {
        this.distance = distance;
    }
    public void updateLap(long laptime) {
        this.lapTime = laptime;
        
    }
    public String makeShortText() {
        String str = "";
        if (this.distance > 0) {
            String distanceUnitStr =
                    Util.distanceUnitDisplayStr(this.distanceUnit,
                            this.getContext().getResources());
            float distance = Util.convertMeter(this.distance, this.distanceUnit);

            str += "/";
            str += String.format("%.2f%s", distance, distanceUnitStr);
        }
        return str;
    }
    public String makeLongText(String str) {
        if (this.lapTime > 0) {
            str += DateUtils.formatElapsedTime(this.lapTime / 1000);
        }
        return str;

    }

}
