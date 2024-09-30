package fr.labomg.biophonie.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import fr.labomg.biophonie.core.ui.R

val bodyFontFamily =
    FontFamily(
        listOf(
            Font(resId = R.font.ibm_plex_mono_text),
            Font(resId = R.font.ibm_plex_mono_bold, weight = FontWeight.Bold),
        )
    )

val displayFontFamily =
    FontFamily(
        listOf(
            Font(resId = R.font.ibm_plex_mono),
            Font(resId = R.font.ibm_plex_mono_bold, weight = FontWeight.Bold),
            Font(resId = R.font.ibm_plex_mono_italic, style = FontStyle.Italic),
        )
    )

// Default Material 3 typography values
val baseline = Typography()

val typography =
    Typography(
        displayLarge = baseline.displayLarge.copy(fontFamily = displayFontFamily),
        displayMedium = baseline.displayMedium.copy(fontFamily = displayFontFamily),
        displaySmall = baseline.displaySmall.copy(fontFamily = displayFontFamily),
        headlineLarge =
            baseline.headlineLarge.copy(
                fontFamily = displayFontFamily,
                fontStyle = FontStyle.Italic
            ),
        headlineMedium =
            baseline.headlineMedium.copy(
                fontFamily = displayFontFamily,
                fontStyle = FontStyle.Italic
            ),
        headlineSmall =
            baseline.headlineSmall.copy(
                fontFamily = displayFontFamily,
                fontStyle = FontStyle.Italic
            ),
        titleLarge = baseline.titleLarge.copy(fontFamily = displayFontFamily),
        titleMedium = baseline.titleMedium.copy(fontFamily = displayFontFamily),
        titleSmall = baseline.titleSmall.copy(fontFamily = displayFontFamily),
        bodyLarge = baseline.bodyLarge.copy(fontFamily = bodyFontFamily),
        bodyMedium = baseline.bodyMedium.copy(fontFamily = bodyFontFamily),
        bodySmall = baseline.bodySmall.copy(fontFamily = bodyFontFamily),
        labelLarge = baseline.labelLarge.copy(fontFamily = bodyFontFamily),
        labelMedium = baseline.labelMedium.copy(fontFamily = bodyFontFamily),
        labelSmall = baseline.labelSmall.copy(fontFamily = bodyFontFamily),
    )
