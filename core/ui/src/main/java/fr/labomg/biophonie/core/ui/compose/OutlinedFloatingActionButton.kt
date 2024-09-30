package fr.labomg.biophonie.core.ui.compose

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
fun OutlinedFloatingActionButton(
    @DrawableRes drawableId: Int,
    @StringRes contentDesc: Int,
    modifier: Modifier = Modifier
) {
    FloatingActionButton(
        onClick = {},
        containerColor = MaterialTheme.colorScheme.background,
        shape = CircleShape,
        modifier =
            modifier
                .width(IntrinsicSize.Max)
                .height(IntrinsicSize.Max)
                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
    ) {
        Image(
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
            painter = painterResource(id = drawableId),
            contentDescription = stringResource(id = contentDesc),
            modifier = Modifier.fillMaxSize().padding(10.dp)
        )
    }
}
