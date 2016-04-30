package net.kazhik.gambarumeter.main.notification;

import android.content.Context;
import android.text.format.DateUtils;

import net.kazhik.gambarumeter.R;
import net.kazhik.gambarumeterlib.DistanceUtil;

/**
 * Created by kazhik on 14/10/25.
 */
public class LocationNotificationView {
    private NotificationView notificationView = new NotificationView();

    private int stepCount = 0;
    private float distance = 0.0f;
    private long lapTime = 0;
    private DistanceUtil distanceUtil;


    public void initialize(Context context) {
        this.notificationView.initialize(context);

        this.distanceUtil = DistanceUtil.getInstance(context);

        this.clear();
    }
    public void clear() {
        this.stepCount = 0;

        this.distance = 0.0f;
        this.lapTime = 0;
    }

    public void show(long elapsed) {
        Context context = this.notificationView.getContext();
        if (context == null) {
            return;
        }
        String contentTitle = DateUtils.formatElapsedTime(elapsed / 1000);

        String contentText = "";
        if (this.stepCount >= 0) {
            contentText += this.stepCount + context.getString(R.string.steps);
        }
        if (this.distance > 0) {
            if (!contentText.isEmpty()) {
                contentText += " ";
            }
            contentText += this.distanceUtil.getDistanceAndUnitStr(this.distance);
        }
        if (this.lapTime > 0) {
            if (!contentText.isEmpty()) {
                contentText += " ";
            }
            contentText += DateUtils.formatElapsedTime(this.lapTime / 1000);
            contentText += "/";
            contentText += this.distanceUtil.getUnitStr();
        }

        this.notificationView.show(contentTitle, contentText);

    }

    public void dismiss() {
        this.notificationView.dismiss();

    }

    public void updateStepCount(int stepCount) {
        this.stepCount = stepCount;
    }
    public void updateDistance(float distance) {
        this.distance = distance;
    }
    public void updateLap(long laptime) {
        this.lapTime = laptime;
    }

}
