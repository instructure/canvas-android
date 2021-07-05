/*
 * Copyright (C) 2021 - present Instructure, Inc.
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

package com.instructure.pandautils.features.elementary.schedule

import android.content.res.Resources
import android.text.format.DateUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.managers.AnnouncementManager
import com.instructure.canvasapi2.managers.CourseManager
import com.instructure.canvasapi2.managers.PlannerManager
import com.instructure.canvasapi2.managers.ToDoManager
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.pandautils.features.elementary.homeroom.HomeroomAction
import com.instructure.pandautils.features.elementary.homeroom.HomeroomViewData
import com.instructure.pandautils.features.elementary.schedule.itemviewmodels.ScheduleDayHeaderItemViewModel
import com.instructure.pandautils.mvvm.Event
import com.instructure.pandautils.mvvm.ItemViewModel
import com.instructure.pandautils.mvvm.ViewState
import com.instructure.pandautils.utils.isSameDay
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import org.threeten.bp.DateTimeUtils
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ScheduleViewModel @Inject constructor(
        private val apiPrefs: ApiPrefs,
        private val resources: Resources,
        private val plannerManager: PlannerManager,
        private val courseManager: CourseManager,
        private val announcementManager: AnnouncementManager,
        private val toDoManager: ToDoManager) : ViewModel() {

    val state: LiveData<ViewState>
        get() = _state
    private val _state = MutableLiveData<ViewState>()

    val data: LiveData<ScheduleViewData>
        get() = _data
    private val _data = MutableLiveData<ScheduleViewData>()

    val events: LiveData<Event<ScheduleAction>>
        get() = _events
    private val _events = MutableLiveData<Event<ScheduleAction>>()

    init {
        viewModelScope.launch {
            val startDate = Date()
            val weekStart = DateHelper.getLastSunday(startDate)
            val userTodos = toDoManager.getUserTodosAsync(true).await().dataOrNull

            val plannerItems = plannerManager.getPlannerItemsAsync(
                    true,
                    weekStart.toApiString(),
                    DateHelper.getNextSaturday(startDate).toApiString())
                    .await()
                    .dataOrNull


            val itemViewModels = mutableListOf<ItemViewModel>()
            for (i in 0..6) {
                val calendar = Calendar.getInstance()
                calendar.time = weekStart
                calendar.set(Calendar.DAY_OF_WEEK, calendar.get(Calendar.DAY_OF_WEEK) + i)
                val date = calendar.time
                itemViewModels.add(ScheduleDayHeaderItemViewModel(
                        SimpleDateFormat("EEEE", Locale.getDefault()).format(date),
                        SimpleDateFormat("MMMM dd", Locale.getDefault()).format(date),
                        !Date().isSameDay(date),
                        this@ScheduleViewModel::jumpToToday))

                val filteredPlannerItems = plannerItems?.filter { date.isSameDay(it.plannableDate) }.orEmpty().map {  }
            }
            _data.postValue(ScheduleViewData(itemViewModels))
        }
    }

    fun jumpToToday() {

    }
}