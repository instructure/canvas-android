/*
 * Copyright (C) 2026 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.instructure.pandautils.features.dashboard.customize.course

data class CustomizeCourseUiState(
    val courseId: Long = 0L,
    val courseName: String = "",
    val courseCode: String = "",
    val imageUrl: String? = null,
    val nickname: String = "",
    val selectedColor: Int = 0,
    val availableColors: List<Int> = emptyList(),
    val isLoading: Boolean = false
)