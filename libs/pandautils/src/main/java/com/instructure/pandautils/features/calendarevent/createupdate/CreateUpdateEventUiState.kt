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

import com.google.ical.values.RRule
import com.instructure.canvasapi2.apis.CalendarEventAPI
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.pandautils.compose.composables.SelectContextUiState
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime
import org.threeten.bp.format.DateTimeFormatter


data class CreateUpdateEventUiState(
    val title: String = "",
    val date: LocalDate = LocalDate.now(),
    val startTime: LocalTime? = null,
    val endTime: LocalTime? = null,
    val selectFrequencyUiState: SelectFrequencyUiState = SelectFrequencyUiState(),
    val selectContextUiState: SelectContextUiState = SelectContextUiState(),
    val location: String = "",
    val address: String = "",
    val details: String = "",
    val saving: Boolean = false,
    val errorSnack: String? = null,
    val loadingCanvasContexts: Boolean = false,
    val showUnsavedChangesDialog: Boolean = false,
    val canNavigateBack: Boolean = false,
    val isSeriesEvent: Boolean = false,
    val isSeriesHead: Boolean = false
) {
    val formattedDate = date.format(DateTimeFormatter.ofPattern(DateHelper.dayMonthDateFormat.toPattern())).orEmpty()
}

data class SelectFrequencyUiState(
    val showFrequencyDialog: Boolean = false,
    val selectedFrequency: String? = null,
    val frequencies: Map<String, RRule?> = emptyMap(),
    val customFrequencyUiState: CustomFrequencyUiState = CustomFrequencyUiState()
)

data class CustomFrequencyUiState(
    val show: Boolean = false,
    val quantity: Int = 0,
    val timeUnits: List<String> = emptyList(),
    val selectedTimeUnitIndex: Int = 0,
    val daySelectorVisible: Boolean = false,
    val days: List<DayOfWeek> = emptyList(),
    val selectedDays: Set<DayOfWeek> = emptySet(),
    val repeatsOnVisible: Boolean = false,
    val repeatsOn: List<String> = emptyList(),
    val selectedRepeatsOnIndex: Int = 0,
    val selectedDate: LocalDate? = null,
    val selectedOccurrences: Int = 0
) {
    val formattedEndDate = selectedDate?.format(DateTimeFormatter.ofPattern(DateHelper.dayMonthYearFormat.toPattern())).orEmpty()
}

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
    data class Save(val modifyEventScope: CalendarEventAPI.ModifyEventScope, val isSeriesEvent: Boolean = false) : CreateUpdateEventAction()
    data object SnackbarDismissed : CreateUpdateEventAction()
    data object ShowSelectCalendarScreen : CreateUpdateEventAction()
    data object HideSelectCalendarScreen : CreateUpdateEventAction()
    data object CheckUnsavedChanges : CreateUpdateEventAction()
    data object HideUnsavedChangesDialog : CreateUpdateEventAction()
    data object NavigateBack : CreateUpdateEventAction()
    data object ShowFrequencyDialog : CreateUpdateEventAction()
    data object HideFrequencyDialog : CreateUpdateEventAction()
    data object ShowCustomFrequencyScreen : CreateUpdateEventAction()
    data object HideCustomFrequencyScreen : CreateUpdateEventAction()
    data class UpdateCustomFrequencyQuantity(val quantity: Int) : CreateUpdateEventAction()
    data class UpdateCustomFrequencySelectedTimeUnitIndex(val index: Int) : CreateUpdateEventAction()
    data class UpdateCustomFrequencySelectedDays(val days: Set<DayOfWeek>) : CreateUpdateEventAction()
    data class UpdateCustomFrequencySelectedRepeatsOnIndex(val index: Int) : CreateUpdateEventAction()
    data class UpdateCustomFrequencyEndDate(val date: LocalDate) : CreateUpdateEventAction()
    data class UpdateCustomFrequencyOccurrences(val occurrences: Int) : CreateUpdateEventAction()
    data object SaveCustomFrequency : CreateUpdateEventAction()
}

sealed class CreateUpdateEventViewModelAction {
    data class RefreshCalendarDays(val days: List<LocalDate>) : CreateUpdateEventViewModelAction()
    data object RefreshCalendar : CreateUpdateEventViewModelAction()
    data object NavigateBack : CreateUpdateEventViewModelAction()
    data class AnnounceEventCreation(val title: String) : CreateUpdateEventViewModelAction()
    data class AnnounceEventUpdate(val title: String) : CreateUpdateEventViewModelAction()
}