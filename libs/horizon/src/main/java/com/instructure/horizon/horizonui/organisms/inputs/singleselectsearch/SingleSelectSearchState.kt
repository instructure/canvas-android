package com.instructure.horizon.horizonui.organisms.inputs.singleselectsearch

import androidx.compose.ui.text.input.TextFieldValue
import com.instructure.horizon.horizonui.organisms.inputs.common.InputLabelRequired

data class SingleSelectSearchState(
    val label: String? = null,
    val helperText: String? = null,
    val placeHolderText: String? = null,
    val isFocused: Boolean = false,
    val enabled: Boolean = true,
    val isMenuOpen: Boolean = false,
    val errorText: String? = null,
    val required: InputLabelRequired = InputLabelRequired.Regular,
    val size: SingleSelectSearchInputSize,
    val options: List<String>,
    val selectedOption: String?,
    val onOptionSelected: (String) -> Unit,
    val onMenuOpenChanged: (Boolean) -> Unit,
    val onFocusChanged: (Boolean) -> Unit = {},
    val searchQuery: TextFieldValue = TextFieldValue(""),
    val onSearchQueryChanged: (TextFieldValue) -> Unit = {},
)