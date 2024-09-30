package fr.labomg.biophonie.core.ui.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import fr.labomg.biophonie.core.ui.R

@Composable
fun Header(modifier: Modifier = Modifier) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Image(
            painter = painterResource(id = R.drawable.ic_pine),
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimary),
            contentDescription = null
        )
        Text(
            text = stringResource(id = R.string.app_name),
            maxLines = 1,
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.requiredWidth(IntrinsicSize.Max)
        )
        Text(text = stringResource(id = R.string.subtitle), textAlign = TextAlign.Center)
    }
}

@Preview
@Composable
private fun HeaderPreview() {
    SurfacePreview { Header() }
}
