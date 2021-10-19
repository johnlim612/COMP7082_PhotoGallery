package com.comp7082.photogallery;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.comp7082.photogallery.View.MainActivity;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);
    @Test
    public void photoSearchTest() {
        onView(withId(R.id.btnSearch)).perform(click());
        onView(withId(R.id.etFromDateTime)).perform(typeText(""), closeSoftKeyboard());
        onView(withId(R.id.etToDateTime)).perform(typeText(""), closeSoftKeyboard());
        onView(withId(R.id.etKeywords)).perform(typeText("caption"), closeSoftKeyboard());
        onView(withId(R.id.go)).perform(click());
//        onView(withId(R.id.etCaption)).check(matches(withText("caption")));
        onView(withId(R.id.btnNext)).perform(click());
        onView(withId(R.id.btnPrev)).perform(click());
    }
}