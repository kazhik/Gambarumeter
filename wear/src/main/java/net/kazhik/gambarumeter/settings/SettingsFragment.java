package net.kazhik.gambarumeter.settings;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.wearable.view.WearableListView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.kazhik.gambarumeter.R;
import net.kazhik.gambarumeter.entity.Preference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by kazhik on 14/11/11.
 */
public class SettingsFragment extends Fragment implements WearableListView.ClickListener {
    private List<Preference> prefs = new ArrayList<Preference>();
    
    private static final String TAG = "SettingsFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.settings, container, false);

    }

    public void refreshListItem() {

        Resources res = this.getActivity().getResources();
        this.prefs.add(new Preference("distanceUnit",
                res.getString(R.string.distance_unit), "metre"));

        SettingsAdapter adapter = new SettingsAdapter(this.getActivity(), this.prefs);

        WearableListView listView =
                (WearableListView)this.getActivity().findViewById(R.id.settings_list);
        if (listView == null) {
            Log.d(TAG, "settings_list not found");
            return;
        }
        listView.setAdapter(adapter);
        listView.setClickListener(this);
        listView.setGreedyTouchMode(true);
        adapter.notifyDataSetChanged();

    }

    @Override
    public void onStart() {
        super.onStart();

        this.refreshListItem();

    }

    @Override
    public void onClick(WearableListView.ViewHolder viewHolder) {
        int position = (Integer)viewHolder.itemView.getTag();

        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(this.getActivity());

        SharedPreferences.Editor editor = prefs.edit();
        Preference pref = this.prefs.get(position);
        if (pref.getKey().equals("distanceUnit")) {
            if (pref.getStringValue().equals("mile")) {
                editor.putString("distanceUnit", "metre");
                pref.setStringValue("metre");
            } else {
                editor.putString("distanceUnit", "mile");
                pref.setStringValue("mile");
            }
        }
        this.prefs.set(position, pref);
        
        WearableListView listView =
                (WearableListView)this.getActivity().findViewById(R.id.settings_list);
        SettingsAdapter adapter = (SettingsAdapter)listView.getAdapter();
        adapter.notifyItemChanged(position);

        editor.commit();
    }

    @Override
    public void onTopEmptyRegionClick() {

    }
}
