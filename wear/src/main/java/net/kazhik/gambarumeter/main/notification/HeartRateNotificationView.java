package net.kazhik.gambarumeter.main.notification;

import android.content.Context;
import android.text.format.DateUtils;

import net.kazhik.gambarumeter.R;

/**
 * Created by kazhik on 14/10/25.
 */
public class HeartRateNotificationView {
    private NotificationView notificationView = new NotificationView();

    private int stepCount = 0;
    private int heartRate = 0;

    public void initialize(Context context) {
        this.notificationView.initialize(context);

        this.clear();
    }
    public void clear() {
        this.stepCount = 0;
        this.heartRate = 0;

    }

    public void show(long elapsed) {
        String contentTitle = DateUtils.formatElapsedTime(elapsed / 1000);

        Context context = this.notificationView.getContext();
        String contentText = "";
        if (this.stepCount >= 0) {
            contentText += this.stepCount + context.getString(R.string.steps);
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
    public void updateHeartRate(int heartRate) {
        this.heartRate = heartRate;
    }
}
