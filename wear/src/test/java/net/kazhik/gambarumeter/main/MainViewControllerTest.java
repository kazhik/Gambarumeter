package net.kazhik.gambarumeter.main;

import android.app.Activity;
import android.content.Context;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.TextView;

import net.kazhik.gambarumeter.R;
import net.kazhik.gambarumeter.main.notification.NotificationController;
import net.kazhik.gambarumeterlib.DistanceUtil;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.lang.reflect.Method;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

/**
 * Created by kazhik on 10/19/16.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({DistanceUtil.class, DateUtils.class})
public class MainViewControllerTest {
    @Mock
    private NotificationController mockNotificationController;

    @InjectMocks
    private MainViewController mainViewController;

    @Test
    public void initialize() throws Exception {
        Activity mockActivity = Mockito.mock(Activity.class);

        // Heart rate
        TextView tvHeartRate = Mockito.mock(TextView.class);
        View vHeartRate = Mockito.mock(View.class);
        when(mockActivity.findViewById(eq(R.id.bpm))).thenReturn(tvHeartRate);
        when(mockActivity.findViewById(eq(R.id.heart_rate))).thenReturn(vHeartRate);

        // Distance
        TextView tvDistance = Mockito.mock(TextView.class);
        TextView tvDistanceUnit = Mockito.mock(TextView.class);
        View vDistance = Mockito.mock(View.class);
        when(mockActivity.findViewById(eq(R.id.distance))).thenReturn(vDistance);
        when(mockActivity.findViewById(eq(R.id.distance_value))).thenReturn(tvDistance);
        when(mockActivity.findViewById(eq(R.id.distance_label))).thenReturn(tvDistanceUnit);

        // Separator
        TextView tvSeparator = Mockito.mock(TextView.class);
        when(mockActivity.findViewById(eq(R.id.separator))).thenReturn(tvSeparator);

        PowerMockito.mockStatic(DistanceUtil.class);
        DistanceUtil mockDistanceUtil = PowerMockito.mock(DistanceUtil.class);
        when(DistanceUtil.getInstance(any(Context.class))).thenReturn(mockDistanceUtil);

        // call target method, no flags
        int flags = 0;
        mainViewController.initialize(mockActivity, flags);
        // verify
        Mockito.verify(vHeartRate, Mockito.times(1)).setVisibility(View.GONE);
        Mockito.verify(vDistance, Mockito.times(1)).setVisibility(View.GONE);
        Mockito.verify(tvSeparator, Mockito.times(1)).setVisibility(View.GONE);

        // call target method, heart rate available
        flags = MainViewController.HEARTRATE_AVAILABLE;
        mainViewController.initialize(mockActivity, flags);
        // verify
        Mockito.verify(vHeartRate, Mockito.times(1)).setVisibility(View.VISIBLE);
        Mockito.verify(vDistance, Mockito.times(2)).setVisibility(View.GONE);
        Mockito.verify(tvSeparator, Mockito.times(2)).setVisibility(View.GONE);

        // call target method, heart rate and location available
        flags = MainViewController.HEARTRATE_AVAILABLE | MainViewController.LOCATION_AVAILABLE;
        mainViewController.initialize(mockActivity, flags);
        // verify
        Mockito.verify(vHeartRate, Mockito.times(2)).setVisibility(View.VISIBLE);
        Mockito.verify(vDistance, Mockito.times(1)).setVisibility(View.VISIBLE);
        Mockito.verify(tvSeparator, Mockito.times(1)).setVisibility(View.VISIBLE);

        // call target method, location available
        flags = MainViewController.LOCATION_AVAILABLE;
        mainViewController.initialize(mockActivity, flags);
        // verify
        Mockito.verify(vHeartRate, Mockito.times(2)).setVisibility(View.GONE);
        Mockito.verify(vDistance, Mockito.times(2)).setVisibility(View.VISIBLE);
        Mockito.verify(tvSeparator, Mockito.times(3)).setVisibility(View.GONE);

    }

    @Test
    public void setSplitTime() throws Exception {

    }

    @Test
    public void setStepCount() throws Exception {

    }

    @Test
    public void setHeartRate() throws Exception {

    }

    @Test
    public void setDistance() throws Exception {

    }

    @Test
    public void clear() throws Exception {

    }

    @Test
    public void dismissNotification() throws Exception {

    }

    @Test
    public void refreshView() throws Exception {
        Activity mockActivity = Mockito.mock(Activity.class);
        // Split time
        TextView tvSplitTime = Mockito.mock(TextView.class);
        when(mockActivity.findViewById(eq(R.id.split_time))).thenReturn(tvSplitTime);

        // Step count
        TextView tvStepCount = Mockito.mock(TextView.class);
        when(mockActivity.findViewById(eq(R.id.stepcount_value))).thenReturn(tvStepCount);

        // Heart rate
        TextView tvHeartRate = Mockito.mock(TextView.class);
        View vHeartRate = Mockito.mock(View.class);
        when(mockActivity.findViewById(eq(R.id.bpm))).thenReturn(tvHeartRate);
        when(mockActivity.findViewById(eq(R.id.heart_rate))).thenReturn(vHeartRate);

        // Distance
        TextView tvDistance = Mockito.mock(TextView.class);
        TextView tvDistanceUnit = Mockito.mock(TextView.class);
        View vDistance = Mockito.mock(View.class);
        when(mockActivity.findViewById(eq(R.id.distance))).thenReturn(vDistance);
        when(mockActivity.findViewById(eq(R.id.distance_value))).thenReturn(tvDistance);
        when(mockActivity.findViewById(eq(R.id.distance_label))).thenReturn(tvDistanceUnit);

        // Separator
        TextView tvSeparator = Mockito.mock(TextView.class);
        when(mockActivity.findViewById(eq(R.id.separator))).thenReturn(tvSeparator);

        PowerMockito.mockStatic(DistanceUtil.class);
        DistanceUtil mockDistanceUtil = PowerMockito.mock(DistanceUtil.class);
        when(DistanceUtil.getInstance(any(Context.class))).thenReturn(mockDistanceUtil);
        when(mockDistanceUtil.getUnitStr()).thenReturn("km");

        // call target method, heart rate and location available
        int flags = MainViewController.HEARTRATE_AVAILABLE | MainViewController.LOCATION_AVAILABLE;
        mainViewController.initialize(mockActivity, flags);

        // Prepare values
        // 32:02
        long elapsed = (32 * 60 * 1000)+ (2 * 1000);
        this.mainViewController.setSplitTime(elapsed);
        int stepCount = 3451;
        this.mainViewController.setStepCount(stepCount);
        int heartRate = 59;
        this.mainViewController.setHeartRate(heartRate);
        float distance = 2031;
        this.mainViewController.setDistance(distance);

        // invoke method
        PowerMockito.mockStatic(DateUtils.class);
        when(DateUtils.formatElapsedTime(eq(elapsed / 1000))).thenReturn("32:02");
        when(mockDistanceUtil.getDistanceStr(any(float.class))).thenReturn("2.03");

        Method method = MainViewController.class.getDeclaredMethod("refreshViewOnUiThread");
        method.setAccessible(true);
        method.invoke(this.mainViewController);

        // verify
        Mockito.verify(tvSplitTime).setText("32:02");
        Mockito.verify(tvStepCount).setText("3451");
        Mockito.verify(tvHeartRate).setText("59");
        Mockito.verify(tvDistance).setText("2.03");
        Mockito.verify(this.mockNotificationController).show(eq(elapsed));

    }

}