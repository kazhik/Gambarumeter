package net.kazhik.gambarumeter.main.view;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
    private int stepCount = -1;

    private static final int NOTIFICATION_ID = 3000;

    public void initialize(Context context) {

        this.context = context;

        Intent intent = new Intent(context, WearGambarumeter.class);
//        int flag = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_NO_CREATE;
        int flag = 0;
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, flag);

        NotificationCompat.WearableExtender extender = new NotificationCompat.WearableExtender()
                .setDisplayIntent(pendingIntent)
                .setCustomSizePreset(NotificationCompat.WearableExtender.SIZE_FULL_SCREEN);

/*
        NotificationCompat.Action openMain
                = new NotificationCompat.Action(R.drawable.empty, null, pendingIntent);

        Bitmap bmp = BitmapFactory.decodeResource(this.context.getResources(),
                R.drawable.notification);

        NotificationCompat.WearableExtender extender = new NotificationCompat.WearableExtender()
                .setHintHideIcon(true)
                .setBackground(bmp)
//                .setDisplayIntent(pendingIntent)
//                .setCustomSizePreset(NotificationCompat.WearableExtender.SIZE_LARGE)
                .setContentAction(0)
                .addAction(openMain);
*/

        this.notificationBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_launcher)
                .extend(extender)
                .setOnlyAlertOnce(true)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setOngoing(true);

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
                        "<b>" + this.makeSummaryText(elapsed) + "</b>"))
                .setContentText(Html.fromHtml(
                        "<b>" + this.makeDetailedText() + "</b>"));

        NotificationManagerCompat.from(this.context)
                .notify(NOTIFICATION_ID, this.notificationBuilder.build());

    }
    public void dismiss() {
        NotificationManagerCompat.from(this.context).cancel(NOTIFICATION_ID);
    }

}
