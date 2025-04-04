package com.instructure.horizon.horizonui.organisms.inputs.single_select

data class SingleSelectState(
    val label: String? = null,
    val helperText: String? = null,
    val placeHolderText: String? = null,
    val isFocused: Boolean = false,
    val isDisabled: Boolean = false,
    val isMenuOpen: Boolean = false,
    val errorText: String? = null,
    val size: SingleSelectInputSize,
    val options: List<String>,
    val selectedOption: String?,
    val onOptionSelected: (String) -> Unit,
    val onMenuOpenChanged: (Boolean) -> Unit,
    val onFocusChanged: (Boolean) -> Unit = {},
)