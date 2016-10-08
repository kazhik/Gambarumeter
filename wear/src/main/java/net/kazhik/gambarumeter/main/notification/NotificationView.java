package net.kazhik.gambarumeter.main.notification;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.text.Html;

import net.kazhik.gambarumeter.R;
import net.kazhik.gambarumeter.WearGambarumeter;

/**
 * Created by kazhik on 14/10/25.
 */
public class NotificationView {
    private Context context;
    private NotificationCompat.Builder notificationBuilder;
    private NotificationManagerCompat notificationMgr = null;

    private static final int NOTIFICATION_ID = 3000;
    private static final String TAG = "NotificationView";

    public void initialize(Context context) {

        this.context = context;

        this.setupNotification();

    }

    private void setupNotification() {
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
    public void show(String contentTitle, String contentText) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            this.notificationBuilder
                    .setContentTitle(Html.fromHtml(
                            "<h4><b>" + contentTitle + "</b></h4>",
                            Html.FROM_HTML_MODE_LEGACY))
                    .setContentText(Html.fromHtml(
                            "<h4><b>" + contentText + "</b></h4>",
                            Html.FROM_HTML_MODE_LEGACY));
        } else {
            this.notificationBuilder
                    .setContentTitle(Html.fromHtml(
                            "<h4><b>" + contentTitle + "</b></h4>"))
                    .setContentText(Html.fromHtml(
                            "<h4><b>" + contentText + "</b></h4>"));
        }

        this.notificationMgr.notify(NOTIFICATION_ID, this.notificationBuilder.build());
    }
    public void dismiss() {
        if (this.notificationMgr != null) {
            this.notificationMgr.cancel(NOTIFICATION_ID);
        }
    }

}
