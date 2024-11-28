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

package com.instructure.pandautils.features.calendarevent.details

import android.annotation.SuppressLint
import android.content.Context
import androidx.annotation.ColorInt
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.apis.CalendarEventAPI
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.ScheduleItem
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.pandautils.R
import com.instructure.pandautils.features.reminder.ReminderItem
import com.instructure.pandautils.features.reminder.ReminderManager
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.HtmlContentFormatter
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.color
import com.instructure.pandautils.utils.dueAt
import com.instructure.pandautils.utils.eventHtmlUrl
import com.instructure.pandautils.utils.isUser
import com.instructure.pandautils.utils.orDefault
import com.instructure.pandautils.utils.toLocalDate
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
@SuppressLint("StaticFieldLeak")
class EventViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context,
    private val eventRepository: EventRepository,
    private val htmlContentFormatter: HtmlContentFormatter,
    private val apiPrefs: ApiPrefs,
    private val themePrefs: ThemePrefs,
    private val reminderManager: ReminderManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(EventUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = Channel<EventViewModelAction>()
    val events = _events.receiveAsFlow()

    private val canvasContext: CanvasContext? = savedStateHandle.get<CanvasContext>(Const.CANVAS_CONTEXT)
    private val canvasContextType: String? = savedStateHandle.get<String>(EventFragment.CONTEXT_TYPE)
    private val canvasContextId: Long? = savedStateHandle.get<Long>(EventFragment.CONTEXT_ID)
    private val scheduleItemArg: ScheduleItem? = savedStateHandle.get<ScheduleItem>(EventFragment.SCHEDULE_ITEM)
    private val scheduleItemId: Long? = savedStateHandle.get<Long>(EventFragment.SCHEDULE_ITEM_ID)

    private var scheduleItem: ScheduleItem? = null

    var checkingReminderPermission = false

    init {
        loadData()
    }

    override fun onCleared() {
        super.onCleared()
        reminderManager.removeLiveDataObserver()
    }

    private fun loadData() {
        setToolbarColor()
        viewModelScope.tryLaunch {
            _uiState.update { it.copy(loading = true) }
            if (scheduleItemArg != null) {
                scheduleItem = scheduleItemArg
            } else {
                val scheduleItem = eventRepository.getCalendarEvent(scheduleItemId.orDefault())
                this@EventViewModel.scheduleItem = scheduleItem
            }
            val canManageCalendar = when (scheduleItem?.contextType) {
                CanvasContext.Type.USER -> {
                    scheduleItem?.userId == apiPrefs.user?.id
                }
                CanvasContext.Type.COURSE -> {
                    eventRepository.canManageCourseCalendar(scheduleItem?.courseId.orDefault())
                }
                CanvasContext.Type.GROUP -> {
                    eventRepository.canManageGroupCalendar(scheduleItem?.groupId.orDefault())
                }
                else -> false
            }
            updateUiStateWithData(
                scheduleItem!!,
                canManageCalendar && scheduleItem?.workflowState == "active",
                canManageCalendar
            )
            observeReminders()
        } catch {
            _uiState.update {
                it.copy(
                    loading = false,
                    loadError = context.getString(R.string.errorLoadingEvent)
                )
            }
        }
    }

    private fun setToolbarColor() {
        getCanvasContext()?.let { canvasContext ->
            _uiState.update { it.copy(toolbarUiState = it.toolbarUiState.copy(toolbarColor = canvasContext.color, isUserContext = canvasContext.isUser)) }
        }
    }

    private suspend fun updateUiStateWithData(scheduleItem: ScheduleItem, editAllowed: Boolean, deleteAllowed: Boolean) {
        _uiState.update {
            it.copy(
                toolbarUiState = it.toolbarUiState.copy(
                    subtitle = scheduleItem.contextName.orEmpty(),
                    editAllowed = editAllowed,
                    deleteAllowed = deleteAllowed
                ),
                loading = false,
                title = scheduleItem.title.orEmpty(),
                date = getDateString(scheduleItem.isAllDay, scheduleItem.startDate, scheduleItem.endDate),
                recurrence = scheduleItem.seriesNaturalLanguage.orEmpty(),
                location = scheduleItem.locationName.orEmpty(),
                address = scheduleItem.locationAddress.orEmpty(),
                formattedDescription = htmlContentFormatter.formatHtmlWithIframes(scheduleItem.description.orEmpty()),
                isSeriesEvent = scheduleItem.isRecurring,
                isSeriesHead = scheduleItem.seriesHead
            )
        }
    }

    private fun getDateString(isAllDay: Boolean, startDate: Date?, endDate: Date?): String {
        val dateText = DateHelper.getFormattedDate(context, endDate).orEmpty()

        if (isAllDay) {
            return "$dateText - ${context.getString(R.string.allDayEvent)}"
        }

        if (startDate != null && endDate != null) {
            val startTime = DateHelper.getFormattedTime(context, startDate)
            val endTime = DateHelper.getFormattedTime(context, endDate)
            return if (startTime != endTime) {
                "$dateText $startTime - $endTime"
            } else {
                "$dateText $startTime"
            }
        }

        return DateHelper.getFormattedDate(context, startDate).orEmpty()
    }

    private fun deleteEvent(deleteScope: CalendarEventAPI.ModifyEventScope) {
        viewModelScope.tryLaunch {
            scheduleItem?.let { scheduleItem ->
                _uiState.update { it.copy(toolbarUiState = it.toolbarUiState.copy(deleting = true)) }
                if (scheduleItem.isRecurring) {
                    eventRepository.deleteRecurringCalendarEvent(scheduleItem.id, deleteScope)
                    _events.send(EventViewModelAction.RefreshCalendar)
                } else {
                    val removedEvent = eventRepository.deleteCalendarEvent(scheduleItem.id)
                    _events.send(EventViewModelAction.RefreshCalendarDays(listOfNotNull(removedEvent.startDate?.toLocalDate())))
                }
            }
        } catch {
            _uiState.update {
                it.copy(
                    errorSnack = context.getString(R.string.eventDeleteErrorMessage),
                    toolbarUiState = it.toolbarUiState.copy(deleting = false)
                )
            }
        }
    }

    fun handleAction(action: EventAction) {
        when (action) {
            is EventAction.SnackbarDismissed -> _uiState.update { it.copy(errorSnack = null) }

            is EventAction.OnLtiClicked -> viewModelScope.launch {
                _events.send(EventViewModelAction.OpenLtiScreen(action.url))
            }

            is EventAction.DeleteEvent -> deleteEvent(action.deleteScope)

            is EventAction.EditEvent -> viewModelScope.launch {
                scheduleItem?.let {
                    _events.send(EventViewModelAction.OpenEditEvent(it))
                }
            }

            is EventAction.OnReminderAddClicked -> viewModelScope.launch {
                scheduleItem?.let {
                    _events.send(EventViewModelAction.OnReminderAddClicked)
                }
            }

            is EventAction.OnReminderDeleteClicked -> viewModelScope.launch {
                showDeleteReminderConfirmationDialog(action.context, action.reminderId, themePrefs.textButtonColor)
            }
        }
    }

    fun getCanvasContext(): CanvasContext? {
        return when {
            canvasContext != null -> canvasContext
            canvasContextType != null && canvasContextId != null -> {
                val type = CanvasContext.Type.entries.find { it.apiString == canvasContextType } ?: CanvasContext.Type.UNKNOWN
                CanvasContext.fromContextCode(
                    CanvasContext.makeContextId(type, canvasContextId)
                )
            }
            else -> null
        }
    }

    fun showCreateReminderDialog(context: Context, @ColorInt color: Int) {
        scheduleItem?.let { scheduleItem ->
            viewModelScope.launch {
                when {
                    scheduleItem.dueAt == null -> reminderManager.showCustomReminderDialog(
                        context,
                        apiPrefs.user?.id.orDefault(),
                        scheduleItem.id,
                        scheduleItem.title.orEmpty(),
                        scheduleItem.eventHtmlUrl.orEmpty(),
                        scheduleItem.dueAt
                    )
                    scheduleItem.dueAt?.before(Date()).orDefault() -> reminderManager.showCustomReminderDialog(
                        context,
                        apiPrefs.user?.id.orDefault(),
                        scheduleItem.id,
                        scheduleItem.title.orEmpty(),
                        scheduleItem.eventHtmlUrl.orEmpty(),
                        scheduleItem.dueAt
                    )
                    else -> reminderManager.showBeforeDueDateReminderDialog(
                        context,
                        apiPrefs.user?.id.orDefault(),
                        scheduleItem.id,
                        scheduleItem.title.orEmpty(),
                        scheduleItem.eventHtmlUrl.orEmpty(),
                        scheduleItem.dueAt ?: Date(),
                        color
                    )
                }
            }
        }
    }

    private fun showDeleteReminderConfirmationDialog(context: Context, reminderId: Long, @ColorInt color: Int) {
        viewModelScope.launch { reminderManager.showDeleteReminderDialog(context, reminderId, color) }
    }

    private fun observeReminders() {
        scheduleItem?.let { scheduleItem ->
            reminderManager.observeRemindersLiveData(apiPrefs.user?.id.orDefault(), scheduleItem.id) { reminders ->
                    _uiState.update {
                        it.copy(
                            reminderUiState = it.reminderUiState.copy(
                                reminders = reminders.map { ReminderItem(it.id, it.text, Date(it.time)) }
                            )
                        )
                    }
                }
        }
    }
}
