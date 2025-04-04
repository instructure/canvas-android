package com.instructure.horizon.horizonui.organisms.inputs.multi_select

data class MultiSelectState(
    val label: String? = null,
    val helperText: String? = null,
    val placeHolderText: String? = null,
    val isFocused: Boolean = false,
    val isDisabled: Boolean = false,
    val isMenuOpen: Boolean = false,
    val errorText: String? = null,
    val size: MultiSelectInputSize,
    val options: List<String>,
    val selectedOptions: List<String>,
    val onOptionSelected: (String) -> Unit,
    val onOptionRemoved: (String) -> Unit,
    val onMenuOpenChanged: (Boolean) -> Unit,
    val onFocusChanged: (Boolean) -> Unit = {},
)