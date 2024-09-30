package fr.labomg.biophonie.feature.firstlaunch

data class NameUiState(
    val isNameInvalid: Boolean = false,
    val supportingText: NameError = NameError.EMPTY
)
