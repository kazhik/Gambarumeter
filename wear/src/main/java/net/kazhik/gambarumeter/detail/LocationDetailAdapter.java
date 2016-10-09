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

    // Create new views for list items
    // (invoked by the WearableListView's layout manager)
    @Override
    public WearableListView.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                          int viewType) {

        return new ItemViewHolder(this.getItemView(parent, R.layout.location_item));
    }
    protected LapTime getDataSet(int position) {
        return this.dataSet.get(position);
    }
    public View getItemView(ViewGroup parent, int layoutId) {
        // Inflate our custom layout for list items
        return this.inflater.inflate(layoutId, parent, false);

    }

    // Replace the contents of a list item
    // Instead of creating new views, the list tries to recycle existing ones
    // (invoked by the WearableListView's layout manager)
    @Override
    public void onBindViewHolder(WearableListView.ViewHolder holder,
                                 int position) {
        // retrieve the text view
        ItemViewHolder itemHolder = (ItemViewHolder) holder;

        // replace text contents
        LapTime lapInfo = this.getDataSet(position);
        String distanceStr =
                this.distanceUtil.getDistanceAndUnitStr(lapInfo.getDistance());
        itemHolder.setDistance(distanceStr);

        long laptime = lapInfo.getLaptime();
        itemHolder.setLapTime(TimeUtil.formatSec(laptime));

        itemHolder.setStepCount(String.valueOf(lapInfo.getStepCount()));

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
