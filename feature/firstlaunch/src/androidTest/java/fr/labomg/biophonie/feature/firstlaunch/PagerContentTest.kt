package fr.labomg.biophonie.feature.firstlaunch

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import fr.labomg.biophonie.core.testing.EmptyTestActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@HiltAndroidTest
@RunWith(Parameterized::class)
class PagerContentTest(
    private val description: String,
    private val pagerContent: @Composable () -> Unit
) {

    @get:Rule(order = 0) var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1) val composeRule = createAndroidComposeRule<EmptyTestActivity>()

    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun data(): Collection<Array<Any>> {
            val catalogue: Map<String, @Composable () -> Unit> =
                mapOf(
                    "Browse on the Biophonie map and listen to soundscapes." to
                        {
                            MapExplanation()
                        },
                    "Click to listen to an ambient sound." to { ListeningExplanation() },
                    "Geolocate yourself to listen to the soundscapes around you." to
                        {
                            LocationExplanation()
                        },
                    "Register your own soundscapes on the map!" to { RecordExplanation() },
                    "At Labo.mg we like privacy. We'll only ask for your name :)" to
                        {
                            NameContent()
                        },
                )

            return catalogue.map { arrayOf(it.key, it.value) }
        }
    }

    @Test
    fun pagerContent_descriptionIsVisible() {
        composeRule.setContent { pagerContent() }
        composeRule.onNodeWithText(description).assertIsDisplayed()
    }
}
