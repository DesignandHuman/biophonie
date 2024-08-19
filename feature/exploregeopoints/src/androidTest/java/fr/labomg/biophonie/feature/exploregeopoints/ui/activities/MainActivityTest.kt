package fr.labomg.biophonie.feature.exploregeopoints.ui.activities

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.InputDevice
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.CoordinatesProvider
import androidx.test.espresso.action.GeneralClickAction
import androidx.test.espresso.action.Press
import androidx.test.espresso.action.Tap
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import fr.labomg.biophonie.MainActivity
import fr.labomg.biophonie.feature.exploregeopoints.R
import org.hamcrest.Matcher
import org.junit.Rule
import org.junit.Test
import org.junit.internal.matchers.TypeSafeMatcher
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class MainActivityTest {

    @get:Rule
    var activityRule: ActivityScenarioRule<fr.labomg.biophonie.MainActivity> =
        ActivityScenarioRule(fr.labomg.biophonie.MainActivity::class.java)

    @Test
    fun deploy_bottom_player() {
        onView(withId(R.id.location_fab)).check(matches(isDisplayed()))
        Thread.sleep(200)
        onView(withId(R.id.containerMap)).perform(clickPercent(0.355f, 0.438f))
        Thread.sleep(200)
        // todo move to bottomplayerfragmenttest
        // and to test source set
        onView(withId(R.id.location)).check(matches(withText("WithTemplate")))
        onView(withId(R.id.coordinates)).check(matches(withText("Dec 2022")))
        onView(withId(R.id.play)).perform(click())
        onView(withId(R.id.player_view)).perform(clickPercent(0.99f, 0.747f))
        onView(withId(R.id.duration)).check(matches(withText("00:00")))
        onView(withId(R.id.expand)).perform(click())
        onView(withId(R.id.sound_image)).check(matches(drawableIsCorrect(R.drawable.clearing)))
    }

    @Test
    fun get_location() {
        onView(withId(R.id.location_fab))
            .check(matches(withDrawableVector(R.drawable.ic_baseline_location_searching)))
        onView(withId(R.id.location_fab)).perform(click())
        Thread.sleep(10000)
        onView(withId(R.id.location_fab)).check(matches(withDrawableVector(R.drawable.ic_trip)))
        onView(withId(R.id.location_fab)).perform(click())
        deploy_bottom_player()
    }

    companion object {
        fun clickIn(x: Int, y: Int): ViewAction {
            return GeneralClickAction(
                Tap.SINGLE,
                CoordinatesProvider { view ->
                    val screenPos = IntArray(2)
                    view?.getLocationOnScreen(screenPos)

                    val screenX = (screenPos[0] + x).toFloat()
                    val screenY = (screenPos[1] + y).toFloat()

                    floatArrayOf(screenX, screenY)
                },
                Press.FINGER,
                InputDevice.SOURCE_MOUSE,
                MotionEvent.BUTTON_PRIMARY
            )
        }

        fun clickPercent(pctX: Float, pctY: Float): ViewAction {
            return GeneralClickAction(
                Tap.SINGLE,
                CoordinatesProvider { view ->
                    val screenPos = IntArray(2)
                    view?.getLocationOnScreen(screenPos)

                    val w = view.width
                    val h = view.height

                    val x = w * pctX
                    val y = h * pctY

                    val screenX = screenPos[0] + x
                    val screenY = screenPos[1] + y

                    floatArrayOf(screenX, screenY)
                },
                Press.FINGER,
                InputDevice.SOURCE_MOUSE,
                MotionEvent.BUTTON_PRIMARY
            )
        }
    }
}

fun drawableIsCorrect(@DrawableRes drawableResId: Int): Matcher<View> {
    return object : TypeSafeMatcher<View>() {
        override fun describeTo(description: org.hamcrest.Description?) {
            description?.appendText("with drawable from resource id: ")
            description?.appendValue(drawableResId)
        }

        override fun matchesSafely(target: View?): Boolean {
            return when {
                (target !is ImageView) -> false
                (drawableResId < 0) -> target.drawable == null
                (ContextCompat.getDrawable(target.context, drawableResId) == null) -> false
                else -> {
                    val expectedDrawable =
                        ContextCompat.getDrawable(target.context, drawableResId) as BitmapDrawable
                    val bitmap = (target.drawable as BitmapDrawable).bitmap
                    val otherBitmap = expectedDrawable.bitmap
                    bitmap.sameAs(otherBitmap)
                }
            }
        }
    }
}

@Suppress("DEPRECATION")
fun withDrawableVector(@DrawableRes drawableResId: Int): Matcher<View> {
    return object : TypeSafeMatcher<View>() {
        override fun describeTo(description: org.hamcrest.Description?) {
            description?.appendText("with drawable from resource id: ")
            description?.appendValue(drawableResId)
        }

        override fun matchesSafely(target: View?): Boolean {
            return when {
                (target !is ImageView) -> false
                (drawableResId < 0) -> target.drawable == null
                (ContextCompat.getDrawable(target.context, drawableResId) == null) -> false
                else -> {
                    val expectedDrawable =
                        ContextCompat.getDrawable(target.context, drawableResId) as Drawable
                    val bitmap =
                        Bitmap.createBitmap(
                            target.drawable.intrinsicWidth,
                            target.drawable.intrinsicHeight,
                            Bitmap.Config.ARGB_8888
                        )
                    val newExpectedDrawable =
                        Bitmap.createBitmap(
                            expectedDrawable.intrinsicWidth,
                            expectedDrawable.intrinsicHeight,
                            Bitmap.Config.ARGB_8888
                        )
                    return bitmap.sameAs(newExpectedDrawable)
                }
            }
        }
    }
}
