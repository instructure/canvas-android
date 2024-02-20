/*
 * Copyright (C) 2024 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package com.instructure.pandautils.features.calendar

import androidx.annotation.DrawableRes
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.PlannerItem
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate
import org.threeten.bp.format.TextStyle
import org.threeten.bp.temporal.ChronoUnit
import java.util.Locale

data class CalendarScreenUiState(
    val selectedDay: LocalDate,
    val expanded: Boolean,
    val calendarUiState: CalendarUiState,
    val calendarEventsUiState: CalendarEventsUiState = CalendarEventsUiState(),
    val snackbarMessage: String? = null,
    val scrollToPageOffset: Int = 0,
    val pendingSelectedDay: LocalDate? = null, // Temporary selected date when the calendar is animating to a new month
) {
    val headerUiState: CalendarHeaderUiState
        get() {
            return calendarUiState.headerUiState
        }

    val bodyUiState: CalendarBodyUiState
        get() {
            return calendarUiState.bodyUiState
        }
}

data class CalendarUiState(val headerUiState: CalendarHeaderUiState, val bodyUiState: CalendarBodyUiState)

data class CalendarHeaderUiState(val monthTitle: String, val yearTitle: String)

data class CalendarBodyUiState(
    val previousPage: CalendarPageUiState,
    val currentPage: CalendarPageUiState,
    val nextPage: CalendarPageUiState
)

data class CalendarPageUiState(val calendarRows: List<CalendarRowUiState>)

data class CalendarRowUiState(val days: List<CalendarDayUiState>)

data class CalendarDayUiState(
    val dayNumber: Int,
    val date: LocalDate = LocalDate.now(),
    val enabled: Boolean = true,
    val indicatorCount: Int = 0
) {
    val today: Boolean
        get() {
            val today = LocalDate.now()
            return date.isEqual(today)
        }
}

data class CalendarEventsUiState(
    val previousPage: CalendarEventsPageUiState = CalendarEventsPageUiState(),
    val currentPage: CalendarEventsPageUiState = CalendarEventsPageUiState(),
    val nextPage: CalendarEventsPageUiState = CalendarEventsPageUiState()
)

data class CalendarEventsPageUiState(
    val date: LocalDate = LocalDate.now(),
    val loading: Boolean = false,
    val error: Boolean = false,
    val refreshing: Boolean = false,
    val events: List<EventUiState> = emptyList()
)

data class EventUiState(
    val plannableId: Long,
    val contextName: String,
    val canvasContext: CanvasContext,
    val name: String,
    @DrawableRes val iconRes: Int,
    val date: String? = null,
    val status: String? = null
)

sealed class CalendarAction {
    data object ExpandChanged : CalendarAction()
    data object ExpandDisabled : CalendarAction()
    data object ExpandEnabled : CalendarAction()
    data class DaySelected(val selectedDay: LocalDate) : CalendarAction()
    data object TodayTapped : CalendarAction()
    data class PageChanged(val offset: Int) : CalendarAction()
    data class EventPageChanged(val offset: Int) : CalendarAction()
    data class EventSelected(val id: Long): CalendarAction()
    data class RefreshDay(val date: LocalDate): CalendarAction()
    data object Retry : CalendarAction()
    data object SnackbarDismissed : CalendarAction()
    data object HeightAnimationFinished : CalendarAction()
}

sealed class CalendarViewModelAction {
    data class OpenAssignment(val canvasContext: CanvasContext, val assignmentId: Long): CalendarViewModelAction()
    data class OpenDiscussion(val canvasContext: CanvasContext, val discussionId: Long): CalendarViewModelAction()
    data class OpenQuiz(val canvasContext: CanvasContext, val htmlUrl: String): CalendarViewModelAction()
    data class OpenCalendarEvent(val canvasContext: CanvasContext, val eventId: Long): CalendarViewModelAction()
    data class OpenToDo(val plannerItem: PlannerItem) : CalendarViewModelAction()
}