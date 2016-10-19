package net.kazhik.gambarumeter.main.notification;

import android.content.Context;
import android.text.format.DateUtils;

import net.kazhik.gambarumeter.R;
import net.kazhik.gambarumeterlib.DistanceUtil;

/**
 * Created by kazhik on 16/10/08.
 */

public class NotificationController {
    private NotificationView notificationView = new NotificationView();

    private int stepCount = 0;
    private String stepCountLabel;
    private int heartRate = 0;
    private String heartRateLabel;
    private float distance = 0.0f;
    private long lapTime = 0;
    private DistanceUtil distanceUtil;
    private static final String TAG = "NotificationController";

    public void initialize(Context context) {
        this.notificationView.initialize(context);

        this.distanceUtil = DistanceUtil.getInstance(context);

        this.stepCountLabel = context.getString(R.string.steps);
        this.heartRateLabel = context.getString(R.string.bpm);

        this.clear();
    }
    private void clear() {
        this.stepCount = 0;
        this.heartRate = 0;

        this.distance = 0.0f;
        this.lapTime = 0;
    }

    public void show(long elapsed) {
        String contentTitle = DateUtils.formatElapsedTime(elapsed / 1000);

        String contentText = "";
        if (this.stepCount >= 0) {
            contentText += this.stepCount + this.stepCountLabel;
        }
        if (this.heartRate > 0) {
            if (!contentText.isEmpty()) {
                contentText += " ";
            }
            contentText += this.heartRate + this.heartRateLabel;
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
    public void updateHeartRate(int heartRate) {
        this.heartRate = heartRate;
    }
}
