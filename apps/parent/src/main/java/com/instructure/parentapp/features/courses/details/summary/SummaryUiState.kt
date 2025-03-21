/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
package com.instructure.parentapp.features.courses.details.summary

import android.graphics.Color
import androidx.annotation.ColorInt
import com.instructure.canvasapi2.models.ScheduleItem

data class SummaryUiState(
    val state: ScreenState = ScreenState.Loading,
    val courseId: Long = 0,
    val items: List<ScheduleItem> = emptyList(),
    @ColorInt val studentColor: Int = Color.BLACK
)

sealed class ScreenState {
    data object Loading : ScreenState()
    data object Refreshing : ScreenState()
    data object Error : ScreenState()
    data object Empty : ScreenState()
    data object Content : ScreenState()
}
