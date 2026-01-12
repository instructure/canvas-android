package com.instructure.horizon.features.notebook.common.model

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.instructure.horizon.R

enum class NotebookType(
    @StringRes val labelRes: Int,
    @DrawableRes val iconRes: Int,
    @ColorRes val highlightColor: Int,
    @ColorRes val lineColor: Int
) {
    Confusing(
        R.string.notebookTypeUnclear,
        R.drawable.help,
        R.color.primitives_red12,
        R.color.primitives_red57
    ),
    Important(
        R.string.notebookTypeImportant,
        R.drawable.keep_pin,
        R.color.primitives_sea12,
        R.color.primitives_sea57
    ),
}
