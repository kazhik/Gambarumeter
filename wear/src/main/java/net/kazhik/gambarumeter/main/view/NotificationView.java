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
public class NotificationView {
    private Context context;
    private NotificationCompat.Builder notificationBuilder;
    private int heartRate = -1;
    private int stepCount = -1;
    private float distance = -1.0f;
    private long lapTime = 0;
    private String distanceUnit;

    private static final int NOTIFICATION_ID = 3000;

    public void initialize(Context context) {

        this.context = context;

        Intent intent = new Intent(context, Gambarumeter.class);
        PendingIntent pendingIntent
                = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Action openMain
                = new NotificationCompat.Action(R.drawable.empty, null, pendingIntent);

        Bitmap bmp = BitmapFactory.decodeResource(this.context.getResources(), R.drawable.background);

        NotificationCompat.WearableExtender extender = new NotificationCompat.WearableExtender()
                .setHintHideIcon(true)
                .setBackground(bmp)
//                .setDisplayIntent(pendingIntent)
//                .setCustomSizePreset(NotificationCompat.WearableExtender.SIZE_LARGE)
                .setContentAction(0)
                .addAction(openMain);

        this.notificationBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_launcher)
                .extend(extender)
                .setOnlyAlertOnce(true)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setOngoing(true);

    }
    public void clear() {
        this.heartRate = -1;
        this.stepCount = 0;
        this.distance = -1.0f;
    }
    public NotificationView setDistanceUnit(String distanceUnit) {
        this.distanceUnit = distanceUnit;

        return this;
    }
    public void updateDistance(float distance) {
        this.distance = distance;
    }
    public void updateLap(long laptime) {
        this.lapTime = laptime;
        
    }
    public void updateHeartRate(int heartRate) {

        this.heartRate = heartRate;
    }
    public void updateStepCount(int stepCount) {
        this.stepCount = stepCount;
    }
    private String makeDetailedText() {
        String str = "";

        if (this.stepCount > 0) {
            str += this.stepCount + this.context.getString(R.string.steps);
        }

        return str;
    }
    private String makeSummaryText(long elapsed) {
        String str = DateUtils.formatElapsedTime(elapsed / 1000);

        str += this.makeSensorDataText();

        return str;
    }
    private String makeSensorDataText() {
        String str = "";
        if (this.heartRate > 0) {
            str += "/";
            str += this.heartRate + this.context.getString(R.string.bpm);
        }
        if (this.distance > 0) {
            String distanceUnitStr =
                    Util.distanceUnitDisplayStr(this.distanceUnit,
                            this.context.getResources());
            float distance = Util.convertMeter(this.distance, this.distanceUnit);

            str += "/";
            str += String.format("%.2f%s", distance, distanceUnitStr);
            str += "/";
            str += DateUtils.formatElapsedTime(this.lapTime / 1000);
        }
        return str;
    }
    public void show(long elapsed) {

        this.notificationBuilder
                .setContentTitle(this.makeSummaryText(elapsed))
                .setContentText(this.makeDetailedText());

        NotificationManagerCompat.from(this.context)
                .notify(NOTIFICATION_ID, this.notificationBuilder.build());

    }
    public void dismiss() {
        NotificationManagerCompat.from(this.context).cancel(NOTIFICATION_ID);
    }

}
