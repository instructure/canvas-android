package com.instructure.horizon.horizonui.organisms.inputs.multiselectsearch

import androidx.compose.ui.text.input.TextFieldValue
import com.instructure.horizon.horizonui.organisms.inputs.common.InputLabelRequired

data class MultiSelectSearchState(
    val label: String? = null,
    val helperText: String? = null,
    val placeHolderText: String? = null,
    val isFocused: Boolean = false,
    val enabled: Boolean = true,
    val isMenuOpen: Boolean = false,
    val errorText: String? = null,
    val required: InputLabelRequired = InputLabelRequired.Regular,
    val size: MultiSelectSearchInputSize,
    val options: List<String>,
    val selectedOptions: List<String>,
    val onOptionSelected: (String) -> Unit,
    val onOptionRemoved: (String) -> Unit,
    val onMenuOpenChanged: (Boolean) -> Unit,
    val onFocusChanged: (Boolean) -> Unit = {},
    val searchPlaceHolder: String? = null,
    val searchQuery: TextFieldValue = TextFieldValue(""),
    val onSearchQueryChanged: (TextFieldValue) -> Unit = {},
    val isOptionListLoading: Boolean = false
)