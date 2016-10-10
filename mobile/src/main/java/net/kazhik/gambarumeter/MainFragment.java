package net.kazhik.gambarumeter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import net.kazhik.gambarumeterlib.DistanceUtil;
import net.kazhik.gambarumeterlib.TimeUtil;
import net.kazhik.gambarumeterlib.entity.WorkoutInfo;
import net.kazhik.gambarumeterlib.storage.DataStorage;
import net.kazhik.gambarumeterlib.storage.WorkoutTable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by kazhik on 15/10/14.
 */
public class MainFragment extends Fragment implements NavigationView.OnNavigationItemSelectedListener {

    private SimpleAdapter listAdapter;
    private ArrayList<HashMap<String, String>> mapList = new ArrayList<>();
    private DistanceUtil distanceUtil;

    private static final int CONTEXTMENU_DELETE = 1001;
    private static final int CONTEXTMENU_SELECT = 1002;

    private static final String TAG = "MainFragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Context context = this.getActivity();

        this.distanceUtil = DistanceUtil.getInstance(context);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        NavigationView navigationView =
                (NavigationView)this.getActivity().findViewById(R.id.navigation);

        navigationView.getMenu().clear();
        navigationView.inflateMenu(R.menu.main_drawer);
        navigationView.setNavigationItemSelectedListener(this);

        return inflater.inflate(R.layout.main_fragment, container, false);
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        this.initializeUI();

        this.loadList(0);
    }
    private int idxMonth = 0;

    private void initializeUI() {
        Activity activity = this.getActivity();

        Button leftButton = (Button) activity.findViewById(R.id.left_arrow);
        leftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick left button");
                loadList(-1);
            }
        });
        Button rightButton = (Button) activity.findViewById(R.id.right_arrow);
        rightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick right button");
                loadList(1);
            }
        });

        ListView lv = (ListView) activity.findViewById(R.id.history);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent,
                                    View view,
                                    int position,
                                    long id) {

                ListView listView = (ListView) parent;
                Map<String, String> map = (Map<String, String>) listView
                        .getItemAtPosition(position);

                Log.d(TAG, "startTime: " + map.get("startTime")
                        + ":" + map.get("startTimeStr"));

                /*
                ChartFragment chartFragment = new ChartFragment();
                Bundle chartParam = new Bundle();
                chartParam.putLong("startTime", Long.valueOf(map.get("startTime")));
                chartFragment.setArguments(chartParam);

                getFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, chartFragment)
                        .addToBackStack(null)
                        .commit();
                */

                DetailFragment detailFragment = new DetailFragment();
                Bundle param = new Bundle();
                param.putLong("startTime", Long.parseLong(map.get("startTime")));
                detailFragment.setArguments(param);
                getFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, detailFragment)
                        .addToBackStack(null)
                        .commit();


            }
        });
        registerForContextMenu(lv);

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        AdapterView.AdapterContextMenuInfo info =
                (AdapterView.AdapterContextMenuInfo) menuInfo;

        HashMap<String, String> map = this.mapList.get(info.position);

        menu.setHeaderTitle(map.get("startTimeStr"));
        menu.add(Menu.NONE, CONTEXTMENU_DELETE, Menu.NONE,
                R.string.delete);
        /*
        menu.add(Menu.NONE, CONTEXTMENU_SELECT, Menu.NONE,
                R.string.select);
        */


    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        class ConfirmDeleteListener implements
                DialogInterface.OnClickListener {
            private int position;

            public ConfirmDeleteListener(int position) {
                this.position = position;
            }

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    MainFragment.this.deleteTransaction(this.position);
                }

            }
        }
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
                .getMenuInfo();

        switch (item.getItemId()) {
            case CONTEXTMENU_DELETE:
                AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
                // Add the buttons
                builder.setPositiveButton(android.R.string.ok,
                        new ConfirmDeleteListener(info.position));
                builder.setNegativeButton(android.R.string.cancel,
                        new ConfirmDeleteListener(info.position));
                builder.setTitle(R.string.confirm_delete);
                AlertDialog dialog = builder.create();
                dialog.show();
                break;
            case CONTEXTMENU_SELECT:
                HashMap<String, String> selected = this.mapList.get(info.position);
                String startTime = selected.get("startTime");

                Log.d(TAG, "selected startTime: " + startTime);

                break;
            default:
                break;
        }
        return true;
    }
    public void deleteTransaction(int position) {

        HashMap<String, String> removed = this.mapList.remove(position);
        this.listAdapter.notifyDataSetChanged();

        long startTime = Long.parseLong(removed.get("startTime"));

        Context context = this.getActivity();

        DataStorage storage = new DataStorage(context);
        storage.delete(startTime);
        storage.close();

    }

    private void loadList(int index) {
        Activity activity = this.getActivity();

        WorkoutTable workoutTable = new WorkoutTable(activity);
        workoutTable.openReadonly();

        List<WorkoutInfo> workoutList = workoutTable.selectMonth(this.idxMonth + index);
        workoutTable.close();

        Log.d(TAG, "WorkoutList size=" + workoutList.size());
        if (workoutList.isEmpty()) {
            Toast.makeText(activity, R.string.nodata,
                    Toast.LENGTH_LONG).show();

            return;
        }
        this.idxMonth += index;

        this.mapList.clear();
        for (WorkoutInfo workout: workoutList) {

            HashMap<String, String> map = new HashMap<>();
            long startTime = workout.getStartTime();
            map.put("startTime", String.valueOf(startTime));

            int flag = DateUtils.FORMAT_SHOW_YEAR |
                    DateUtils.FORMAT_SHOW_DATE |
                    DateUtils.FORMAT_SHOW_TIME;
            map.put("startTimeStr",
                    DateUtils.formatDateTime(activity,
                            startTime,
                            flag)
            );

            long stopTime = workout.getStopTime();
            int stepCount = workout.getStepCount();
            float distance = workout.getDistance();
            int heartRate = workout.getHeartRate();

            String resultStr = TimeUtil.formatMsec(stopTime - startTime);
            if (distance > 0.0f) {
                resultStr += "/" + this.distanceUtil.getDistanceAndUnitStr(distance);
            } else if (heartRate > 0) {
                String heartRateStr = String.format("%d%s", heartRate,
                        activity.getResources().getString(R.string.bpm));
                resultStr += "/" + heartRateStr;
            }
            String stepCountStr = stepCount +
                    activity.getResources().getString(R.string.steps);
            resultStr += "/" + stepCountStr;

            map.put("result", resultStr);
//            Log.d(TAG, "startTime: " + map.get("startTime") + ":" + map.get("startTimeStr"));
            this.mapList.add(map);

        }

        this.listAdapter = new SimpleAdapter(activity,
                this.mapList,
                R.layout.history_item,
                new String[] { "startTimeStr", "result" },
                new int[] { R.id.start_time, R.id.result }
        );
        ListView lv = (ListView)activity.findViewById(R.id.history);
        lv.setAdapter(this.listAdapter);
        lv.setSelection(0);
        this.listAdapter.notifyDataSetChanged();
    }

    private void openSettings() {

        getFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new SettingsFragment())
                .addToBackStack(null)
                .commit();

    }

    private void showAboutDialog()
    {
        AboutDialog aboutDialog = AboutDialog.newInstance();
        aboutDialog.show(getFragmentManager(), "dialog");

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        DrawerLayout drawerLayout =
                (DrawerLayout) this.getActivity().findViewById(R.id.drawer_layout);
        drawerLayout.closeDrawers();
        switch (item.getItemId()) {
            case R.id.action_settings:
                this.openSettings();
                break;
            case R.id.action_about:
                this.showAboutDialog();
                break;
            default:
                break;
        }
        return false;
    }
}
