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

package com.instructure.pandautils.features.calendarevent.createupdate

import android.content.Context
import com.google.ical.values.RRule
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.pandautils.compose.composables.SelectCalendarUiState
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime
import org.threeten.bp.format.DateTimeFormatter


data class CreateUpdateEventUiState(
    val title: String = "",
    val date: LocalDate = LocalDate.now(),
    val startTime: LocalTime? = null,
    val endTime: LocalTime? = null,
    val frequencyDialogUiState: FrequencyDialogUiState = FrequencyDialogUiState(),
    val selectCalendarUiState: SelectCalendarUiState = SelectCalendarUiState(),
    val location: String = "",
    val address: String = "",
    val details: String = "",
    val saving: Boolean = false,
    val errorSnack: String? = null,
    val loadingCanvasContexts: Boolean = false,
    val showUnsavedChangesDialog: Boolean = false,
    val canNavigateBack: Boolean = false
) {
    val formattedDate = date.format(DateTimeFormatter.ofPattern(DateHelper.dayMonthDateFormat.toPattern())).orEmpty()

    fun formattedTime(context: Context, time: LocalTime) = time.format(
        DateTimeFormatter.ofPattern(DateHelper.getPreferredTimeFormat(context).toPattern())
    ).orEmpty()
}

data class FrequencyDialogUiState(
    val selectedFrequency: String? = null,
    val frequencies: Map<String, RRule?> = emptyMap()
)

sealed class CreateUpdateEventAction {
    data class UpdateTitle(val title: String) : CreateUpdateEventAction()
    data class UpdateDate(val date: LocalDate) : CreateUpdateEventAction()
    data class UpdateStartTime(val time: LocalTime) : CreateUpdateEventAction()
    data class UpdateEndTime(val time: LocalTime) : CreateUpdateEventAction()
    data class UpdateFrequency(val frequency: String) : CreateUpdateEventAction()
    data class UpdateCanvasContext(val canvasContext: CanvasContext) : CreateUpdateEventAction()
    data class UpdateLocation(val location: String) : CreateUpdateEventAction()
    data class UpdateAddress(val address: String) : CreateUpdateEventAction()
    data class UpdateDetails(val details: String) : CreateUpdateEventAction()
    data object Save : CreateUpdateEventAction()
    data object SnackbarDismissed : CreateUpdateEventAction()
    data object ShowSelectCalendarScreen : CreateUpdateEventAction()
    data object HideSelectCalendarScreen : CreateUpdateEventAction()
    data object CheckUnsavedChanges : CreateUpdateEventAction()
    data object HideUnsavedChangesDialog : CreateUpdateEventAction()
    data object NavigateBack : CreateUpdateEventAction()
    data class CustomFrequencySelected(val rrule: RRule) : CreateUpdateEventAction()
}

sealed class CreateUpdateEventViewModelAction {
    data class RefreshCalendarDays(val days: List<LocalDate>) : CreateUpdateEventViewModelAction()
    data object NavigateBack : CreateUpdateEventViewModelAction()
}