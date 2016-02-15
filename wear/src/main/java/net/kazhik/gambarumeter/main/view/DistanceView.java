package net.kazhik.gambarumeter.main.view;

import android.content.Context;
import android.widget.TextView;

import net.kazhik.gambarumeterlib.DistanceUtil;

/**
 * Created by kazhik on 14/10/18.
 */
public class DistanceView implements Runnable {
    private TextView distanceText;
    private TextView distanceUnitText;
    private float distance = 0;
    private DistanceUtil distanceUtil;
    private boolean available;
    private static final String TAG = "DistanceView";

    public void initialize(TextView distanceText, TextView distanceUnitText) {
        this.distanceText = distanceText;
        this.distanceUnitText = distanceUnitText;

        this.setAvailable(false);
    }
    public void initialize(Context context,
                           TextView distanceText,
                           TextView distanceUnitText) {

        this.distanceText = distanceText;
        this.distanceUnitText = distanceUnitText;

        this.setAvailable(false);

        this.distanceUtil = DistanceUtil.getInstance(context);
    }

    public DistanceView setDistance(float distance) {
        this.distance = distance;

        return this;
    }
    public void setAvailable(boolean available) {
        if (available) {
            this.distanceText.setText("0.00");
        } else {
            this.distanceText.setText("-.--");
        }
        this.available = available;
    }

    public void refresh() {
        this.distanceUnitText.setText(this.distanceUtil.getUnitStr());
        
        if (!this.available) {
            return;
        }
        String str = this.distanceUtil.getDistanceStr(this.distance);
        this.distanceText.setText(str);
    }

    @Override
    public void run() {
        this.refresh();
    }
}
