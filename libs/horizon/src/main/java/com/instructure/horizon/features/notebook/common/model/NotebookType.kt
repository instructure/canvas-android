package com.instructure.horizon.features.notebook.common.model

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.instructure.horizon.R

enum class NotebookType(
    @StringRes val labelRes: Int,
    @DrawableRes val iconRes: Int,
    @ColorRes val color: Int
) {
    Confusing(R.string.notebookTypeConfusing, R.drawable.help, R.color.icon_error),
    Important(R.string.notebookTypeImportant, R.drawable.flag_2, R.color.icon_action),
}
