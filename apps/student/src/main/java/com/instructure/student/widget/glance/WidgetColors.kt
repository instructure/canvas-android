/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

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
    private val textLightestLight = Color(0xFFFFFFFF)
    private val textLightestDark = Color(0xFF0F1316)
    private val borderMediumLight = Color(0xFF9EA6AD)
    private val borderMediumDark = Color(0xFF565D64)

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

    val textLightest = ColorProvider(
        day = textLightestLight,
        night = textLightestDark
    )

    val borderMedium = ColorProvider(
        day = borderMediumLight,
        night = borderMediumDark
    )
}
