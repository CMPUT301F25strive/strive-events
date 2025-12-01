package com.example.eventlottery;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.Manifest;
import android.view.View;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.PerformException;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.espresso.util.HumanReadables;
import androidx.test.espresso.util.TreeIterables;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.example.eventlottery.data.RepositoryProvider;

/**
 * Very small UI flow that covers registration and bottom navigation.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class BasicEntrantFlowTest {

    @Rule
    public final GrantPermissionRule permissionRule =
            GrantPermissionRule.grant(Manifest.permission.POST_NOTIFICATIONS);

    private TestProfileRepository profileRepository;

    @Before
    public void setupRepository() {
        profileRepository = new TestProfileRepository();
        RepositoryProvider.setProfileRepositoryForTesting(profileRepository);
    }

    @After
    public void resetRepository() {
        RepositoryProvider.resetForTesting();
    }

    @Test
    public void registerNavigateToMyEventsThenProfile() {
        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class);
        try {
            onView(isRoot()).perform(waitForViewToBeVisible(R.id.etName, 5_000));
            onView(withId(R.id.etName)).perform(replaceText("UITester"), closeSoftKeyboard());
            onView(withId(R.id.etEmail)).perform(replaceText("uitester@example.com"), closeSoftKeyboard());
            onView(withId(R.id.etPhone)).perform(replaceText("5550101"), closeSoftKeyboard());
            onView(withId(R.id.btnMainAction)).perform(click());

            onView(withId(R.id.eventRecycler)).check(matches(isDisplayed()));

            onView(withId(R.id.nav_my_events)).perform(click());
            onView(withId(R.id.myEventsToggleGroup)).check(matches(isDisplayed()));

            onView(withId(R.id.nav_profile)).perform(click());
            onView(withId(R.id.profileName)).check(matches(isDisplayed()));
        } finally {
            scenario.close();
        }
    }

    private static ViewAction waitForViewToBeVisible(int viewId, long millis) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isRoot();
            }

            @Override
            public String getDescription() {
                return "wait for view with id " + viewId + " for " + millis + " millis.";
            }

            @Override
            public void perform(UiController uiController, View rootView) {
                long startTime = System.currentTimeMillis();
                long endTime = startTime + millis;

                do {
                    for (View child : TreeIterables.breadthFirstViewTraversal(rootView)) {
                        if (child.getId() == viewId && child.getVisibility() == View.VISIBLE) {
                            return;
                        }
                    }

                    uiController.loopMainThreadForAtLeast(50);
                } while (System.currentTimeMillis() < endTime);

                throw new PerformException.Builder()
                        .withActionDescription(getDescription())
                        .withViewDescription(HumanReadables.describe(rootView))
                        .build();
            }
        };
    }
}
