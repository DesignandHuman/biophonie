package fr.labomg.biophonie.feature.firstlaunch

import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasPerformImeAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import fr.labomg.biophonie.core.testing.HiltEmptyTestActivity
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class NameContentTest {

    @get:Rule(order = 0) var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1) val composeRule = createAndroidComposeRule<HiltEmptyTestActivity>()

    private lateinit var okString: String
    private lateinit var nameString: String
    private lateinit var specialCharError: String
    private lateinit var nameTooShort: String
    private lateinit var nameNotUnique: String
    private lateinit var inputField: SemanticsMatcher

    @Before
    fun init() {
        hiltRule.inject()
        composeRule.activity.apply {
            okString = getString(R.string.validation)
            nameString = getString(R.string.name)
            specialCharError = getString(R.string.special_characters_unallowed)
            nameTooShort = getString(R.string.name_should_be_longer)
            nameNotUnique = getString(R.string.name_already_taken)
        }
        inputField = hasText(nameString) and hasPerformImeAction()
    }

    @Test
    fun nameContent_enterName_filterSpecialCharacters() {
        composeRule.setContent { NameContent() }
        val specialCharacters = "}+*/&$1"
        for (char in specialCharacters) {
            composeRule.onNode(inputField).performTextInput("name$char")
            composeRule.onNodeWithText(specialCharError).assertExists()
            composeRule.onNode(inputField).performTextClearance()
        }
    }

    @Test
    fun name_Content_submitShortName_raisesError() {
        composeRule.setContent { NameContent() }
        composeRule.onNode(inputField).performTextInput("Al")
        composeRule.onNodeWithText(okString).performClick()
        composeRule.onNodeWithText(nameTooShort).assertExists()
        composeRule.onNode(inputField).performTextInput("i")
        composeRule.onNodeWithText(nameTooShort).assertDoesNotExist()
        composeRule.onNodeWithText(okString).performClick()
        composeRule.onNodeWithText(nameTooShort).assertDoesNotExist()
    }

    @Test
    fun name_Content_submitExistingName_raisesError() {
        composeRule.setContent { NameContent() }
        composeRule.onNode(inputField).performTextInput("Bob")
        composeRule.onNodeWithText(okString).performClick()
        composeRule.onNodeWithText(nameNotUnique).assertExists()
    }
}
