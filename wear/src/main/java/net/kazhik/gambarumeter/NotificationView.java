package net.kazhik.gambarumeter;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.text.format.DateUtils;

/**
 * Created by kazhik on 14/10/25.
 */
public class NotificationView {
    private Context context;
    private NotificationCompat.Builder notificationBuilder;
    private int heartRate = 0;
    private int stepCount = 0;

    private static final String TAG = "NotificationView";

    public void initialize(Context context) {

        this.context = context;

        Intent intent = new Intent(context, Gambarumeter.class);
        PendingIntent pendingIntent
                = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Action openMain
                = new NotificationCompat.Action(R.drawable.empty, null, pendingIntent);

        NotificationCompat.WearableExtender extender = new NotificationCompat.WearableExtender()
                .setHintHideIcon(true)
                .setContentAction(0)
                .addAction(openMain);

        this.notificationBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_launcher)
                .extend(extender)
                .setOnlyAlertOnce(true)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setOngoing(true);

    }
    public void updateHeartRate(int heartRate) {

        this.heartRate = heartRate;
    }
    public void updateStepCount(int stepCount) {
        this.stepCount = stepCount;
    }
    private String makeText(long elapsed) {

        return DateUtils.formatElapsedTime(elapsed / 1000)
                + "/"
                + this.heartRate
                + this.context.getString(R.string.bpm)
                + "/"
                + this.stepCount
                + this.context.getString(R.string.steps);
    }
    public void updateTime(long elapsed) {
        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
        bigTextStyle.bigText(this.makeText(elapsed));
        this.notificationBuilder.setStyle(bigTextStyle);

        NotificationManagerCompat.from(context).notify(3000, this.notificationBuilder.build());

    }

}
