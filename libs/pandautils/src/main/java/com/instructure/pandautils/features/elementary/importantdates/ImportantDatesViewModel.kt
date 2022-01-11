/*
 * Copyright (C) 2022 - present Instructure, Inc.
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

package com.instructure.pandautils.features.elementary.importantdates

import android.content.res.Resources
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.apis.CalendarEventAPI
import com.instructure.canvasapi2.managers.CalendarEventManager
import com.instructure.canvasapi2.managers.CourseManager
import com.instructure.canvasapi2.models.ScheduleItem
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.pandautils.R
import com.instructure.pandautils.features.elementary.importantdates.itemviewmodels.ImportantDatesHeaderItemViewModel
import com.instructure.pandautils.mvvm.Event
import com.instructure.pandautils.mvvm.ItemViewModel
import com.instructure.pandautils.mvvm.ViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ImportantDatesViewModel @Inject constructor(
        private val courseManager: CourseManager,
        private val calendarEventManager: CalendarEventManager,
        private val resources: Resources
) : ViewModel() {

    val state: LiveData<ViewState>
        get() = _state
    private val _state = MutableLiveData<ViewState>()

    val data: LiveData<ImportantDatesViewData>
        get() = _data
    private val _data = MutableLiveData<ImportantDatesViewData>()

    val events: LiveData<Event<ImportantDatesAction>>
        get() = _events
    private val _events = MutableLiveData<Event<ImportantDatesAction>>()

    init {
        _state.postValue(ViewState.Loading)
        loadData(false)
    }

    fun refresh() {
        _state.postValue(ViewState.Refresh)
        loadData(true)
    }

    private fun loadData(forceNetwork: Boolean) {
        viewModelScope.launch {
            try {
                val courses = courseManager.getCoursesAsync(forceNetwork).await().dataOrThrow
                val contextIds = courses.map { it.contextId }

                val endDate = Calendar.getInstance().apply {
                    roll(Calendar.YEAR, 1)
                }.time
                val importantDateEvents = calendarEventManager.getImportantDatesAsync(startDate = Date().toApiString(),
                        endDate = endDate.toApiString(),
                        type = CalendarEventAPI.CalendarEventType.CALENDAR,
                        canvasContexts = contextIds,
                        forceNetwork = forceNetwork).await().dataOrThrow

                val importantDateAssignments = calendarEventManager.getImportantDatesAsync(startDate = Date().toApiString(),
                        endDate = endDate.toApiString(),
                        type = CalendarEventAPI.CalendarEventType.ASSIGNMENT,
                        canvasContexts = contextIds,
                        forceNetwork = forceNetwork).await().dataOrThrow

                val importantDates = (importantDateAssignments + importantDateEvents)
                        .filter { it.startDate != null }
                        .sortedBy { it.startDate }

                val items = createItems(importantDates)
                if (items.isNotEmpty()) {
                    _data.postValue(ImportantDatesViewData(items))
                    _state.postValue(ViewState.Success)
                } else {
                    _state.postValue(ViewState.Empty(emptyTitle = R.string.importantDatesEmptyTitle, emptyImage = R.drawable.ic_panda_noannouncements))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _state.postValue(ViewState.Error(resources.getString(R.string.errorOccurred)))
            }
        }
    }

    private fun createItems(importantDates: List<ScheduleItem>): List<ItemViewModel> {
        val importantDatesMap = importantDates.groupBy { it.startDate }
        return importantDatesMap.map {
            createDayGroup(it.key, it.value)
        }
    }

    private fun createDayGroup(date: Date?, items: List<ScheduleItem>): ItemViewModel {
        return ImportantDatesHeaderItemViewModel(
                ImportantDatesHeaderViewData(
                        SimpleDateFormat("EEEE, MMMM dd", Locale.getDefault()).format(date!!)
                ),
                emptyList()
        )
    }
}