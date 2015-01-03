package net.kazhik.gambarumeter.settings;

import android.content.Context;
import android.support.wearable.view.WearableListView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import net.kazhik.gambarumeter.R;
import net.kazhik.gambarumeter.entity.Preference;

import java.util.List;
import java.util.Map;

/**
 * Created by kazhik on 15/01/02.
 */
public class SettingsAdapter extends WearableListView.Adapter {
    private List<Preference> dataSet;
    private final Context context;
    private final LayoutInflater inflater;
    private static final String TAG = "SettingsAdapter";

    // Provide a suitable constructor (depends on the kind of dataset)
    public SettingsAdapter(Context context, List<Preference> dataset) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.dataSet = dataset;

    }

    // Provide a reference to the type of views you're using
    public static class ItemViewHolder extends WearableListView.ViewHolder {
        private TextView itemName;
        private TextView itemValue;
        private ImageView image;
        public ItemViewHolder(View itemView) {
            super(itemView);
            // find the text view within the custom item's layout
            this.itemName = (TextView) itemView.findViewById(R.id.item_name);
            this.itemValue = (TextView) itemView.findViewById(R.id.item_value);
        }
    }

    // Create new views for list items
    // (invoked by the WearableListView's layout manager)
    @Override
    public WearableListView.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                          int viewType) {

        // Inflate our custom layout for list items
        return new ItemViewHolder(this.inflater.inflate(R.layout.settings_item, null));
    }

    // Replace the contents of a list item
    // Instead of creating new views, the list tries to recycle existing ones
    // (invoked by the WearableListView's layout manager)
    @Override
    public void onBindViewHolder(WearableListView.ViewHolder holder,
                                 int position) {
        // retrieve the text view
        ItemViewHolder itemHolder = (ItemViewHolder) holder;
        TextView itemName = itemHolder.itemName;
        TextView itemValue = itemHolder.itemValue;

        // replace text contents
        Preference pref = this.dataSet.get(position);

        
        itemName.setText(pref.getName());
        itemValue.setText(pref.getStringValue());

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
