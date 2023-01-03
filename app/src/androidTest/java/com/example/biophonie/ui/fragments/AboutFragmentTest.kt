package com.example.biophonie.ui.fragments

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.swipeUp
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.biophonie.R
import com.example.biophonie.ui.fragments.AboutFragment
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AboutFragmentTest{
    @Test
    fun user_can_visit_laboMg(){
        launchFragmentInContainer<AboutFragment>()
        onView(withId(R.id.about_text)).perform(swipeUp())
        onView(withId(R.id.link)).check(matches(isDisplayed()))
    }
}