package net.kazhik.gambarumeter.main;

import android.app.Activity;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import net.kazhik.gambarumeter.R;
import net.kazhik.gambarumeter.main.notification.NotificationController;
import net.kazhik.gambarumeterlib.DistanceUtil;

import static net.kazhik.gambarumeter.R.id.distance;

/**
 * Created by kazhik on 10/18/16.
 */

public class MainViewController {
    private Activity activity;

    private long elapsed = 0;
    private TextView tvSplitTime;

    private int stepCount = 0;
    private TextView tvStepCount;

    private int heartRate = 0;
    private TextView tvHeartRate;

    private float distance = -1f;
    private TextView tvDistance;
    private TextView tvDistanceUnit;

    private DistanceUtil distanceUtil;

    public final static int HEARTRATE_AVAILABLE = 1 << 0;
    public final static int LOCATION_AVAILABLE = 1 << 1;

    private NotificationController notificationController = new NotificationController();

    private static final String TAG = "MainViewController";

    public void initialize(Activity activity, int flags) {
        this.tvSplitTime = (TextView) activity.findViewById(R.id.split_time);

        this.tvStepCount = (TextView) activity.findViewById(R.id.stepcount_value);

        if ((flags & HEARTRATE_AVAILABLE) == HEARTRATE_AVAILABLE) {
            Log.d(TAG, "initialize(1): " + flags);
            this.tvHeartRate = (TextView) activity.findViewById(R.id.bpm);
            activity.findViewById(R.id.heart_rate).setVisibility(View.VISIBLE);
        } else {
            Log.d(TAG, "initialize(2): " + flags);
            activity.findViewById(R.id.heart_rate).setVisibility(View.GONE);
        }
        if ((flags & LOCATION_AVAILABLE) == LOCATION_AVAILABLE) {
            Log.d(TAG, "initialize(3): " + flags);
            this.tvDistance = (TextView) activity.findViewById(R.id.distance_value);
            this.tvDistanceUnit = (TextView) activity.findViewById(R.id.distance_label);
            activity.findViewById(R.id.distance).setVisibility(View.VISIBLE);
            this.distanceUtil = DistanceUtil.getInstance(activity);
        } else {
            Log.d(TAG, "initialize(4): " + flags);
            activity.findViewById(R.id.distance).setVisibility(View.GONE);

        }
        if ((flags & HEARTRATE_AVAILABLE) == HEARTRATE_AVAILABLE &&
                (flags & LOCATION_AVAILABLE) == LOCATION_AVAILABLE) {
            activity.findViewById(R.id.separator).setVisibility(View.VISIBLE);
        } else {
            activity.findViewById(R.id.separator).setVisibility(View.GONE);
        }

        this.notificationController.initialize(activity);

        this.activity = activity;
    }
    public void setSplitTime(long elapsed) {
        this.elapsed = elapsed;
    }
    public void setStepCount(int stepCount) {
        this.stepCount = stepCount;
        this.notificationController.updateStepCount(stepCount);
    }
    public void setHeartRate(int heartRate) {
        this.heartRate = heartRate;
        this.notificationController.updateHeartRate(heartRate);
    }
    public void setDistance(float distance) {
        this.distance = distance;
        this.notificationController.updateDistance(distance);
    }
    public void clear() {
        this.setStepCount(0);
        this.setSplitTime(0);
        this.setHeartRate(0);
        this.setDistance(0f);
    }
    public void dismissNotification() {
        this.notificationController.dismiss();
    }

    private void refreshViewOnUiThread() {
        this.tvSplitTime.setText(DateUtils.formatElapsedTime(this.elapsed / 1000));
        this.tvStepCount.setText(String.valueOf(this.stepCount));
        if (this.tvHeartRate != null) {
            this.tvHeartRate.setText(String.valueOf(this.heartRate));
        }
        if (this.tvDistance != null) {
            if (this.distance < 0) {
                this.tvDistance.setText(R.string.location_nosignal);
            } else {
                String str = this.distanceUtil.getDistanceStr(this.distance);
                this.tvDistance.setText(str);
            }
            this.tvDistanceUnit.setText(this.distanceUtil.getUnitStr());
        }

        this.notificationController.show(this.elapsed);

    }
    public void refreshView() {
        this.activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                refreshViewOnUiThread();
            }
        });
    }
}
