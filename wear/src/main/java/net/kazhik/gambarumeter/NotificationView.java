package net.kazhik.gambarumeter;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.text.format.DateUtils;
import android.util.Log;

import net.kazhik.gambarumeter.monitor.Stopwatch;

import java.util.List;

/**
 * Created by kazhik on 14/10/25.
 */
public class NotificationView implements Stopwatch.OnTickListener {
    private Context context;
    private NotificationCompat.Builder notificationBuilder;
    private int heartRate = 0;
    private int stepCount = 0;
    private Stopwatch stopwatch;

    private static final String TAG = "NotificationView";

    public void initialize(Context context) {

        this.context = context;

        this.stopwatch = new Stopwatch(1000L, this);

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
//                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_launcher)
                .extend(extender)
                .setOnlyAlertOnce(true)
                .setPriority(NotificationCompat.PRIORITY_MAX);

    }
    public void start() {
        this.stopwatch.start();

    }
    public void stop() {
        this.stopwatch.stop();
    }
    public void updateHeartRate(int heartRate) {

        this.heartRate = heartRate;
    }
    public void updateStepCount(int stepCount) {
        this.stepCount = stepCount;
    }
    private String makeText(long elapsed) {
        StringBuffer strBuff = new StringBuffer();

        strBuff.append(DateUtils.formatElapsedTime(elapsed / 1000))
                .append("/")
                .append(this.heartRate)
                .append(this.context.getString(R.string.bpm))
                .append("/")
                .append(this.stepCount)
                .append(this.context.getString(R.string.steps))
                ;

        return strBuff.toString();
    }

    @Override
    public void onTick(long elapsed) {
//        this.notificationBuilder.setContentText(this.makeText(elapsed));
        this.notificationBuilder.setStyle(
                new NotificationCompat.BigTextStyle().bigText(this.makeText(elapsed)));

        NotificationManagerCompat.from(context).notify(3000, this.notificationBuilder.build());

    }
}
