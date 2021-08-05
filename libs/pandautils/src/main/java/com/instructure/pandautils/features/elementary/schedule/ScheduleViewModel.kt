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
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.managers.*
import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.canvasapi2.utils.toDate
import com.instructure.pandautils.R
import com.instructure.pandautils.binding.GroupItemViewModel
import com.instructure.pandautils.features.elementary.schedule.itemviewmodels.*
import com.instructure.pandautils.mvvm.Event
import com.instructure.pandautils.mvvm.ItemViewModel
import com.instructure.pandautils.mvvm.ViewState
import com.instructure.pandautils.utils.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val apiPrefs: ApiPrefs,
    private val resources: Resources,
    private val plannerManager: PlannerManager,
    private val courseManager: CourseManager,
    private val userManager: UserManager,
    private val calendarEventManager: CalendarEventManager
) : ViewModel() {

    private lateinit var startDate: Date

    private lateinit var missingSubmissions: List<Assignment>
    private lateinit var calendarEvents: Map<Long?, ScheduleItem?>
    private lateinit var plannerItems: List<PlannerItem>
    private lateinit var coursesMap: Map<Long, Course>

    private var todayHeader: ScheduleDayGroupItemViewModel? = null
    private val simpleDateFormat = SimpleDateFormat("hh:mm aa", Locale.getDefault())

    val state: LiveData<ViewState>
        get() = _state
    private val _state = MutableLiveData<ViewState>()

    val data: LiveData<ScheduleViewData>
        get() = _data
    private val _data = MutableLiveData<ScheduleViewData>()

    val events: LiveData<Event<ScheduleAction>>
        get() = _events
    private val _events = MutableLiveData<Event<ScheduleAction>>()

    fun getDataForDate(dateString: String) {
        _state.postValue(ViewState.Loading)
        startDate = dateString.toDate() ?: Date()
        getData(false)
    }

    fun refresh() {
        _state.postValue(ViewState.Refresh)
        getData(true)
    }

    private fun jumpToToday() {
        val todayPos = calculateTodayPosition()
        if (todayPos != -1) {
            _events.postValue(Event(ScheduleAction.JumpToToday(todayPos)))
        }
    }

    private fun getData(forceNetwork: Boolean) {
        viewModelScope.launch {
            try {
                val weekStart = startDate.getLastSunday()

                val courses = courseManager.getCoursesAsync(forceNetwork).await()
                coursesMap = courses.dataOrThrow
                    .filter { !it.homeroomCourse }
                    .associateBy { it.id }

                plannerItems = plannerManager.getPlannerItemsAsync(
                    forceNetwork,
                    weekStart.toApiString(),
                    startDate.getNextSaturday().toApiString()
                )
                    .await()
                    .dataOrNull
                    .orEmpty()

                missingSubmissions =
                    userManager.getAllMissingSubmissionsAsync(forceNetwork).await().dataOrNull.orEmpty()

                calendarEvents = plannerItems.filter { it.plannableType == PlannableType.CALENDAR_EVENT }
                    .map { calendarEventManager.getCalendarEventAsync(it.plannable.id, forceNetwork) }
                    .awaitAll()
                    .map { it.dataOrNull }
                    .associateBy { it?.id }

                val itemViewModels = mutableListOf<ItemViewModel>()
                for (i in 0..6) {
                    val calendar = Calendar.getInstance()
                    calendar.time = weekStart
                    calendar.add(Calendar.DATE, i)
                    val date = calendar.time

                    itemViewModels.add(createItemsForDate(date))
                }
                _data.postValue(ScheduleViewData(itemViewModels))
                _state.postValue(ViewState.Success)
                jumpToToday()
            } catch (e: Exception) {
                e.printStackTrace()
                _state.postValue(ViewState.Error(resources.getString(R.string.schedule_error_message)))
            }
        }
    }

    private fun createItemsForDate(date: Date): ScheduleDayGroupItemViewModel {
        val items = mutableListOf<ItemViewModel>()

        items.addAll(createCourseItems(date))

        if (date.isSameDay(Date()) && missingSubmissions.isNotEmpty()) {
            items.add(createMissingItems())
        }

        val dayHeader = createDayHeader(date, items)

        if (date.isSameDay(Date())) {
            todayHeader = dayHeader
        }

        return dayHeader
    }

    private fun createMissingItems(): ScheduleMissingItemsGroupItemViewModel {
        val missingItems = missingSubmissions.map {
            ScheduleMissingItemViewModel(
                data = ScheduleMissingItemData(
                    it.name,
                    it.dueDate?.let { resources.getString(R.string.schedule_due_text, simpleDateFormat.format(it)) },
                    getPointsText(it.pointsPossible),
                    if (it.discussionTopicHeader != null) PlannerItemType.DISCUSSION else PlannerItemType.ASSIGNMENT,
                    coursesMap[it.courseId]?.name,
                    getCourseColor(coursesMap[it.courseId])
                ),
                open = {
                    val course = coursesMap[it.courseId]
                    if (course != null) {
                        if (it.discussionTopicHeader != null) {
                            _events.postValue(
                                Event(
                                    ScheduleAction.OpenDiscussion(
                                        course, it.discussionTopicHeader!!.id, it.discussionTopicHeader!!.title
                                            ?: ""
                                    )
                                )
                            )
                        } else {
                            _events.postValue(Event(ScheduleAction.OpenAssignment(course, it.id)))
                        }
                    }
                }
            )
        }
        return ScheduleMissingItemsGroupItemViewModel(items = missingItems)
    }

    private fun createDayHeader(date: Date, items: List<ItemViewModel>): ScheduleDayGroupItemViewModel {
        return ScheduleDayGroupItemViewModel(
            getTitleForDate(date),
            SimpleDateFormat("MMMM dd", Locale.getDefault()).format(date),
            !Date().isSameDay(date),
            items
        )
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
                it.key?.name ?: resources.getString(R.string.schedule_todo_title),
                it.key != null,
                getCourseColor(it.key),
                it.key?.imageUrl ?: "",
                it.value.map {
                    createPlannerItemViewModel(it)
                }
            )
            ScheduleCourseItemViewModel(
                scheduleViewData
            ) {
                it.key?.let { course ->
                    _events.postValue(Event(ScheduleAction.OpenCourse(course)))
                }
            }
        }

        return if (courseViewModels.isEmpty()) {
            listOf(
                ScheduleEmptyItemViewModel(
                    ScheduleEmptyViewData(resources.getString(R.string.nothing_planned_yet))
                )
            )
        } else {
            courseViewModels
        }
    }

    private fun createChips(plannerItem: PlannerItem): List<SchedulePlannerItemTagItemViewModel> {
        val chips = mutableListOf<PlannerItemTag>()

        if (plannerItem.submissionState?.graded == true && plannerItem.submissionState?.excused == false) {
            chips.add(PlannerItemTag.GRADED)
        }

        if (plannerItem.submissionState?.excused == true) {
            chips.add(PlannerItemTag.EXCUSED)
        }

        if (plannerItem.submissionState?.withFeedback == true) {
            chips.add(PlannerItemTag.FEEDBACK)
        }

        if (plannerItem.submissionState?.late == true) {
            chips.add(PlannerItemTag.LATE)
        }

        if (plannerItem.submissionState?.redoRequest == true) {
            chips.add(PlannerItemTag.REDO)
        }

        if (plannerItem.newActivity == true) {
            chips.add(PlannerItemTag.REPLIES)
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
                getTypeForPlannerItem(plannerItem),
                getPointsText(plannerItem.plannable.pointsPossible),
                getDueText(plannerItem),
                isPlannableOpenable(plannerItem),
                createChips(plannerItem)
            ),
            plannerItem.plannerOverride?.markedComplete ?: false,
            { scheduleItemViewModel, markedAsDone ->
                updatePlannerOverride(
                    plannerItem,
                    scheduleItemViewModel,
                    markedAsDone
                )
            },
            { openPlannable(plannerItem) }
        )
    }

    private fun updatePlannerOverride(
        plannerItem: PlannerItem,
        itemViewModel: SchedulePlannerItemViewModel,
        markedAsDone: Boolean
    ) {
        if (itemViewModel.completed == markedAsDone) return

        viewModelScope.launch {
            itemViewModel.apply {
                completed = markedAsDone
                notifyChange()
            }
            try {
                if (plannerItem.plannerOverride == null) {
                    val plannerOverride = PlannerOverride(
                        plannableType = plannerItem.plannableType,
                        plannableId = plannerItem.plannable.id,
                        markedComplete = markedAsDone
                    )
                    val createdOverride =
                        plannerManager.createPlannerOverrideAsync(true, plannerOverride).await().dataOrThrow
                    plannerItem.plannerOverride = createdOverride
                } else {
                    val updatedOverride =
                        plannerManager.updatePlannerOverrideAsync(true, markedAsDone, plannerItem.plannerOverride?.id!!)
                            .await().dataOrThrow
                    plannerItem.plannerOverride = updatedOverride
                }
            } catch (e: Exception) {
                e.printStackTrace()
                itemViewModel.apply {
                    completed = !markedAsDone
                    notifyChange()
                }
            }
        }

    }

    private fun openPlannable(plannerItem: PlannerItem) {
        when (plannerItem.plannableType) {
            PlannableType.ASSIGNMENT -> _events.postValue(
                Event(
                    ScheduleAction.OpenAssignment(
                        plannerItem.canvasContext,
                        plannerItem.plannable.id
                    )
                )
            )
            PlannableType.CALENDAR_EVENT -> _events.postValue(
                Event(
                    ScheduleAction.OpenCalendarEvent(
                        plannerItem.canvasContext,
                        plannerItem.plannable.id
                    )
                )
            )
            PlannableType.DISCUSSION_TOPIC -> _events.postValue(
                Event(
                    ScheduleAction.OpenDiscussion(
                        plannerItem.canvasContext,
                        plannerItem.plannable.id,
                        plannerItem.plannable.title
                    )
                )
            )
            PlannableType.QUIZ -> {
                if (plannerItem.plannable.assignmentId != null) {
                    // This is a quiz assignment, go to the assignment page
                    _events.postValue(
                        Event(
                            ScheduleAction.OpenAssignment(
                                plannerItem.canvasContext,
                                plannerItem.plannable.id
                            )
                        )
                    )
                } else {
                    var htmlUrl = plannerItem.htmlUrl.orEmpty()
                    if (htmlUrl.startsWith('/')) htmlUrl = apiPrefs.fullDomain + htmlUrl
                    _events.postValue(Event(ScheduleAction.OpenQuiz(plannerItem.canvasContext, htmlUrl)))
                }
            }
            PlannableType.ANNOUNCEMENT -> _events.postValue(
                Event(
                    ScheduleAction.OpenDiscussion(
                        plannerItem.canvasContext,
                        plannerItem.plannable.id,
                        plannerItem.plannable.title
                    )
                )
            )
            else -> Unit
        }
    }

    private fun isPlannableOpenable(plannerItem: PlannerItem): Boolean {
        return when (plannerItem.plannableType) {
            PlannableType.PLANNER_NOTE -> false
            PlannableType.CALENDAR_EVENT -> true
            else -> plannerItem.courseId != null
        }
    }

    private fun getTypeForPlannerItem(plannerItem: PlannerItem): PlannerItemType {
        return when (plannerItem.plannableType) {
            PlannableType.ASSIGNMENT -> PlannerItemType.ASSIGNMENT
            PlannableType.ANNOUNCEMENT -> PlannerItemType.ANNOUNCEMENT
            PlannableType.QUIZ -> PlannerItemType.QUIZ
            PlannableType.WIKI_PAGE -> PlannerItemType.PAGE
            PlannableType.CALENDAR_EVENT -> PlannerItemType.CALENDAR_EVENT
            PlannableType.DISCUSSION_TOPIC -> PlannerItemType.DISCUSSION
            PlannableType.PLANNER_NOTE -> PlannerItemType.TO_DO
            PlannableType.TODO -> PlannerItemType.TO_DO
        }
    }

    private fun getPointsText(points: Double?): String? {
        if (points == null) return null
        val numberFormatter = DecimalFormat("##.##")
        return resources.getQuantityString(R.plurals.schedule_points, points.toInt(), numberFormatter.format(points))
    }

    private fun getDueText(plannerItem: PlannerItem): String {
        return when (plannerItem.plannableType) {
            PlannableType.CALENDAR_EVENT -> getCalendarEventDueText(plannerItem)
            PlannableType.PLANNER_NOTE -> resources.getString(
                R.string.schedule_todo_due_text,
                simpleDateFormat.format(plannerItem.plannable.todoDate.toDate() ?: plannerItem.plannableDate)
            )
            else -> resources.getString(R.string.schedule_due_text, simpleDateFormat.format(plannerItem.plannableDate))
        }
    }

    private fun getCalendarEventDueText(plannerItem: PlannerItem): String {
        val calendarEvent = calendarEvents[plannerItem.plannable.id]
        if (calendarEvent?.isAllDay == true) {
            return resources.getString(R.string.schedule_all_day_event_text)
        } else {
            val startText = calendarEvent?.startDate?.let { simpleDateFormat.format(it) }
            val endText = calendarEvent?.endDate?.let { simpleDateFormat.format(it) }
            if (startText != null && endText != null) {
                return resources.getString(R.string.schedule_calendar_event_interval_text, startText, endText)
            }
        }

        return resources.getString(
            R.string.schedule_calendar_event_due_text,
            simpleDateFormat.format(plannerItem.plannableDate)
        )
    }

    private fun calculateTodayPosition(): Int {
        var position = -1
        if (todayHeader != null) {
            val items = _data.value?.itemViewModels.orEmpty()
            items.forEach {
                position++
                if (it == todayHeader) return@forEach
                if (it is GroupItemViewModel) {
                    position += getGroupOpenChildCount(it)
                }
            }
        }
        return position
    }

    private fun getGroupOpenChildCount(group: GroupItemViewModel): Int {
        var childCount = 0
        if (!group.collapsed) {
            childCount += group.items.size
            group.items.filterIsInstance<GroupItemViewModel>().forEach {
                childCount += getGroupOpenChildCount(it)
            }
        }
        return childCount
    }

    private fun getCourseColor(course: Course?): String {
        return if (!course?.courseColor.isNullOrEmpty()) {
            course?.courseColor!!
        } else {
            ColorApiHelper.K5_DEFAULT_COLOR
        }
    }
}