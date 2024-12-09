package fr.labomg.biophonie.feature.exploregeopoints

import androidx.compose.animation.graphics.ExperimentalAnimationGraphicsApi
import androidx.compose.animation.graphics.res.animatedVectorResource
import androidx.compose.animation.graphics.res.rememberAnimatedVectorPainter
import androidx.compose.animation.graphics.vector.AnimatedImageVector
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import fr.labomg.biophonie.core.ui.compose.OutlinedFloatingActionButton
import fr.labomg.biophonie.core.ui.theme.size
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

private const val TRANSITION_ANIMATION_DELAY = 200

@OptIn(ExperimentalAnimationGraphicsApi::class)
@Composable
fun RecordButton(onClick: () -> Unit, modifier: Modifier = Modifier, isRecording: Boolean = false) {
    var animationEnd by remember { mutableStateOf(false) }
    val image = AnimatedImageVector.animatedVectorResource(R.drawable.loading_rec)
    val enabledOnClick =
        remember(isRecording) {
            if (!isRecording) onClick
            else {
                {}
            }
        }

    LaunchedEffect(isRecording) {
        if (isRecording) {
            try {
                while (isActive) {
                    animationEnd = !animationEnd
                    delay(image.totalDuration.toLong() + TRANSITION_ANIMATION_DELAY)
                }
            } finally {
                animationEnd = false
            }
        }
    }

    OutlinedFloatingActionButton(
        onClick = enabledOnClick,
        painter = rememberAnimatedVectorPainter(animatedImageVector = image, atEnd = animationEnd),
        contentDesc = if (isRecording) R.string.searching_location else R.string.record,
        modifier = modifier.size(MaterialTheme.size.extraLarge)
    )
}
