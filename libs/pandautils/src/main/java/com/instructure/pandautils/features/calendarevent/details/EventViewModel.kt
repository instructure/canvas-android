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
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.ScheduleItem
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.pandautils.R
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.HtmlContentFormatter
import com.instructure.pandautils.utils.backgroundColor
import com.instructure.pandautils.utils.orDefault
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
    private val apiPrefs: ApiPrefs
) : ViewModel() {

    private val _uiState = MutableStateFlow(EventUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = Channel<EventViewModelAction>()
    val events = _events.receiveAsFlow()

    val canvasContext: CanvasContext? = savedStateHandle.get<CanvasContext>(Const.CANVAS_CONTEXT)
    private val scheduleItemArg: ScheduleItem? = savedStateHandle.get<ScheduleItem>(EventFragment.SCHEDULE_ITEM)
    private val scheduleItemId: Long? = savedStateHandle.get<Long>(EventFragment.SCHEDULE_ITEM_ID)

    private var scheduleItem: ScheduleItem? = null

    init {
        loadData()
    }

    private fun loadData() {
        setToolbarColor()
        viewModelScope.tryLaunch {
            _uiState.update { it.copy(loading = true) }
            if (scheduleItemArg != null) {
                scheduleItem = scheduleItemArg
                updateUiStateWithData(scheduleItemArg)
            } else {
                val scheduleItem = eventRepository.getCalendarEvent(scheduleItemId.orDefault())
                this@EventViewModel.scheduleItem = scheduleItem
                updateUiStateWithData(scheduleItem)
            }
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
        canvasContext?.backgroundColor?.let { color ->
            _uiState.update { it.copy(toolbarUiState = it.toolbarUiState.copy(toolbarColor = color)) }
        }
    }

    private suspend fun updateUiStateWithData(scheduleItem: ScheduleItem) {
        _uiState.update {
            it.copy(
                toolbarUiState = it.toolbarUiState.copy(
                    subtitle = scheduleItem.contextName.orEmpty(),
                    modifyAllowed = scheduleItem.contextId == apiPrefs.user?.id
                ),
                loading = false,
                title = scheduleItem.title.orEmpty(),
                date = getDateString(scheduleItem.isAllDay, scheduleItem.startDate, scheduleItem.endDate),
                recurrence = scheduleItem.seriesNaturalLanguage.orEmpty(),
                location = scheduleItem.locationName.orEmpty(),
                address = scheduleItem.locationAddress.orEmpty(),
                formattedDescription = htmlContentFormatter.formatHtmlWithIframes(scheduleItem.description.orEmpty())
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

    fun handleAction(action: EventAction) {
        when (action) {
            is EventAction.OnLtiClicked -> viewModelScope.launch {
                _events.send(EventViewModelAction.OpenLtiScreen(action.url))
            }

            is EventAction.DeleteEvent -> Unit

            is EventAction.EditEvent -> viewModelScope.launch {
                scheduleItem?.let {
                    _events.send(EventViewModelAction.OpenEditEvent(it))
                }
            }
        }
    }
}