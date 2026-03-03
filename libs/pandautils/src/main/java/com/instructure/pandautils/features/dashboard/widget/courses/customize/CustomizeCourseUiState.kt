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

package com.instructure.pandautils.features.dashboard.widget.courses.customize

data class CustomizeCourseUiState(
    val courseId: Long = 0L,
    val courseName: String = "",
    val courseCode: String = "",
    val imageUrl: String? = null,
    val nickname: String = "",
    val selectedColor: Int = 0,
    val initialColor: Int = 0,
    val availableColors: List<Int> = emptyList(),
    val showColorOverlay: Boolean = true,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val shouldNavigateBack: Boolean = false,
    val onNicknameChanged: (String) -> Unit = {},
    val onColorSelected: (Int) -> Unit = {},
    val onDone: () -> Unit = {},
    val onNavigationHandled: () -> Unit = {},
    val onErrorHandled: () -> Unit = {}
)