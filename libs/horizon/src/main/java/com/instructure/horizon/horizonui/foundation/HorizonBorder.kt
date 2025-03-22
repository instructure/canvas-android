package com.instructure.horizon.horizonui.foundation

import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// This color is the UI Colors/Lines and Borders/Stroke color from the Figma file. We should define it here, because we can't use composables in BorderStroke.
private val borderStrokeColor = Color(0xFF273540)

object HorizonBorder {
    val level1 = BorderStroke(1.dp, borderStrokeColor)
    val level2 = BorderStroke(2.dp, borderStrokeColor)
}