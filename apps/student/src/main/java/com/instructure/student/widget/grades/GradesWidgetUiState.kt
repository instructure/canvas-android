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
package com.instructure.student.widget.grades

import androidx.annotation.ColorInt
import com.instructure.student.widget.glance.WidgetState

data class GradesWidgetUiState(
    val state: WidgetState,
    val courses: List<WidgetCourseItem> = emptyList()
)

data class WidgetCourseItem(
    val name: String,
    val courseCode: String,
    val isLocked: Boolean,
    val gradeText: String?,
    @ColorInt val courseColorLight: Int,
    @ColorInt val courseColorDark: Int,
    val url: String
)
