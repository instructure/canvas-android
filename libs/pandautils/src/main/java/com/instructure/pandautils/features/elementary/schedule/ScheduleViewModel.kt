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
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.pandautils.R
import com.instructure.pandautils.features.elementary.homeroom.HomeroomAction
import com.instructure.pandautils.features.elementary.homeroom.HomeroomViewData
import com.instructure.pandautils.features.elementary.schedule.itemviewmodels.ScheduleCourseItemViewModel
import com.instructure.pandautils.features.elementary.schedule.itemviewmodels.ScheduleDayHeaderItemViewModel
import com.instructure.pandautils.features.elementary.schedule.itemviewmodels.ScheduleEmptyItemViewModel
import com.instructure.pandautils.features.elementary.schedule.itemviewmodels.SchedulePlannerItemViewModel
import com.instructure.pandautils.mvvm.Event
import com.instructure.pandautils.mvvm.ItemViewModel
import com.instructure.pandautils.mvvm.ViewState
import com.instructure.pandautils.utils.ColorApiHelper
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

            val courses = courseManager.getCoursesAsync(true).await()
            val coursesMap = courses.dataOrThrow
                    .filter { !it.homeroomCourse }
                    .associateBy { it.id }

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

                val coursePlannerMap = plannerItems
                        ?.filter {
                            date.isSameDay(it.plannable.dueAt) && it.courseId != null
                        }
                        .orEmpty()
                        .groupBy { coursesMap[it.courseId] }

                val courseViewModels = coursePlannerMap.entries.map {
                    val scheduleViewData = ScheduleCourseViewData(
                            it.key?.name ?: "To Do",
                            true,
                            getCourseColor(it.key),
                            it.key?.imageUrl ?: "",
                            it.value.map {
                                SchedulePlannerItemViewModel(
                                        SchedulePlannerItemData(
                                                it.plannable.title,
                                                PlannerItemType.ASSIGNMENT,
                                                it.plannable.pointsPossible,
                                                if (it.plannable.dueAt != null) "Due ${SimpleDateFormat("hh:mm aa", Locale.getDefault()).format(it.plannable.dueAt)}" else "",
                                                true
                                        ),
                                        {},
                                        {}
                                )
                            }
                    )
                    ScheduleCourseItemViewModel(
                            scheduleViewData,
                            {}
                    )
                }

                if (courseViewModels.isEmpty()) {
                    itemViewModels.add(ScheduleEmptyItemViewModel(
                            ScheduleEmptyViewData(resources.getString(R.string.nothing_planned_yet))
                    ))
                } else {
                    itemViewModels.addAll(courseViewModels)
                }
            }
            _data.postValue(ScheduleViewData(itemViewModels))
        }
    }

    fun jumpToToday() {

    }

    private fun getCourseColor(course: Course?): String {
        return if (!course?.courseColor.isNullOrEmpty()) {
            course?.courseColor!!
        } else {
            ColorApiHelper.K5_DEFAULT_COLOR
        }
    }
}