package com.example.eventlottery;
import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.anything;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.action.ViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.GrantPermissionRule;

import com.example.eventlottery.entrant.EntrantEventListFragment;
import com.example.eventlottery.entrant.EventListAdapter;
import com.example.eventlottery.entrant.MyEventsFragment;
import com.example.eventlottery.entrant.ProfileFragment;

import org.junit.Rule;
import org.junit.Test;
import android.Manifest;

import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.junit.rules.TestRule;


public class MainActivityTest {
    public ActivityScenarioRule<MainActivity> scenario = new
            ActivityScenarioRule<MainActivity>(MainActivity.class);

    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(
            android.Manifest.permission.POST_NOTIFICATIONS);

    @Rule
    public RuleChain chain = RuleChain
            .outerRule(permissionRule)
            .around(scenario);
    @Test
    public void openApp(){
        onView(withId(R.id.appNameText)).check(matches(isDisplayed()));
    }


}
