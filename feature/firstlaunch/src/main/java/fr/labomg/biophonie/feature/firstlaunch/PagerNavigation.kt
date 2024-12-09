@file:OptIn(ExperimentalFoundationApi::class)

package fr.labomg.biophonie.feature.firstlaunch

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import fr.labomg.biophonie.core.ui.theme.spacing
import fr.labomg.biophonie.feature.firstlaunch.Constants.NAVIGATION_DOT_SIZE
import kotlinx.coroutines.launch

@Composable
fun VerticalPagerNavigation(
    modifier: Modifier = Modifier,
    pagerState: PagerState = rememberPagerState(pageCount = { Constants.PAGE_NUMBER })
) {
    Row(
        modifier.fillMaxWidth().padding(MaterialTheme.spacing.small),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        SkipButton(pagerState, Modifier.weight(1f))
        VerticalPagerIndicator(pagerState)
        ContinueButton(pagerState, Modifier.weight(1f))
    }
}

@Composable
fun HorizontalPagerNavigation(
    modifier: Modifier = Modifier,
    pagerState: PagerState = rememberPagerState(pageCount = { Constants.PAGE_NUMBER })
) {
    Column(
        modifier.fillMaxHeight().padding(MaterialTheme.spacing.small),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SkipButton(pagerState, Modifier.weight(1f))
        HorizontalPagerIndicator(pagerState)
        ContinueButton(pagerState, Modifier.weight(1f))
    }
}

@Composable
fun ContinueButton(pagerState: PagerState, modifier: Modifier = Modifier) {
    val coroutineScope = rememberCoroutineScope()
    NavigationButton(
        text = stringResource(R.string.next),
        onClick = {
            coroutineScope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
        },
        modifier = modifier.alpha(if (!pagerState.isLastPage()) 1f else 0f)
    )
}

@Composable
fun SkipButton(pagerState: PagerState, modifier: Modifier = Modifier) {
    val coroutineScope = rememberCoroutineScope()
    NavigationButton(
        text = stringResource(R.string.skip),
        onClick = { coroutineScope.launch { pagerState.scrollToPage(pagerState.pageCount) } },
        modifier = modifier.alpha(if (!pagerState.isLastPage()) 1f else 0f)
    )
}

@Composable
fun NavigationButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(onClick = onClick, shape = RectangleShape, modifier = modifier) { Text(text) }
}

@Composable
fun VerticalPagerIndicator(pagerState: PagerState, modifier: Modifier = Modifier) {
    Row(modifier = modifier.padding(horizontal = MaterialTheme.spacing.small)) { Dots(pagerState) }
}

@Composable
fun HorizontalPagerIndicator(pagerState: PagerState, modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(vertical = MaterialTheme.spacing.small)) { Dots(pagerState) }
}

@Composable
fun Dots(pagerState: PagerState, modifier: Modifier = Modifier) {
    repeat(pagerState.pageCount) { pageNumber ->
        val color =
            if (pagerState.currentPage == pageNumber) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.inversePrimary
        Box(
            modifier =
                modifier
                    .padding(MaterialTheme.spacing.extraSmall)
                    .clip(CircleShape)
                    .background(color)
                    .size(NAVIGATION_DOT_SIZE.dp)
        )
    }
}

private fun PagerState.isLastPage(): Boolean = currentPage == pageCount - 1

@PreviewLightDark
@Composable
private fun VerticalPagerNavigationPreview() {
    FirstLaunchPreview {
        VerticalPagerNavigation(pagerState = rememberPagerState(1) { Constants.PAGE_NUMBER })
    }
}

@Preview
@Composable
private fun HorizontalPagerNavigationPreview() {
    FirstLaunchPreview {
        HorizontalPagerNavigation(pagerState = rememberPagerState(1) { Constants.PAGE_NUMBER })
    }
}
