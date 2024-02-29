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
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.utils.DateHelper
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime
import org.threeten.bp.format.DateTimeFormatter

data class CreateUpdateToDoUiState(
    val title: String = "",
    val date: LocalDate = LocalDate.now(),
    val time: LocalTime = LocalTime.of(12, 0),
    val selectedCanvasContext: CanvasContext? = null,
    val details: String = "",
    val saving: Boolean = false,
    val errorSnack: String? = null,
    val loadingCanvasContexts: Boolean = false,
    val showCalendarSelector: Boolean = false,
    val canvasContexts: List<CanvasContext> = emptyList()
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
    data class UpdateCanvasContext(val canvasContext: CanvasContext) : CreateUpdateToDoAction()
    data class UpdateDetails(val details: String) : CreateUpdateToDoAction()
    data object Save : CreateUpdateToDoAction()
    data object SnackbarDismissed : CreateUpdateToDoAction()
    data object ShowSelectCalendarScreen : CreateUpdateToDoAction()
    data object HideSelectCalendarScreen : CreateUpdateToDoAction()
    data class CheckUnsavedChanges(val result: (Boolean) -> Unit) : CreateUpdateToDoAction()
}

sealed class CreateUpdateToDoViewModelAction {
    data class RefreshCalendarDays(val days: List<LocalDate>) : CreateUpdateToDoViewModelAction()
}