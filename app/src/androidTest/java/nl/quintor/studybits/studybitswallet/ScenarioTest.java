package nl.quintor.studybits.studybitswallet;

import android.Manifest;
import android.support.test.espresso.matcher.RootMatchers;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.rule.GrantPermissionRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;
import android.view.WindowManager;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.core.AllOf.allOf;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class ScenarioTest {

    @Rule
    public ActivityTestRule<MainActivity> mainActivityRule = new ActivityTestRule<>(MainActivity.class);

    @Rule
    public GrantPermissionRule grantPermissionRule = GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE);

    @Before
    public void unlockScreen() {
        Log.d("STUDYBITS", "@Before/start");
        MainActivity activity = mainActivityRule.getActivity();
        Runnable wakeUpDevice = new Runnable() {
            public void run() {
                Log.d("STUDYBITS", "@Before/runnable");
                activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        };
        activity.runOnUiThread(wakeUpDevice);
        Log.d("STUDYBITS", "@Before/end");
    }

    @Test
    public void fullScenarioTest() {
        Log.d("STUDYBITS", "Starting test");
        // Reset
        onView(withId(R.id.fab))
                .inRoot(RootMatchers.isFocusable())
                .perform(click());
        Log.d("STUDYBITS", "Clicked reset");

        onView(allOf(withId(android.support.design.R.id.snackbar_text), withText("Successfully reset")))
                .check(matches(isDisplayed()));

        Log.d("STUDYBITS", "Successfully reset");

        // Navigate to universities

        onView(withId(R.id.button_university))
                .perform(click());

        onView(withId(R.id.university_fab))
                .check(matches(isDisplayed()));

        // Open university dialog

        onView(withId(R.id.university_fab))
                .perform(click());

        onView(withId(R.id.university_endpoint_text))
                .check(matches(isDisplayed()));

        // Enter text
        onView(withId(R.id.university_endpoint_text))
                .perform(typeText(TestConfiguration.ENDPOINT_RUG));

        onView(withId(R.id.student_id_text))
                .perform(typeText("12345678"));

        // Click connect
        onView(withText(R.string.connect))
                .perform(click());

        // Check connection to university
        onView(withText("Rijksuniversiteit Groningen"))
                .check(matches(isDisplayed()));

        // Navigate to credentials
        pressBack();
        onView(withId(R.id.button_credential))
                .perform(click());

        // Check presence of credential
        onView(withText("Rijksuniversiteit Groningen"))
                .check(matches(isDisplayed()));

        // Accept credential
        onView(withText("Rijksuniversiteit Groningen"))
                .perform(click());

        // Navigate to universities
        pressBack();
        onView(withId(R.id.button_university))
                .perform(click());

        onView(withId(R.id.university_fab))
                .check(matches(isDisplayed()));

        // Open university dialog
        onView(withId(R.id.university_fab))
                .perform(click());

        onView(withId(R.id.university_endpoint_text))
                .check(matches(isDisplayed()));

        // Enter text
        onView(withId(R.id.university_endpoint_text))
                .perform(typeText(TestConfiguration.ENDPOINT_GENT));

        // Click connect
        onView(withText(R.string.connect))
                .perform(click());

        // Check connection to university
        onView(withText("Universiteit Gent"))
                .check(matches(isDisplayed()));


        // Navigate to exchange positions
        pressBack();
        onView(withId(R.id.button_exchange_position))
                .perform(click());

        // Accept exchange position
        onView(withText("MSc Marketing"))
                .check(matches(isDisplayed()))
                .perform(click());

        onView(withText("Send"))
                .check(matches(isDisplayed()))
                .perform(click());

        // Check result
        onView(allOf(withId(android.support.design.R.id.snackbar_text), withText("You're going abroad!")))
                .check(matches(isDisplayed()));
    }
}
