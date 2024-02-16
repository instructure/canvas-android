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

package com.instructure.pandautils.features.todo.createupdate

import android.content.Context
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.utils.DateHelper
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime
import org.threeten.bp.format.DateTimeFormatter

data class CreateUpdateToDoUiState(
    val title: String = "",
    val date: LocalDate = LocalDate.now(),
    val time: LocalTime = LocalTime.of(12, 0),
    val selectedCourse: Course? = null,
    val details: String = "",
    val saving: Boolean = false,
    val errorSnack: String? = null
) {
    val formattedDate = date.format(DateTimeFormatter.ofPattern(DateHelper.dayMonthDateFormat.toPattern())).orEmpty()

    fun formattedTime(context: Context) = time.format(
        DateTimeFormatter.ofPattern(DateHelper.getPreferredTimeFormat(context).toPattern())
    ).orEmpty()
}

sealed class CreateUpdateToDoAction {
    data class UpdateTitle(val title: String) : CreateUpdateToDoAction()
    data class UpdateDate(val date: LocalDate) : CreateUpdateToDoAction()
    data class UpdateTime(val time: LocalTime) : CreateUpdateToDoAction()
    data class UpdateCourse(val course: Course) : CreateUpdateToDoAction()
    data class UpdateDetails(val details: String) : CreateUpdateToDoAction()
    data object Save : CreateUpdateToDoAction()
    data object SnackbarDismissed : CreateUpdateToDoAction()
}

sealed class CreateUpdateToDoViewModelAction {
    data class RefreshCalendarDay(val date: LocalDate) : CreateUpdateToDoViewModelAction()
}