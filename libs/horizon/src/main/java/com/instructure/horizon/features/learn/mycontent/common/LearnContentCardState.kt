package com.instructure.horizon.features.learn.mycontent.common

import androidx.annotation.DrawableRes
import com.instructure.horizon.horizonui.molecules.StatusChipColor

data class LearnContentCardState(
    val imageUrl: String? = null,
    val name: String = "",
    val progress: Double? = null,
    val route: String = "",
    val buttonLabel: String? = null,
    val cardChips: List<LearnContentCardChipState> = emptyList(),
    val isProgram: Boolean = false,
    val courseNames: List<String> = emptyList(),
)

data class LearnContentCardChipState(
    val label: String = "",
    @DrawableRes val iconRes: Int? = null,
    val color: StatusChipColor = StatusChipColor.Grey,
)