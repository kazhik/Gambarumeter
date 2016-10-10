package net.kazhik.gambarumeter.detail;

import android.content.Context;
import android.support.wearable.view.WearableListView;
import android.view.ViewGroup;

import net.kazhik.gambarumeter.R;
import net.kazhik.gambarumeterlib.entity.LapTime;

import java.util.List;

/**
 * Created by kazhik on 16/01/28.
 */
public class FullDetailAdapter extends LocationDetailAdapter {
    private static final String TAG = "FullDetailAdapter";

    // Provide a suitable constructor (depends on the kind of dataset)
    public FullDetailAdapter(Context context, List<LapTime> dataset) {
        super(context, dataset);
    }

    // Create new views for list items
    // (invoked by the WearableListView's layout manager)
    @Override
    public WearableListView.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                          int viewType) {

        // Inflate our custom layout for list items
        return new ItemViewHolder(this.getItemView(parent, R.layout.full_item));
    }
    // Replace the contents of a list item
    // Instead of creating new views, the list tries to recycle existing ones
    // (invoked by the WearableListView's layout manager)
    @Override
    public void onBindViewHolder(WearableListView.ViewHolder holder,
                                 int position) {

        super.onBindViewHolder(holder, position);

        ItemViewHolder itemHolder = (ItemViewHolder) holder;
        LapTime lapInfo = this.getDataSet(position);
        itemHolder.setHeartRate(String.valueOf(lapInfo.getHeartRate()));

        // replace list item's metadata
        holder.itemView.setTag(position);
    }


}
