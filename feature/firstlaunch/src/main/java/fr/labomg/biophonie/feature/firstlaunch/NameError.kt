package fr.labomg.biophonie.feature.firstlaunch

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource

enum class NameError {
    EMPTY,
    NOT_NULL,
    TOO_SHORT,
    SPECIAL_CHAR,
    NOT_UNIQUE,
    NETWORK_UNREACHABLE,
    SERVER_ERROR,
    UNKNOWN
}

@Composable
fun NameError.ToText(modifier: Modifier = Modifier) {
    val stringResource =
        when (this) {
            NameError.SPECIAL_CHAR -> R.string.special_characters_unallowed
            NameError.NOT_UNIQUE -> R.string.name_already_taken
            NameError.NETWORK_UNREACHABLE -> R.string.network_error
            NameError.NOT_NULL -> R.string.name_cannot_be_null
            NameError.TOO_SHORT -> R.string.name_should_be_longer
            NameError.SERVER_ERROR -> R.string.server_error
            NameError.UNKNOWN -> R.string.unknown_error
            NameError.EMPTY -> null
        }
    stringResource?.let { Text(stringResource(stringResource), modifier = modifier) }
}
