package net.kazhik.gambarumeter.main.view;

import android.content.Context;
import android.text.format.DateUtils;

import net.kazhik.gambarumeterlib.DistanceUtil;

/**
 * Created by kazhik on 14/10/25.
 */
public class LocationNotificationView extends NotificationView {
    private float distance = -1.0f;
    private long lapTime = 0;
    private DistanceUtil distanceUtil;

    @Override
    public void initialize(Context context) {
        super.initialize(context);

        this.distanceUtil = DistanceUtil.getInstance(context);
    }

    public void clear() {
        super.clear();
        this.distance = -1.0f;
        this.lapTime = 0;
    }
    public void updateDistance(float distance) {
        this.distance = distance;
    }
    public void updateLap(long laptime) {
        this.lapTime = laptime;
        
    }
    public String makeShortText() {
        String str = "";
        if (this.distance > 0) {
            str += " ";
            str += this.distanceUtil.getDistanceAndUnitStr(this.distance);
        }
        return str;
    }
    public String makeLongText(String str) {
        if (this.lapTime > 0) {
            if (!str.isEmpty()) {
                str += " ";
            }
            str += DateUtils.formatElapsedTime(this.lapTime / 1000);
            str += "/";
            str += this.distanceUtil.getUnitStr();
        }
        return str;

    }

}
