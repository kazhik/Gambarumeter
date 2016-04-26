package net.kazhik.gambarumeter.main.notification;

import net.kazhik.gambarumeter.R;

/**
 * Created by kazhik on 14/10/25.
 */
public class HeartRateNotificationView extends NotificationView {
    private int heartRate = -1;

    public void clear() {
        super.clear();
        this.heartRate = -1;
    }
    public void updateHeartRate(int heartRate) {

        this.heartRate = heartRate;
    }
    public String makeShortText() {
        String str = "";
        if (this.heartRate > 0) {
            str += "/";
            str += this.heartRate + this.getContext().getString(R.string.bpm);
        }
        return str;
    }
    public String makeLongText(String str) {
        
        return str;
        
    }

}
