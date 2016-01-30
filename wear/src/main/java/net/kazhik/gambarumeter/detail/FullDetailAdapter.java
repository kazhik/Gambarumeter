package net.kazhik.gambarumeter.detail;

import android.content.Context;

import net.kazhik.gambarumeter.R;
import net.kazhik.gambarumeterlib.entity.LapTime;

import java.util.List;

/**
 * Created by kazhik on 16/01/28.
 */
public class FullDetailAdapter extends LocationDetailAdapter {
    private static final String TAG = "FullDetailAdapter";

    public FullDetailAdapter(Context context, List<LapTime> dataset) {
        super(context, dataset);
    }
    @Override
    protected int getLayoutId() {
        return R.layout.full_item;
    }

}
