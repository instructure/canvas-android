package com.instructure.horizon.horizonui.molecules

import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.instructure.horizon.horizonui.foundation.HorizonColors

@Composable
fun HorizonDivider(thickness: Dp = 1.dp, color: Color = HorizonColors.LineAndBorder.lineStroke()) {
    HorizontalDivider(
        thickness = thickness,
        color = color,
    )
}