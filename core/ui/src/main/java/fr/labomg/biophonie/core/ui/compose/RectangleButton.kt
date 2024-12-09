package fr.labomg.biophonie.core.ui.compose

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.tooling.preview.PreviewLightDark

@Composable
fun RectangleButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    content: @Composable () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        shape = RectangleShape,
        contentPadding = contentPadding,
        modifier = modifier
    ) {
        content()
    }
}

@Composable
@PreviewLightDark
private fun RectangleButtonPreview() {
    SurfacePreview { RectangleButton(onClick = {}) { Text(text = "Click me !") } }
}
