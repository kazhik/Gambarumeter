package net.kazhik.gambarumeter.main.notification;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.text.Html;
import android.text.format.DateUtils;

import net.kazhik.gambarumeter.WearGambarumeter;
import net.kazhik.gambarumeter.R;

/**
 * Created by kazhik on 14/10/25.
 */
public abstract class NotificationView {
    private Context context;
    private NotificationCompat.Builder notificationBuilder;
    private NotificationManagerCompat notificationMgr = null;
    private int stepCount = -1;

    private static final int NOTIFICATION_ID = 3000;
    private static final String TAG = "NotificationView";

    public void initialize(Context context) {

        this.context = context;

        this.buildNotification();

    }

    private void buildNotification() {
        Intent intent = new Intent(context, WearGambarumeter.class);
        int flag = PendingIntent.FLAG_UPDATE_CURRENT;
        PendingIntent pendingIntent =
                PendingIntent.getActivity(context, 0, intent, flag);
        Bitmap bmp = BitmapFactory.decodeResource(this.context.getResources(),
                R.drawable.notification);
        NotificationCompat.Action openMain
                = new NotificationCompat.Action(R.drawable.empty, null, pendingIntent);

        NotificationCompat.WearableExtender extender = new NotificationCompat.WearableExtender()
//                .setDisplayIntent(pendingIntent)
                .setContentAction(0)
                .addAction(openMain)
                .setHintHideIcon(true)
                .setBackground(bmp);

        this.notificationBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_launcher)
                .setOnlyAlertOnce(true)
                .setPriority(Notification.PRIORITY_MAX)
                .setOngoing(true)
                .setContentIntent(pendingIntent)
                .extend(extender);

        this.notificationMgr = NotificationManagerCompat.from(this.context);
    }

    public Context getContext() {
        return this.context;
        
    }
    public void clear() {
        this.stepCount = 0;
    }
    public void updateStepCount(int stepCount) {
        this.stepCount = stepCount;
    }
    private String makeDetailedText() {
        String str = "";

        if (this.stepCount >= 0) {
            str += this.stepCount + this.context.getString(R.string.steps);
        }
        str = this.makeLongText(str);

        return str;
    }
    private String makeSummaryText(long elapsed) {
        String str = DateUtils.formatElapsedTime(elapsed / 1000);

        str += this.makeShortText();

        return str;
    }
    public abstract String makeShortText();
    public abstract String makeLongText(String str);
    public void show(long elapsed) {

        this.notificationBuilder
                .setContentTitle(Html.fromHtml(
                        "<h4><b>" + this.makeSummaryText(elapsed) + "</b></h4>"))
                .setContentText(Html.fromHtml(
                        "<h4><b>" + this.makeDetailedText() + "</b></h4>"));

        this.notificationMgr.notify(NOTIFICATION_ID, this.notificationBuilder.build());

    }
    public void dismiss() {
        if (this.notificationMgr != null) {
            this.notificationMgr.cancel(NOTIFICATION_ID);
        }
    }

}
