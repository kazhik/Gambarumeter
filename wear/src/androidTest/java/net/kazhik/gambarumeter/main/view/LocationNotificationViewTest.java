package net.kazhik.gambarumeter.main.view;

import android.test.InstrumentationTestCase;

import java.lang.reflect.Method;

/**
 * Created by kazhik on 15/01/10.
 */
public class LocationNotificationViewTest extends InstrumentationTestCase {

    public void testMakeSummaryText() throws Exception {

        LocationNotificationView notificationView = new LocationNotificationView();


        notificationView.initialize(this.getInstrumentation().getTargetContext().getApplicationContext());

        notificationView.setDistanceUnit("metre");

        notificationView.updateStepCount(445);
        notificationView.updateDistance(2300);
        notificationView.updateLap(10 * 60 * 1000);

        Method makeSummaryText =
                notificationView.getClass().getSuperclass().getDeclaredMethod("makeSummaryText",
                        long.class);
        makeSummaryText.setAccessible(true);
        Object[] parameters = new Object[]{45 * 1000};

        String text = (String)makeSummaryText.invoke(notificationView, parameters);

        assertEquals("00:45 2.3km", text);
        System.out.println(text);

    }

    public void testMakeDetailedText() throws Exception {

        LocationNotificationView notificationView = new LocationNotificationView();


        notificationView.initialize(this.getInstrumentation().getTargetContext().getApplicationContext());

        notificationView.setDistanceUnit("metre");

        notificationView.updateStepCount(445);
        notificationView.updateDistance(2300);
        notificationView.updateLap(((4 * 60) + 30) * 1000);

        Method makeDetailedText =
                notificationView.getClass().getSuperclass().getDeclaredMethod("makeDetailedText");
        makeDetailedText.setAccessible(true);

        String text = (String)makeDetailedText.invoke(notificationView);

        assertEquals("445歩 04:30/km", text);
        System.out.println(text);

    }

}
