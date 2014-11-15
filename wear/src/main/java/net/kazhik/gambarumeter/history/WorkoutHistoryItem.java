package net.kazhik.gambarumeter.history;

import android.content.Context;
import android.support.wearable.view.WearableListView;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.kazhik.gambarumeter.R;

/**
 * Created by kazhik on 14/11/10.
 */
public class WorkoutHistoryItem extends LinearLayout
        implements WearableListView.Item {

    private TextView startTime;
    private float scale;

    public WorkoutHistoryItem(Context context) {
        this(context, null);
    }
    public WorkoutHistoryItem(Context context, AttributeSet attrs) {

        this(context, attrs, 0);
    }
    public WorkoutHistoryItem(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        this.startTime = (TextView) findViewById(R.id.start_time);
    }


    @Override
    public float getProximityMinValue() {
        return 1f;
    }

    @Override
    public float getProximityMaxValue() {
        return 1.6f;
    }

    @Override
    public float getCurrentProximityValue() {
        return this.scale;
    }

    @Override
    public void setScalingAnimatorValue(float v) {
        this.scale = v;

    }

    @Override
    public void onScaleUpStart() {

    }

    @Override
    public void onScaleDownStart() {

    }
}
