package com.instructure.student.widget.glance

import androidx.compose.ui.graphics.Color
import androidx.glance.color.ColorProvider


object WidgetColors {

    private val backgroundLightestLight = Color(0xFFFFFFFF)
    private val backgroundLightestDark = Color(0xFF0F1316)
    private val textDarkestLight = Color(0xFF273540)
    private val textDarkestDark = Color(0xFFFFFFFF)
    private val textDarkLight = Color(0xFF66717C)
    private val textDarkDark = Color(0xFF818991)
    private val textDangerLight = Color(0xFFE62429)
    private val textDangerDark = Color(0xFFFF3653)

    val backgroundLightest = ColorProvider(
        day = backgroundLightestLight,
        night = backgroundLightestDark
    )

    val textDarkest = ColorProvider(
        day = textDarkestLight,
        night = textDarkestDark
    )

    val textDark = ColorProvider(
        day = textDarkLight,
        night = textDarkDark
    )

    val textDanger = ColorProvider(
        day = textDangerLight,
        night = textDangerDark
    )
}
