package fr.labomg.biophonie.feature.firstlaunch

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.graphics.ExperimentalAnimationGraphicsApi
import androidx.compose.animation.graphics.res.animatedVectorResource
import androidx.compose.animation.graphics.res.rememberAnimatedVectorPainter
import androidx.compose.animation.graphics.vector.AnimatedImageVector
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fr.labomg.biophonie.core.ui.compose.OutlinedFloatingActionButton
import fr.labomg.biophonie.core.ui.compose.RectangleButton
import fr.labomg.biophonie.core.ui.compose.SurfacePreview
import fr.labomg.biophonie.core.ui.theme.spacing
import fr.labomg.biophonie.feature.firstlaunch.Constants.ANIMATION_DURATION
import fr.labomg.biophonie.feature.firstlaunch.Constants.GRADIENT_END_COLOR_STOP
import fr.labomg.biophonie.feature.firstlaunch.Constants.GRADIENT_START_COLOR_STOP
import fr.labomg.biophonie.feature.firstlaunch.Constants.RECORD_FAB_SIZE
import kotlinx.coroutines.delay

@Composable
@OptIn(ExperimentalAnimationGraphicsApi::class)
fun MapExplanation(modifier: Modifier = Modifier) {
    val image = AnimatedImageVector.animatedVectorResource(R.drawable.tuto_map_animated)
    var atEnd by remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.padding(MaterialTheme.spacing.large)
    ) {
        Image(
            painter = rememberAnimatedVectorPainter(image, atEnd),
            contentDescription = null,
            modifier = Modifier.weight(1f).fillMaxWidth()
        )
        LaunchedEffect(Unit) { atEnd = true }
        ExplanationText(
            textId = R.string.firstlaunch_map_description,
            modifier =
                Modifier.padding(
                    top = MaterialTheme.spacing.small,
                    start = MaterialTheme.spacing.small,
                    end = MaterialTheme.spacing.small
                )
        )
    }
}

@Composable
fun ListeningExplanation(modifier: Modifier = Modifier) {
    var shown by remember { mutableStateOf(false) }
    val transition = updateTransition(targetState = shown, label = "")
    val reveal by
        transition.animateFloat(
            transitionSpec = {
                tween(durationMillis = ANIMATION_DURATION, easing = FastOutLinearInEasing)
            },
            label = ""
        ) {
            if (it) 1f else 0f
        }
    LaunchedEffect(Unit) { shown = true }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.padding(MaterialTheme.spacing.large)
    ) {
        Box(modifier = Modifier.weight(1f)) {
            Image(
                painter = painterResource(id = R.drawable.tuto_amplitudes),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
                contentDescription = null,
                contentScale = ContentScale.FillHeight,
                alignment = Alignment.CenterStart,
                modifier =
                    Modifier.fillMaxHeight().drawWithContent {
                        clipRect(right = size.width * reveal) { this@drawWithContent.drawContent() }
                    }
            )
            OutlinedFloatingActionButton(
                drawableId = R.drawable.ic_play,
                contentDesc = R.string.play,
                modifier = Modifier.align(Alignment.Center)
            )
        }
        ExplanationText(
            textId = R.string.firstlaunch_listening_description,
            modifier = Modifier.padding(top = MaterialTheme.spacing.small)
        )
    }
}

@Composable
@OptIn(ExperimentalAnimationGraphicsApi::class)
fun LocationExplanation(modifier: Modifier = Modifier) {
    val backgroundColor = MaterialTheme.colorScheme.surface
    val image = AnimatedImageVector.animatedVectorResource(R.drawable.tuto_localize_animated)
    var atEnd by remember { mutableStateOf(false) }

    Box(
        contentAlignment = Alignment.Center,
        modifier =
            modifier.drawWithContent {
                this@drawWithContent.drawContent()
                drawRect(
                    Brush.radialGradient(
                        colorStops =
                            arrayOf(
                                GRADIENT_START_COLOR_STOP to Color.Transparent,
                                GRADIENT_END_COLOR_STOP to backgroundColor
                            )
                    )
                )
            }
    ) {
        Image(
            painter = rememberAnimatedVectorPainter(image, atEnd),
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.tertiary),
            contentDescription = null,
            modifier = Modifier.matchParentSize()
        )
        LaunchedEffect(Unit) {
            delay(300)
            atEnd = true
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(MaterialTheme.spacing.extraLarge)
        ) {
            LocationDecoration(
                Modifier.weight(2f)
                    .fillMaxWidth()
                    .padding(horizontal = MaterialTheme.spacing.medium)
            )
            ExplanationText(
                textId = R.string.firstlaunch_location_description,
                modifier = Modifier.weight(1f).padding(top = MaterialTheme.spacing.small)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UnclickableFab(
    @DrawableRes drawableId: Int,
    @StringRes contentDesc: Int,
    modifier: Modifier = Modifier
) {
    CompositionLocalProvider(LocalRippleConfiguration provides null) {
        OutlinedFloatingActionButton(
            drawableId = drawableId,
            contentDesc = contentDesc,
            modifier = modifier
        )
    }
}

@Composable
private fun LocationDecoration(modifier: Modifier = Modifier) {
    Box(modifier) {
        UnclickableFab(
            drawableId = R.drawable.ic_baseline_location_searching,
            contentDesc = R.string.locate,
            modifier = Modifier.align(Alignment.TopStart)
        )
        VerticalDivider(
            color = MaterialTheme.colorScheme.secondary,
            thickness = 2.dp,
            modifier = Modifier.rotate(30f).align(Alignment.Center)
        )
        UnclickableFab(
            drawableId = R.drawable.trip,
            contentDesc = R.string.trip,
            modifier = Modifier.align(Alignment.BottomEnd)
        )
    }
}

@OptIn(ExperimentalAnimationGraphicsApi::class)
@Composable
fun RecordExplanation(modifier: Modifier = Modifier) {
    val image = AnimatedImageVector.animatedVectorResource(R.drawable.tuto_record_animated)
    var atEnd by remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.padding(MaterialTheme.spacing.extraLarge)
    ) {
        Box(modifier = Modifier.weight(1f)) {
            Image(
                painter = rememberAnimatedVectorPainter(image, atEnd),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant),
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
            LaunchedEffect(Unit) { atEnd = true }
            UnclickableFab(
                drawableId = R.drawable.ic_mic,
                contentDesc = R.string.record,
                modifier = Modifier.align(Alignment.Center).size(RECORD_FAB_SIZE.dp)
            )
        }
        ExplanationText(textId = R.string.firstlaunch_record_description)
    }
}

@Composable
private fun ExplanationText(@StringRes textId: Int, modifier: Modifier = Modifier) {
    Text(
        text = AnnotatedString.fromHtml(stringResource(textId)),
        textAlign = TextAlign.Center,
        modifier = modifier
    )
}

@Composable
fun NameContent(modifier: Modifier = Modifier, viewModel: TutorialViewModel = hiltViewModel()) {
    val nameUiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.padding(MaterialTheme.spacing.large).fillMaxSize()
    ) {
        OutlinedTextField(
            value = viewModel.name,
            onValueChange = viewModel::updateName,
            isError = nameUiState.isNameInvalid,
            supportingText = { nameUiState.supportingText.ToText() },
            colors =
                OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.secondaryContainer
                ),
            singleLine = true,
            label = { Text(stringResource(R.string.name)) },
            keyboardActions =
                KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        viewModel.submit()
                    }
                ),
            keyboardOptions =
                KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done,
                    capitalization = KeyboardCapitalization.Sentences
                )
        )
        ExplanationText(R.string.firstlaunch_name_description)
        RectangleButton(
            onClick = viewModel::submit,
            modifier = Modifier.padding(top = MaterialTheme.spacing.medium)
        ) {
            Text(text = stringResource(R.string.validation))
        }
    }
}

@Preview(heightDp = 400)
@Composable
private fun MapExplanationPreview() {
    SurfacePreview { MapExplanation() }
}

@Preview(heightDp = 400)
@Composable
private fun ListeningExplanationPreview() {
    SurfacePreview { ListeningExplanation() }
}

@Preview(heightDp = 400)
@Composable
private fun LocationExplanationPreview() {
    SurfacePreview { LocationExplanation() }
}

@Preview(heightDp = 400)
@Composable
private fun RecordExplanationPreview() {
    SurfacePreview { RecordExplanation() }
}
