package com.instructure.horizon.horizonui.organisms.inputs.singleselectimage

import android.graphics.drawable.Drawable
import com.instructure.horizon.horizonui.organisms.inputs.common.InputLabelRequired

data class SingleSelectImageState(
    val label: String? = null,
    val helperText: String? = null,
    val placeHolderText: String? = null,
    val isFocused: Boolean = false,
    val enabled: Boolean = true,
    val isMenuOpen: Boolean = false,
    val errorText: String? = null,
    val required: InputLabelRequired = InputLabelRequired.Regular,
    val size: SingleSelectImageInputSize,
    val options: List<Pair<Drawable, String>>,
    val selectedOption: String?,
    val onOptionSelected: (Pair<Drawable, String>) -> Unit,
    val onMenuOpenChanged: (Boolean) -> Unit,
    val onFocusChanged: (Boolean) -> Unit = {},
)