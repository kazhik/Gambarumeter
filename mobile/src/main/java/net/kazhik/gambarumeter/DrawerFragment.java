package net.kazhik.gambarumeter;

import android.app.Fragment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by kazhik on 16/02/08.
 */
public abstract class DrawerFragment extends Fragment {
    public abstract List<Map<String, String>> makeDrawerItems();
    public abstract void onClickDrawerItem(int resId);
    public abstract void onClickDrawerItem(int resId, long startTime);

    protected Map<String, String> makeDrawerItem(int strId, int iconId) {
        Map<String, String> drawerItem = new HashMap<>();

        drawerItem.put("id", String.valueOf(strId));
        drawerItem.put("text", getString(strId));
        drawerItem.put("icon", String.valueOf(iconId));

        return drawerItem;
    }

}
