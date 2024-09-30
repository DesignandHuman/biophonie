package fr.labomg.biophonie.core.ui.compose

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import fr.labomg.biophonie.core.ui.theme.AppTheme

@Composable
fun SurfacePreview(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    AppTheme { Surface(modifier = modifier) { content() } }
}
