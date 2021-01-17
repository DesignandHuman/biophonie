package com.example.biophonie.ui

import android.view.InputDevice
import android.view.MotionEvent
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.CoordinatesProvider
import androidx.test.espresso.action.GeneralClickAction
import androidx.test.espresso.action.Press
import androidx.test.espresso.action.Tap
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.example.biophonie.R
import com.example.biophonie.ui.activities.MapActivity

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Rule

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class MapActivityTest {

    @get:Rule
    var activityRule: ActivityScenarioRule<MapActivity>
            = ActivityScenarioRule(MapActivity::class.java)

    @Test
    fun deploy_bottomPlayer() {
        onView(withId(R.id.containerMap)).perform(
            clickPercent(
                0.5F,
                0.5F
            )
        )
        Thread.sleep(200)
        onView(withId(R.id.container)).check(matches(isDisplayed()))
    }

    @Test
    fun deploy_about(){
        onView(withId(R.id.about)).perform(click())
        onView(withId(R.id.topPanel)).check(matches(isDisplayed()))
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
                MotionEvent.BUTTON_PRIMARY)
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
                MotionEvent.BUTTON_PRIMARY)
        }
    }
}