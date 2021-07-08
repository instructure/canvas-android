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
import com.instructure.canvasapi2.managers.*
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.PlannerItem
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.pandautils.R
import com.instructure.pandautils.features.elementary.homeroom.HomeroomAction
import com.instructure.pandautils.features.elementary.homeroom.HomeroomViewData
import com.instructure.pandautils.features.elementary.schedule.itemviewmodels.*
import com.instructure.pandautils.mvvm.Event
import com.instructure.pandautils.mvvm.ItemViewModel
import com.instructure.pandautils.mvvm.ViewState
import com.instructure.pandautils.utils.ColorApiHelper
import com.instructure.pandautils.utils.isNextDay
import com.instructure.pandautils.utils.isPreviousDay
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
        private val assignmentManager: AssignmentManager,
        private val announcementManager: AnnouncementManager) : ViewModel() {

    private lateinit var assignmentMap: Map<Long?, Assignment?>
    private lateinit var plannerItems: List<PlannerItem>
    private lateinit var coursesMap: Map<Long, Course>

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

            val courses = courseManager.getCoursesAsync(true).await()
            coursesMap = courses.dataOrThrow
                    .filter { !it.homeroomCourse }
                    .associateBy { it.id }

            plannerItems = plannerManager.getPlannerItemsAsync(
                    true,
                    weekStart.toApiString(),
                    DateHelper.getNextSaturday(startDate).toApiString())
                    .await()
                    .dataOrNull
                    .orEmpty()

            assignmentMap = plannerItems
                    .filter { it.courseId != null }
                    .map { assignmentManager.getAssignmentAsync(it.plannable.id, it.courseId!!, true) }
                    .awaitAll()
                    .map { it.dataOrNull }
                    .associateBy { it?.id }

            val itemViewModels = mutableListOf<ItemViewModel>()
            for (i in 0..6) {
                val calendar = Calendar.getInstance()
                calendar.time = weekStart
                calendar.set(Calendar.DAY_OF_WEEK, calendar.get(Calendar.DAY_OF_WEEK) + i)
                val date = calendar.time

                itemViewModels.addAll(createCourseItemsForDate(date))
            }
            _data.postValue(ScheduleViewData(itemViewModels))
        }
    }

    fun jumpToToday() {

    }

    private fun createCourseItemsForDate(date: Date): List<ItemViewModel> {
        val items = mutableListOf<ItemViewModel>()
        items.add(createDayHeader(date))
        items.addAll(createCourseItems(date))

        return items
    }

    private fun createDayHeader(date: Date): ScheduleDayHeaderItemViewModel {
        return ScheduleDayHeaderItemViewModel(
                getTitleForDate(date),
                SimpleDateFormat("MMMM dd", Locale.getDefault()).format(date),
                !Date().isSameDay(date),
                this@ScheduleViewModel::jumpToToday)
    }

    private fun getTitleForDate(date: Date): String {
        val today = Date()
        if (date.isSameDay(today)) return resources.getString(R.string.today)
        if (date.isNextDay(today)) return resources.getString(R.string.tomorrow)
        if (date.isPreviousDay(today)) return resources.getString(R.string.yesterday)
        return SimpleDateFormat("EEEE", Locale.getDefault()).format(date)
    }

    private fun createCourseItems(date: Date): List<ItemViewModel> {
        val coursePlannerMap = plannerItems
                .filter {
                    date.isSameDay(it.plannableDate)
                }
                .groupBy { coursesMap[it.courseId] }


        val courseViewModels = coursePlannerMap.entries.map {
            val scheduleViewData = ScheduleCourseViewData(
                    it.key?.name ?: "To Do",
                    it.key != null,
                    getCourseColor(it.key),
                    it.key?.imageUrl ?: "",
                    it.value.map {
                        createPlannerItemViewModel(it)
                    }
            )
            ScheduleCourseItemViewModel(
                    scheduleViewData,
                    {}
            )
        }

        return if (courseViewModels.isEmpty()) {
            listOf(ScheduleEmptyItemViewModel(
                    ScheduleEmptyViewData(resources.getString(R.string.nothing_planned_yet))
            ))
        } else {
            courseViewModels
        }
    }

    private fun createChips(plannerItem: PlannerItem): List<SchedulePlannerItemTagItemViewModel> {
        val chips = mutableListOf<PlannerItemTag>()
        val assignment = assignmentMap[plannerItem.plannable.id]

        if (assignment != null) {
            if (assignment.submission?.isGraded == true && assignment.submission?.excused == false) {
                chips.add(PlannerItemTag.GRADED)
            }

            if (assignment.submission?.excused == true) {
                chips.add(PlannerItemTag.EXCUSED)
            }

            if (assignment.submission?.submissionComments?.isNotEmpty() == true) {
                chips.add(PlannerItemTag.FEEDBACK)
            }

            if (assignment.submission?.late == true) {
                chips.add(PlannerItemTag.LATE)
            }

            if (assignment.discussionTopicHeader != null && assignment.discussionTopicHeader!!.unreadCount > 0) {
                chips.add(PlannerItemTag.REPLIES)
            }

        }

        return chips.map {
            SchedulePlannerItemTagItemViewModel(
                    SchedulePlannerItemTag(
                            resources.getString(it.text),
                            resources.getColor(it.color)
                    )
            )
        }
    }

    private fun createPlannerItemViewModel(plannerItem: PlannerItem): SchedulePlannerItemViewModel {
        return SchedulePlannerItemViewModel(
                SchedulePlannerItemData(
                        plannerItem.plannable.title,
                        PlannerItemType.ASSIGNMENT,
                        plannerItem.plannable.pointsPossible,
                        "Due ${SimpleDateFormat("hh:mm aa", Locale.getDefault()).format(plannerItem.plannableDate)}",
                        true,
                        createChips(plannerItem)
                ),
                {},
                {}
        )
    }

    private fun getCourseColor(course: Course?): String {
        return if (!course?.courseColor.isNullOrEmpty()) {
            course?.courseColor!!
        } else {
            ColorApiHelper.K5_DEFAULT_COLOR
        }
    }
}