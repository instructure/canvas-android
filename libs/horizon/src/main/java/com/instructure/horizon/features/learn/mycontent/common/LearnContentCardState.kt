package com.instructure.horizon.features.learn.mycontent.common

import androidx.annotation.DrawableRes
import com.instructure.horizon.horizonui.molecules.StatusChipColor

data class LearnContentCardState(
    val imageUrl: String? = null,
    val name: String = "",
    val progress: Double? = null,
    val route: String = "",
    val buttonState: LearnContentCardButtonState? = null,
    val cardChips: List<LearnContentCardChipState> = emptyList(),
    val courseNames: List<String> = emptyList(),
)

data class LearnContentCardButtonState(
    val label: String = "",
    val route: Any = "",
)

data class LearnContentCardChipState(
    val label: String = "",
    @DrawableRes val iconRes: Int? = null,
    val color: StatusChipColor = StatusChipColor.Grey,
)