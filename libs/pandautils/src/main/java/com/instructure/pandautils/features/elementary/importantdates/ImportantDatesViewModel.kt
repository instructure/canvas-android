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
import androidx.annotation.DrawableRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.apis.CalendarEventAPI
import com.instructure.canvasapi2.managers.CalendarEventManager
import com.instructure.canvasapi2.managers.CourseManager
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.ScheduleItem
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.pandautils.R
import com.instructure.pandautils.features.elementary.importantdates.itemviewmodels.ImportantDatesHeaderItemViewModel
import com.instructure.pandautils.features.elementary.importantdates.itemviewmodels.ImportantDatesItemViewModel
import com.instructure.pandautils.mvvm.Event
import com.instructure.pandautils.mvvm.ViewState
import com.instructure.pandautils.utils.ColorApiHelper
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.pandautils.utils.ThemedColor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ImportantDatesViewModel @Inject constructor(
        private val courseManager: CourseManager,
        private val calendarEventManager: CalendarEventManager,
        private val resources: Resources,
        private val colorKeeper: ColorKeeper
) : ViewModel() {

    val state: LiveData<ViewState>
        get() = _state
    private val _state = MutableLiveData<ViewState>()

    val data: LiveData<ImportantDatesViewData>
        get() = _data
    private val _data = MutableLiveData<ImportantDatesViewData>(ImportantDatesViewData(emptyList()))

    val events: LiveData<Event<ImportantDatesAction>>
        get() = _events
    private val _events = MutableLiveData<Event<ImportantDatesAction>>()

    private var courseMap: Map<Long, Course> = emptyMap()
    private var importantDatesMap: Map<Long, ScheduleItem> = emptyMap()

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
                courseMap = courses.associateBy { it.id }
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

                importantDatesMap = importantDates.associateBy { it.id }

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

    private fun createItems(importantDates: List<ScheduleItem>): List<ImportantDatesHeaderItemViewModel> {
        val importantDatesMap = importantDates.groupBy {
            it.startDate?.let {
                SimpleDateFormat("EEEE, MMMM dd", Locale.getDefault()).format(it)
            } ?: resources.getString(R.string.importantDatesNoDueDate)
        }
        return importantDatesMap.map {
            createDayGroup(it.key, it.value)
        }
    }

    private fun createDayGroup(dateTitle: String, items: List<ScheduleItem>): ImportantDatesHeaderItemViewModel {
        return ImportantDatesHeaderItemViewModel(
                ImportantDatesHeaderViewData(dateTitle),
                createImportantDateItems(items)
        )
    }

    private fun createImportantDateItems(items: List<ScheduleItem>): List<ImportantDatesItemViewModel> {
        return items.map {
            val color = if (courseMap.containsKey(it.courseId)) colorKeeper.getOrGenerateColor(courseMap[it.courseId]) else ThemedColor(resources.getColor(R.color.textInfo))
            ImportantDatesItemViewModel(
                    ImportantDatesItemViewData(
                            scheduleItemId = it.id,
                            title = it.title ?: "",
                            courseName = courseMap[it.courseId]?.name ?: "",
                            courseColor = color,
                            icon = getIcon(it)
                    ),
                    this@ImportantDatesViewModel::open
            )
        }
    }

    @DrawableRes
    private fun getIcon(scheduleItem: ScheduleItem): Int {
        return if (scheduleItem.assignment == null) {
            R.drawable.ic_calendar
        } else {
            if (scheduleItem.assignment!!.getSubmissionTypes().contains(Assignment.SubmissionType.EXTERNAL_TOOL) ||
                    scheduleItem.assignment!!.getSubmissionTypes().contains(Assignment.SubmissionType.ONLINE_QUIZ)) {
                R.drawable.ic_quiz
            } else {
                R.drawable.ic_assignment
            }
        }
    }

    private fun getCourseColor(course: Course?): String {
        return when {
            !course?.courseColor.isNullOrEmpty() -> course?.courseColor!!
            else -> ColorApiHelper.K5_DEFAULT_COLOR
        }
    }

    private fun open(scheduleItemId: Long) {
        val scheduleItem = importantDatesMap[scheduleItemId]
        val course = courseMap[scheduleItem?.courseId]
        val canvasContext = CanvasContext.fromContextCode(course?.contextId)
        if (scheduleItem != null && canvasContext != null) {
            if (scheduleItem.assignment == null) {
                _events.postValue(Event(ImportantDatesAction.OpenCalendarEvent(canvasContext, scheduleItem)))
            } else {
                _events.postValue(Event(ImportantDatesAction.OpenAssignment(canvasContext, scheduleItem.assignment!!.id)))
            }
        } else {
            _events.postValue(Event(ImportantDatesAction.ShowToast(resources.getString(R.string.errorOccurred))))
        }
    }
}