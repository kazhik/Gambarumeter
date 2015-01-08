package net.kazhik.gambarumeter.main.view;

import android.os.Handler;
import android.widget.TextView;

import net.kazhik.gambarumeter.Util;

/**
 * Created by kazhik on 14/10/18.
 */
public class DistanceView implements Runnable {
    private TextView distanceText;
    private TextView distanceUnitText;
    private float distance = 0;
    private String distanceUnit;
    private String distanceUnitStr;
    
    public void initialize(TextView distanceText, TextView distanceUnitText) {
        this.distanceText = distanceText;
        this.distanceUnitText = distanceUnitText;

        this.setEnable(false);
    }

    public DistanceView setDistance(float distance) {
        this.distance = distance;

        return this;
    }
    public DistanceView setDistanceUnit(String distanceUnit) {
        this.distanceUnit = distanceUnit;
        
        return this;
    }
    public DistanceView setDistanceUnitStr(String distanceUnitStr) {
        this.distanceUnitStr = distanceUnitStr;

        return this;
    }
    public void setEnable(boolean enabled) {
        if (enabled) {
            this.distanceText.setText("0.00");
        } else {
            this.distanceText.setText("-.--");
        }
    }

    public void refresh() {
        
        float distance = Util.convertMeter(this.distance, this.distanceUnit);

        String str = String.format("%.2f", distance);
        this.distanceText.setText(str);
        
        this.distanceUnitText.setText(this.distanceUnitStr);
    }

    @Override
    public void run() {
        this.refresh();
    }
}
