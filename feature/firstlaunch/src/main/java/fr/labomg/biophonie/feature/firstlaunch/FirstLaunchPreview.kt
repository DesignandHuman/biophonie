package fr.labomg.biophonie.feature.firstlaunch

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import fr.labomg.biophonie.core.ui.theme.AppTheme

@Composable
fun FirstLaunchPreview(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    AppTheme {
        Surface(color = MaterialTheme.colorScheme.primary, modifier = modifier) { content() }
    }
}
