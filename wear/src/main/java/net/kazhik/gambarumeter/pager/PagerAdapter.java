package net.kazhik.gambarumeter.pager;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.wearable.view.FragmentGridPagerAdapter;
import android.support.wearable.view.GridViewPager;
import android.widget.Toast;

import net.kazhik.gambarumeter.R;
import net.kazhik.gambarumeter.history.FullHistoryFragment;
import net.kazhik.gambarumeter.history.HeartRateHistoryFragment;
import net.kazhik.gambarumeter.history.LocationHistoryFragment;
import net.kazhik.gambarumeter.main.FullMainFragment;
import net.kazhik.gambarumeter.main.GpsMainFragment;
import net.kazhik.gambarumeter.main.HeartRateMainFragment;

/**
 * Created by kazhik on 14/11/11.
 */
public class PagerAdapter extends FragmentGridPagerAdapter
        implements GridViewPager.OnPageChangeListener {

    private Context context;
    private PagerFragment[][] fragments;
    private Toast toast;
    private static final String TAG = "PagerAdapter";

    public PagerAdapter(Context context, FragmentManager fm) {
        super(fm);

        this.context = context;

        PackageManager pm = context.getPackageManager();

        if (pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)) {
            if (pm.hasSystemFeature(PackageManager.FEATURE_SENSOR_HEART_RATE)) {
                this.fragments = new PagerFragment[][]{
                        {
                                new FullMainFragment(),
                                new FullHistoryFragment()
                        }
                };
            } else {
                this.fragments = new PagerFragment[][]{
                        {
                                new GpsMainFragment(),
                                new LocationHistoryFragment()
                        }
                };
            }
        } else {
            this.fragments = new PagerFragment[][]{
                    {
                            new HeartRateMainFragment(),
                            new HeartRateHistoryFragment()
                    }
            };
        }
        
        
        
    }

    @Override
    public Fragment getFragment(int row, int col) {
        if (row > fragments.length - 1 || col > fragments[row].length - 1) {
            return null;
        }
        return fragments[row][col];
    }

    @Override
    public void onPageScrolled(int row, int column,
                               float rowOffset, float columnOffset,
                               int rowOffsetPixels, int columnOffsetPixels) {
        if (column == 0 && columnOffset < 0) {
            if (this.toast == null) {
                this.toast = Toast.makeText(this.context,
                        R.string.longpress_quit,
                        Toast.LENGTH_SHORT);
            }
            if (!this.toast.getView().isShown()) {
                this.toast.show();
            }
        }

    }

    @Override
    public void onPageSelected(int row, int col) {
        PagerFragment fragment = fragments[row][col];
        fragment.refreshView();
    }

    @Override
    public void onPageScrollStateChanged(int i) {

    }

    @Override
    public int getRowCount() {
        return fragments.length;
    }

    @Override
    public int getColumnCount(int row) {
        return fragments[row].length;
    }
}
