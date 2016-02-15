package net.kazhik.gambarumeter.detail;

import android.content.Context;
import android.support.wearable.view.WearableListView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.kazhik.gambarumeter.R;
import net.kazhik.gambarumeterlib.DistanceUtil;
import net.kazhik.gambarumeterlib.TimeUtil;
import net.kazhik.gambarumeterlib.entity.LapTime;

import java.util.List;

/**
 * Created by kazhik on 14/11/10.
 */
public class LocationDetailAdapter extends WearableListView.Adapter {
    private List<LapTime> dataSet;
    private final LayoutInflater inflater;
    private DistanceUtil distanceUtil;
    private static final String TAG = "LocationDetailAdapter";

    // Provide a suitable constructor (depends on the kind of dataset)
    public LocationDetailAdapter(Context context, List<LapTime> dataset) {
        this.inflater = LayoutInflater.from(context);
        this.dataSet = dataset;

        this.distanceUtil = DistanceUtil.getInstance(context);

    }

    // Provide a reference to the type of views you're using
    public static class ItemViewHolder extends WearableListView.ViewHolder {
        private TextView distanceText;
        private TextView lapTimeText;
        private TextView stepCountText;
        public ItemViewHolder(View itemView) {
            super(itemView);
            // find the text view within the custom item's layout
            this.distanceText = (TextView) itemView.findViewById(R.id.distance);
            this.lapTimeText = (TextView) itemView.findViewById(R.id.laptime);
            this.stepCountText = (TextView) itemView.findViewById(R.id.stepcount_value);
        }
    }

    // Create new views for list items
    // (invoked by the WearableListView's layout manager)
    @Override
    public WearableListView.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                          int viewType) {

        // Inflate our custom layout for list items
        int layoutId = this.getLayoutId();
        return new ItemViewHolder(this.inflater.inflate(layoutId, parent, false));
    }
    protected int getLayoutId() {
        return R.layout.location_item;
    }

    // Replace the contents of a list item
    // Instead of creating new views, the list tries to recycle existing ones
    // (invoked by the WearableListView's layout manager)
    @Override
    public void onBindViewHolder(WearableListView.ViewHolder holder,
                                 int position) {
        // retrieve the text view
        ItemViewHolder itemHolder = (ItemViewHolder) holder;
        TextView distanceText = itemHolder.distanceText;
        TextView lapTimeText = itemHolder.lapTimeText;
        TextView stepCountText = itemHolder.stepCountText;

        // replace text contents
        LapTime lapInfo = this.dataSet.get(position);
        String distanceStr =
                this.distanceUtil.getDistanceAndUnitStr(lapInfo.getDistance());
        distanceText.setText(distanceStr);

        long laptime = lapInfo.getLaptime();
        lapTimeText.setText(TimeUtil.formatSec(laptime));

        stepCountText.setText(String.valueOf(lapInfo.getStepCount()));

        // replace list item's metadata
        holder.itemView.setTag(position);
    }
    // Return the size of your dataset
    // (invoked by the WearableListView's layout manager)
    @Override
    public int getItemCount() {
        return this.dataSet.size();
    }


}
