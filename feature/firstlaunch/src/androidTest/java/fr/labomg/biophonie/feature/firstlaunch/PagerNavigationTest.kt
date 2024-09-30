package fr.labomg.biophonie.feature.firstlaunch

import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.ui.test.isNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import junit.framework.TestCase.assertEquals
import org.junit.Rule
import org.junit.Test

class PagerNavigationTest {

    @get:Rule val rule = createComposeRule()

    @Test
    fun clickSkip_goesToLast() {
        lateinit var pagerState: PagerState
        rule.setContent {
            pagerState = rememberPagerState { 4 }
            HorizontalPager(state = pagerState) {}
            VerticalPagerNavigation(pagerState = pagerState)
        }
        rule.onNodeWithText("Skip").performClick()
        assertEquals(3, pagerState.currentPage)
        rule.onNodeWithText("Skip").isNotDisplayed()
        rule.onNodeWithText("Start").isNotDisplayed()
    }

    @Test
    fun clickNext_goesToNext() {
        lateinit var pagerState: PagerState
        rule.setContent {
            pagerState = rememberPagerState(1) { 4 }
            HorizontalPager(state = pagerState) {}
            VerticalPagerNavigation(pagerState = pagerState)
        }
        rule.onNodeWithText("Next").performClick()
        rule.waitForIdle()
        assertEquals(2, pagerState.currentPage)
    }
}
