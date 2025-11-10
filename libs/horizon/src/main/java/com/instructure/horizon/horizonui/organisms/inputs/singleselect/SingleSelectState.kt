package com.instructure.horizon.horizonui.organisms.inputs.singleselect

import com.instructure.horizon.horizonui.organisms.inputs.common.InputLabelRequired

data class SingleSelectState(
    val label: String? = null,
    val helperText: String? = null,
    val placeHolderText: String? = null,
    val isFocused: Boolean = false,
    val enabled: Boolean = true,
    val isMenuOpen: Boolean = false,
    val errorText: String? = null,
    val isSingleLineOptions: Boolean = false,
    val isFullWidth: Boolean = false,
    val required: InputLabelRequired = InputLabelRequired.Regular,
    val size: SingleSelectInputSize,
    val options: List<String>,
    val selectedOption: String?,
    val onOptionSelected: (String) -> Unit,
    val onMenuOpenChanged: (Boolean) -> Unit,
    val onFocusChanged: (Boolean) -> Unit = {},
)