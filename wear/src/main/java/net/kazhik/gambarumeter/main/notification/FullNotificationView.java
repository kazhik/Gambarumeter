package net.kazhik.gambarumeter.main.notification;

import android.content.Context;
import android.text.format.DateUtils;

import net.kazhik.gambarumeter.R;
import net.kazhik.gambarumeterlib.DistanceUtil;

/**
 * Created by kazhik on 16/01/22.
 */
public class FullNotificationView {
    private NotificationView notificationView = new NotificationView();

    private int stepCount = 0;
    private int heartRate = 0;
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
        this.heartRate = 0;

        this.distance = 0.0f;
        this.lapTime = 0;
    }
    private String makeContentTitle() {
        String str = "";

        return str;
    }
    private String makeContentText() {
        String str = "";

        return str;

    }

    public void show(long elapsed) {
        String contentTitle = DateUtils.formatElapsedTime(elapsed / 1000);

        Context context = this.notificationView.getContext();
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
        if (this.heartRate > 0) {
            if (!contentText.isEmpty()) {
                contentText += " ";
            }
            contentText += this.heartRate + context.getString(R.string.bpm);
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
    public void updateHeartRate(int heartRate) {
        this.heartRate = heartRate;
    }



}
