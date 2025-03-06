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

package com.instructure.pandautils.features.calendartodo.createupdate

import android.content.Context
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.pandautils.compose.composables.SelectContextUiState
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime
import org.threeten.bp.format.DateTimeFormatter

data class CreateUpdateToDoUiState(
    val title: String = "",
    val date: LocalDate = LocalDate.now(),
    val time: LocalTime = LocalTime.of(12, 0),
    val details: String = "",
    val saving: Boolean = false,
    val errorSnack: String? = null,
    val loadingCanvasContexts: Boolean = false,
    val selectContextUiState: SelectContextUiState = SelectContextUiState(),
    val showUnsavedChangesDialog: Boolean = false,
    val canNavigateBack: Boolean = false
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
    data object CheckUnsavedChanges : CreateUpdateToDoAction()
    data object HideUnsavedChangesDialog : CreateUpdateToDoAction()
    data object NavigateBack : CreateUpdateToDoAction()
}

sealed class CreateUpdateToDoViewModelAction {
    data class RefreshCalendarDays(val days: List<LocalDate>) : CreateUpdateToDoViewModelAction()
    data object NavigateBack : CreateUpdateToDoViewModelAction()
    data class AnnounceToDoCreation(val title: String) : CreateUpdateToDoViewModelAction()
    data class AnnounceToDoUpdate(val title: String) : CreateUpdateToDoViewModelAction()
}