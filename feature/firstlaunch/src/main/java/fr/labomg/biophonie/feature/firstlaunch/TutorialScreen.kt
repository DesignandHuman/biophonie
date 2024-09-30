@file:OptIn(ExperimentalFoundationApi::class)

package fr.labomg.biophonie.feature.firstlaunch

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.tooling.preview.PreviewFontScale
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.DpSize
import fr.labomg.biophonie.core.ui.compose.Header
import fr.labomg.biophonie.core.ui.theme.spacing

@Composable
fun TutorialScreen(
    modifier: Modifier = Modifier,
    windowWidth: WindowWidthSizeClass = WindowWidthSizeClass.Compact
) {
    when (windowWidth) {
        WindowWidthSizeClass.Compact -> VerticalTutorialScreen(modifier)
        else -> HorizontalTutorialScreen(modifier)
    }
}

@Composable
fun VerticalTutorialScreen(modifier: Modifier = Modifier) {
    val pagerState = rememberPagerState(pageCount = { Constants.PAGE_NUMBER })
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Header(Modifier.fillMaxWidth().padding(vertical = MaterialTheme.spacing.medium))
        HorizontalPager(
            state = pagerState,
            modifier =
                Modifier.weight(1f)
                    .padding(horizontal = MaterialTheme.spacing.small)
                    .aspectRatio(1f)
                    .clip(CircleShape)
                    .fillMaxWidth()
        ) {
            Surface(color = MaterialTheme.colorScheme.surface) {
                PagerContent(pagerState.currentPage)
            }
        }
        VerticalPagerNavigation(pagerState = pagerState)
    }
}

@Composable
fun HorizontalTutorialScreen(modifier: Modifier = Modifier) {
    val pagerState = rememberPagerState(pageCount = { Constants.PAGE_NUMBER })
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround,
        modifier = modifier.fillMaxHeight()
    ) {
        Header(Modifier.padding(horizontal = MaterialTheme.spacing.medium).weight(0.5f))
        VerticalPager(
            state = pagerState,
            modifier =
                Modifier.fillMaxHeight()
                    .weight(1f)
                    .padding(MaterialTheme.spacing.medium)
                    .aspectRatio(1f, true)
                    .clip(CircleShape)
        ) {
            Surface(color = MaterialTheme.colorScheme.surface) {
                PagerContent(pagerState.currentPage)
            }
        }
        HorizontalPagerNavigation(pagerState = pagerState)
    }
}

@Composable
fun PagerContent(page: Int, modifier: Modifier = Modifier) {
    val keyboardController = LocalSoftwareKeyboardController.current
    keyboardController?.hide()
    when (page) {
        0 -> MapExplanation(modifier)
        1 -> ListeningExplanation(modifier)
        2 -> LocationExplanation(modifier)
        3 -> RecordExplanation(modifier)
        else -> NameContent(modifier)
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@PreviewLightDark
@PreviewScreenSizes
@PreviewFontScale
@Composable
private fun TutorialScreenPreview() {
    BoxWithConstraints {
        val windowSize = WindowSizeClass.calculateFromSize(DpSize(maxWidth, maxHeight))
        FirstLaunchPreview { TutorialScreen(windowWidth = windowSize.widthSizeClass) }
    }
}
