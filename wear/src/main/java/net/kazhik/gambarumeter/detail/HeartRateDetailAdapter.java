package net.kazhik.gambarumeter.detail;

import android.content.Context;
import android.support.wearable.view.WearableListView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.kazhik.gambarumeter.R;
import net.kazhik.gambarumeterlib.entity.HeartRateDetail;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by kazhik on 14/11/10.
 */
public class HeartRateDetailAdapter extends WearableListView.Adapter {
    private List<HeartRateDetail> dataSet;
    private final LayoutInflater inflater;
    private static final String TAG = "HeartRateDetailAdapter";

    // Provide a suitable constructor (depends on the kind of dataset)
    public HeartRateDetailAdapter(Context context, List<HeartRateDetail> dataset) {
        this.inflater = LayoutInflater.from(context);
        this.dataSet = dataset;

    }

    // Provide a reference to the type of views you're using
    public static class ItemViewHolder extends WearableListView.ViewHolder {
        private TextView tvTimestamp;
        private TextView tvHeartRate;
        private TextView tvStepcount;
        public ItemViewHolder(View itemView) {
            super(itemView);
            // find the text view within the custom item's layout
            this.tvTimestamp = (TextView) itemView.findViewById(R.id.heartrate_timestamp);
            this.tvHeartRate = (TextView) itemView.findViewById(R.id.heartrate_value);
            this.tvStepcount = (TextView) itemView.findViewById(R.id.stepcount_value);
        }
    }

    // Create new views for list items
    // (invoked by the WearableListView's layout manager)
    @Override
    public WearableListView.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                          int viewType) {

        // Inflate our custom layout for list items
        return new ItemViewHolder(this.inflater.inflate(R.layout.heartrate_item, null));
    }

    // Replace the contents of a list item
    // Instead of creating new views, the list tries to recycle existing ones
    // (invoked by the WearableListView's layout manager)
    @Override
    public void onBindViewHolder(WearableListView.ViewHolder holder,
                                 int position) {
        // retrieve the text view
        ItemViewHolder itemHolder = (ItemViewHolder) holder;
        TextView tvTimestamp = itemHolder.tvTimestamp;
        TextView tvHeartRate = itemHolder.tvHeartRate;
        TextView tvStepcount = itemHolder.tvStepcount;

        // replace text contents
        HeartRateDetail heartRate = this.dataSet.get(position);
        tvTimestamp.setText(this.formatTimestamp(heartRate.getTimestamp()));
        tvHeartRate.setText(String.valueOf(heartRate.getHeartRate()));
        tvStepcount.setText(String.valueOf(heartRate.getStepCount()));

        // replace list item's metadata
        holder.itemView.setTag(position);
    }
    private String formatTimestamp(long timestamp) {

        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date(timestamp));

        return String.format("%02d:%02d",
                cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));
    }

    // Return the size of your dataset
    // (invoked by the WearableListView's layout manager)
    @Override
    public int getItemCount() {
        return this.dataSet.size();
    }


}
