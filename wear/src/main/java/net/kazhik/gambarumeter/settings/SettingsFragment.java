package net.kazhik.gambarumeter.settings;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.wearable.view.WearableListView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.kazhik.gambarumeter.pager.PagerFragment;
import net.kazhik.gambarumeter.R;
import net.kazhik.gambarumeterlib.entity.Preference;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kazhik on 14/11/11.
 */
public class SettingsFragment extends PagerFragment
        implements WearableListView.ClickListener {
    private List<Preference> prefList = new ArrayList<>();
    private SharedPreferences prefs;
    private static final String TAG = "SettingsFragment";

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        Log.d(TAG, "onCreateView: ");
        return inflater.inflate(R.layout.settings, container, false);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Log.d(TAG, "onActivityCreated: ");
        this.prefs =
                PreferenceManager.getDefaultSharedPreferences(this.getActivity());

        if (this.prefList.isEmpty()) {
            Resources res = this.getActivity().getResources();
            this.prefList.add(new Preference("distanceUnit",
                    res.getString(R.string.distance_unit),
                    this.prefs.getString("distanceUnit", "metre")));
        }
        Activity activity = this.getActivity();
        SettingsAdapter adapter = new SettingsAdapter(activity, this.prefList);

        WearableListView listView =
                (WearableListView)activity.findViewById(R.id.settings_list);

        listView.setAdapter(adapter);
        listView.setClickListener(this);
        listView.setGreedyTouchMode(true);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void refreshView() {
        
        
    }
    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onClick(WearableListView.ViewHolder viewHolder) {
        int position = (Integer)viewHolder.itemView.getTag();

        SharedPreferences.Editor editor = this.prefs.edit();
        Preference pref = this.prefList.get(position);
        if (pref.getKey().equals("distanceUnit")) {
            if (pref.getStringValue().equals("mile")) {
                editor.putString("distanceUnit", "metre");
                pref.setStringValue("metre");
            } else {
                editor.putString("distanceUnit", "mile");
                pref.setStringValue("mile");
            }
        }
        this.prefList.set(position, pref);
        
        WearableListView listView =
                (WearableListView)this.getActivity().findViewById(R.id.settings_list);
        SettingsAdapter adapter = (SettingsAdapter)listView.getAdapter();
        adapter.notifyItemChanged(position);

        editor.apply();
    }

    @Override
    public void onTopEmptyRegionClick() {

    }
}
