package net.kazhik.gambarumeter.detail;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.wearable.view.WearableListView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.kazhik.gambarumeter.R;
import net.kazhik.gambarumeter.entity.Lap;
import net.kazhik.gambarumeter.entity.WorkoutInfo;
import net.kazhik.gambarumeter.net.kazhik.gambarumeter.util.Util;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by kazhik on 14/11/10.
 */
public class LocationDetailAdapter extends WearableListView.Adapter {
    private List<Lap> dataSet;
    private final Context context;
    private final LayoutInflater inflater;
    private String prefDistanceUnit;
    private static final String TAG = "LocationDetailAdapter";

    // Provide a suitable constructor (depends on the kind of dataset)
    public LocationDetailAdapter(Context context, List<Lap> dataset) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.dataSet = dataset;

        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(context);
        this.prefDistanceUnit = prefs.getString("distanceUnit", "metre");

    }

    // Provide a reference to the type of views you're using
    public static class ItemViewHolder extends WearableListView.ViewHolder {
        private TextView distanceText;
        private TextView lapTimeText;
        public ItemViewHolder(View itemView) {
            super(itemView);
            // find the text view within the custom item's layout
            this.distanceText = (TextView) itemView.findViewById(R.id.distance);
            this.lapTimeText = (TextView) itemView.findViewById(R.id.laptime);
        }
    }

    // Create new views for list items
    // (invoked by the WearableListView's layout manager)
    @Override
    public WearableListView.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                          int viewType) {

        // Inflate our custom layout for list items
        return new ItemViewHolder(this.inflater.inflate(R.layout.location_item, null));
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

        // replace text contents
        Lap lapInfo = this.dataSet.get(position);
        long laptime = lapInfo.getLaptime();
        float distance = Util.convertMeter(lapInfo.getDistance(), this.prefDistanceUnit);
        Log.d(TAG, "distance: " + lapInfo.getDistance() + "; laptime: " + laptime);
        String distanceUnitStr;
        if (this.prefDistanceUnit.equals("mile")) {
            distanceUnitStr = context.getResources().getString(R.string.mile);
        } else {
            distanceUnitStr = context.getResources().getString(R.string.km);
        }
        String distanceStr = String.format("%.2f%s", distance, distanceUnitStr);
        distanceText.setText(distanceStr);


        lapTimeText.setText(this.formatLapTime(laptime));

        // replace list item's metadata
        holder.itemView.setTag(position);
    }
    private String formatLapTime(long lapTime) {

        long hour = lapTime / 60 / 60;
        long min = lapTime / 60 - (hour * 60);
        long sec = lapTime % 60;

        return String.format("%d:%02d:%02d", hour, min, sec);
    }

    // Return the size of your dataset
    // (invoked by the WearableListView's layout manager)
    @Override
    public int getItemCount() {
        return this.dataSet.size();
    }


}
