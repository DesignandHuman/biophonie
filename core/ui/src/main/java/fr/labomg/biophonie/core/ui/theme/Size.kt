package fr.labomg.biophonie.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class Size(
    val default: Dp = 40.dp,
    val extraSmall: Dp = 15.dp,
    val small: Dp = 20.dp,
    val medium: Dp = 40.dp,
    val large: Dp = 60.dp,
    val extraLarge: Dp = 70.dp
)

val LocalSize = staticCompositionLocalOf { Size() }

val MaterialTheme.size: Size
    @Composable @ReadOnlyComposable get() = LocalSize.current
