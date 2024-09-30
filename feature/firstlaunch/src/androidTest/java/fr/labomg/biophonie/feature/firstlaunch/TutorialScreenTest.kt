package fr.labomg.biophonie.feature.firstlaunch

import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToIndex
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import fr.labomg.biophonie.core.testing.EmptyTestActivity
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class TutorialScreenTest {

    @get:Rule(order = 0) var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1) val composeRule = createAndroidComposeRule<EmptyTestActivity>()

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun scrollTutorial_goesToNameContent() {
        composeRule.setContent { TutorialScreen() }
        composeRule.onNode(hasScrollAction()).performScrollToIndex(4)
        composeRule.onNodeWithText("Name").assertExists()
    }
}
