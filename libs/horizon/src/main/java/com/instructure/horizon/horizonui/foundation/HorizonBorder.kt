package com.instructure.horizon.horizonui.foundation

import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

object HorizonBorder {
    fun level1(color: Color = HorizonColors.LineAndBorder.lineStroke()) = BorderStroke(1.dp, color)
    fun level2(color: Color = HorizonColors.LineAndBorder.lineStroke()) = BorderStroke(2.dp, color)
}