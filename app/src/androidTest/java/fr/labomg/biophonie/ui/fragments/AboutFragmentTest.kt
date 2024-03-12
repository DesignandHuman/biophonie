package fr.labomg.biophonie.ui.fragments

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.swipeUp
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import fr.labomg.biophonie.R
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AboutFragmentTest {
    @Test
    fun user_can_visit_laboMg() {
        launchFragmentInContainer<AboutFragment>()
        onView(withId(R.id.about_text)).perform(swipeUp())
        onView(withId(R.id.link)).check(matches(isDisplayed()))
    }
}
