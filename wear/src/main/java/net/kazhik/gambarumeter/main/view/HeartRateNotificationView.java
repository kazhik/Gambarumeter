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
import net.kazhik.gambarumeter.main.view.NotificationView;

/**
 * Created by kazhik on 14/10/25.
 */
public class HeartRateNotificationView extends NotificationView {
    private int heartRate = -1;

    public void clear() {
        super.clear();
        this.heartRate = -1;
    }
    public void updateHeartRate(int heartRate) {

        this.heartRate = heartRate;
    }
    public String makeShortText() {
        String str = "";
        if (this.heartRate > 0) {
            str += "/";
            str += this.heartRate + this.getContext().getString(R.string.bpm);
        }
        return str;
    }
    public String makeLongText(String str) {
        
        return str;
        
    }

}
