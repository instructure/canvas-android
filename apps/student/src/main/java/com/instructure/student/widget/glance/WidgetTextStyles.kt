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

import androidx.compose.ui.unit.sp
import androidx.glance.text.FontFamily
import androidx.glance.text.FontWeight
import androidx.glance.text.TextStyle


object WidgetTextStyles {

    val normalDarkest = TextStyle(
        color = WidgetColors.textDarkest,
        fontSize = 16.sp,
        fontWeight = FontWeight.Normal,
        fontFamily = FontFamily("lato_regular")
    )

    val normalDark = TextStyle(
        color = WidgetColors.textDark,
        fontSize = 16.sp,
        fontWeight = FontWeight.Normal,
        fontFamily = FontFamily("lato_regular")
    )

    val mediumDarkest = TextStyle(
        color = WidgetColors.textDarkest,
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium,
        fontFamily = FontFamily("lato_regular")
    )

    val mediumDark = TextStyle(
        color = WidgetColors.textDark,
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium,
        fontFamily = FontFamily("lato_regular")
    )
}
