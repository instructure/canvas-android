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
import android.util.Range
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.managers.AssignmentManager
import com.instructure.canvasapi2.managers.CalendarEventManager
import com.instructure.canvasapi2.managers.CourseManager
import com.instructure.canvasapi2.managers.PlannerManager
import com.instructure.canvasapi2.managers.UserManager
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.PlannableType
import com.instructure.canvasapi2.models.PlannerItem
import com.instructure.canvasapi2.models.PlannerOverride
import com.instructure.canvasapi2.models.ScheduleItem
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.canvasapi2.utils.toDate
import com.instructure.pandautils.R
import com.instructure.pandautils.binding.GroupItemViewModel
import com.instructure.pandautils.features.elementary.schedule.itemviewmodels.ScheduleCourseItemViewModel
import com.instructure.pandautils.features.elementary.schedule.itemviewmodels.ScheduleDayGroupItemViewModel
import com.instructure.pandautils.features.elementary.schedule.itemviewmodels.ScheduleEmptyItemViewModel
import com.instructure.pandautils.features.elementary.schedule.itemviewmodels.ScheduleMissingItemViewModel
import com.instructure.pandautils.features.elementary.schedule.itemviewmodels.ScheduleMissingItemsGroupItemViewModel
import com.instructure.pandautils.features.elementary.schedule.itemviewmodels.SchedulePlannerItemTagItemViewModel
import com.instructure.pandautils.features.elementary.schedule.itemviewmodels.SchedulePlannerItemViewModel
import com.instructure.pandautils.mvvm.Event
import com.instructure.pandautils.mvvm.ItemViewModel
import com.instructure.pandautils.mvvm.ViewState
import com.instructure.pandautils.utils.ColorApiHelper
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.pandautils.utils.MissingItemsPrefs
import com.instructure.pandautils.utils.color
import com.instructure.pandautils.utils.date.DateTimeProvider
import com.instructure.pandautils.utils.getLastSunday
import com.instructure.pandautils.utils.getNextSaturday
import com.instructure.pandautils.utils.isNextDay
import com.instructure.pandautils.utils.isPreviousDay
import com.instructure.pandautils.utils.isSameDay
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ScheduleViewModel @Inject constructor(
        private val apiPrefs: ApiPrefs,
        private val resources: Resources,
        private val plannerManager: PlannerManager,
        private val courseManager: CourseManager,
        private val userManager: UserManager,
        private val calendarEventManager: CalendarEventManager,
        private val assignmentManager: AssignmentManager,
        private val missingItemsPrefs: MissingItemsPrefs,
        private val dateTimeProvider: DateTimeProvider,
        private val colorKeeper: ColorKeeper
) : ViewModel() {

    private lateinit var startDate: Date

    private lateinit var missingSubmissions: List<Assignment>
    private lateinit var calendarEvents: Map<Long?, ScheduleItem?>
    private lateinit var discussions: Map<Long?, Assignment?>
    private lateinit var plannerItems: List<PlannerItem>
    private lateinit var coursesMap: Map<Long, Course>

    private var todayHeader: ScheduleDayGroupItemViewModel? = null
    private val simpleDateFormat = SimpleDateFormat("hh:mm aa", Locale.getDefault())

    var todayPosition: Int = -1

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
        startDate = dateString.toDate() ?: dateTimeProvider.getCalendar().time
        getData(false)
    }

    fun refresh(forceNetwork: Boolean = true) {
        _state.postValue(ViewState.Refresh)
        getData(forceNetwork)
    }

    private fun jumpToToday() {
        if (todayPosition != -1) {
            _events.postValue(Event(ScheduleAction.JumpToToday))
        }
    }

    private fun getData(forceNetwork: Boolean) {
        viewModelScope.launch {
            try {
                val weekStart = startDate.getLastSunday()

                val courses = courseManager.getCoursesAsync(forceNetwork).await()
                coursesMap = courses.dataOrThrow
                        .associateBy { it.id }

                courses.dataOrThrow.forEach {
                    colorKeeper.addToCache(it.contextId, getCourseColor(it))
                }

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

                discussions =
                        plannerItems.filter { (it.plannableType == PlannableType.ANNOUNCEMENT || it.plannableType == PlannableType.DISCUSSION_TOPIC) && it.courseId != null }
                                .map { assignmentManager.getAssignmentAsync(it.plannable.id, it.courseId!!, forceNetwork) }
                                .awaitAll()
                                .map { it.dataOrNull }
                                .associateBy { it?.id }

                val itemViewModels = mutableListOf<ScheduleDayGroupItemViewModel>()
                for (i in 0..6) {
                    val calendar = dateTimeProvider.getCalendar()
                    calendar.time = weekStart
                    calendar.add(Calendar.DATE, i)
                    val date = calendar.time

                    itemViewModels.add(createItemsForDate(date))
                }
                _data.postValue(ScheduleViewData(itemViewModels))
                _state.postValue(ViewState.Success)
                todayPosition = calculateTodayPosition(itemViewModels)
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

        val today = dateTimeProvider.getCalendar().time
        if (date.isSameDay(today) && missingSubmissions.isNotEmpty()) {
            items.add(createMissingItems())
        }

        val dayHeader = createDayHeader(date, items)

        if (date.isSameDay(today)) {
            todayHeader = dayHeader
        }

        return dayHeader
    }

    private fun createMissingItems(): ScheduleMissingItemsGroupItemViewModel {
        val missingItems = missingSubmissions.map { assignment ->
            val color = if (coursesMap.containsKey(assignment.courseId)) coursesMap[assignment.courseId].color else resources.getColor(R.color.textInfo)
            ScheduleMissingItemViewModel(
                    data = ScheduleMissingItemData(
                            title = assignment.name,
                            dueString = assignment.dueDate?.let {
                                resources.getString(
                                        R.string.schedule_due_text,
                                        simpleDateFormat.format(it)
                                )
                            },
                            points = getPointsText(assignment.pointsPossible, assignment.courseId),
                            type = if (assignment.discussionTopicHeader != null) PlannerItemType.DISCUSSION else PlannerItemType.ASSIGNMENT,
                            courseName = coursesMap[assignment.courseId]?.name,
                            courseColor = color,
                            contentDescription = createMissingItemContentDescription(assignment)
                    ),
                    open = {
                        val course = coursesMap[assignment.courseId]
                        if (course != null) {
                            if (assignment.discussionTopicHeader != null) {
                                _events.postValue(
                                        Event(
                                                ScheduleAction.OpenDiscussion(
                                                        course,
                                                        assignment.discussionTopicHeader!!.id,
                                                        assignment.discussionTopicHeader!!.title
                                                                ?: ""
                                                )
                                        )
                                )
                            } else {
                                _events.postValue(Event(ScheduleAction.OpenAssignment(course, assignment.id)))
                            }
                        }
                    }
            )
        }
        return ScheduleMissingItemsGroupItemViewModel(missingItemsPrefs = missingItemsPrefs, items = missingItems)
    }

    private fun createMissingItemContentDescription(assignment: Assignment): String {
        val typeContentDescription = if (assignment.discussionTopicHeader != null) resources.getString(R.string.a11y_discussion_topic) else resources.getString(R.string.a11y_assignment)
        val pointsContentDescription = resources.getQuantityString(R.plurals.a11y_schedule_points, assignment.pointsPossible.toInt(), assignment.pointsPossible)
        val dueContentDescription = assignment.dueDate?.let {
            resources.getString(
                    R.string.schedule_due_text,
                    simpleDateFormat.format(it)
            )
        }
        val courseContentDescription = resources.getString(R.string.a11y_schedule_course_header_content_description, coursesMap[assignment.courseId]?.name)

        return "$typeContentDescription ${assignment.name} $courseContentDescription $pointsContentDescription $dueContentDescription"
    }

    private fun createDayHeader(date: Date, items: List<ItemViewModel>): ScheduleDayGroupItemViewModel {
        return ScheduleDayGroupItemViewModel(
                getTitleForDate(date),
                SimpleDateFormat("MMMM dd", Locale.getDefault()).format(date),
                !dateTimeProvider.getCalendar().time.isSameDay(date),
                items
        )
    }

    private fun getTitleForDate(date: Date): String {
        val today = dateTimeProvider.getCalendar().time
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
                .groupBy { coursesMap[it.courseId ?: it.plannable.courseId] }


        val courseViewModels = coursePlannerMap.entries
                .sortedBy { it.key?.name }
                .map { entry ->
                    val color = if (coursesMap.containsKey(entry.key?.id)) coursesMap[entry.key?.id].color else resources.getColor(R.color.textInfo)
                    val scheduleViewData = ScheduleCourseViewData(
                            entry.key?.name ?: resources.getString(R.string.schedule_todo_title),
                            entry.key != null && !entry.key!!.homeroomCourse,
                            color,
                            entry.key?.imageUrl ?: "",
                            entry.value.map {
                                createPlannerItemViewModel(it)
                            }
                    )
                    ScheduleCourseItemViewModel(
                            scheduleViewData
                    ) {
                        entry.key?.let { course ->
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
            chips.add(PlannerItemTag.Graded)
        }

        if (plannerItem.submissionState?.excused == true) {
            chips.add(PlannerItemTag.Excused)
        }

        if (plannerItem.submissionState?.withFeedback == true) {
            chips.add(PlannerItemTag.Feedback)
        }

        if (plannerItem.submissionState?.missing == true) {
            chips.add(PlannerItemTag.Missing)
        }

        if (plannerItem.submissionState?.late == true) {
            chips.add(PlannerItemTag.Late)
        }

        if (plannerItem.submissionState?.redoRequest == true) {
            chips.add(PlannerItemTag.Redo)
        }

        if (plannerItem.plannableType == PlannableType.DISCUSSION_TOPIC || plannerItem.plannableType == PlannableType.ANNOUNCEMENT) {
            val discussion = discussions[plannerItem.plannable.id]
            discussion?.discussionTopicHeader?.unreadCount?.let { unreadCount ->
                if (unreadCount > 0) {
                    chips.add(PlannerItemTag.Replies(unreadCount))
                }
            }
        }

        return chips.map {
            SchedulePlannerItemTagItemViewModel(
                    SchedulePlannerItemTag(
                            if (it is PlannerItemTag.Replies) resources.getQuantityString(it.text, it.replyCount, it.replyCount)
                            else resources.getString(it.text),
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
                        getPointsText(plannerItem.plannable.pointsPossible, plannerItem.courseId ?: 0),
                        getDueText(plannerItem),
                        isPlannableOpenable(plannerItem),
                        createContentDescription(plannerItem),
                        createChips(plannerItem)
                ),
                plannerItem.plannerOverride?.markedComplete ?: false || plannerItem.submissionState?.submitted ?: false,
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

    private fun createContentDescription(plannerItem: PlannerItem): String {
        val typeContentDescription = createTypeContentDescription(plannerItem.plannableType)
        val dateContentDescription = getDueText(plannerItem)
        val markedAsDoneContentDescription =
                if (plannerItem.plannerOverride?.markedComplete == true || plannerItem.submissionState?.submitted == true) resources.getString(R.string.a11y_marked_as_done) else resources.getString(
                        R.string.a11y_not_marked_as_done
                )

        return "$typeContentDescription ${plannerItem.plannable.title} $dateContentDescription $markedAsDoneContentDescription"
    }

    private fun createTypeContentDescription(plannableType: PlannableType): String {
        return when (plannableType) {
            PlannableType.ANNOUNCEMENT -> resources.getString(R.string.a11y_announcement)
            PlannableType.DISCUSSION_TOPIC -> resources.getString(R.string.a11y_discussion_topic)
            PlannableType.CALENDAR_EVENT -> resources.getString(R.string.a11y_calendar_event)
            PlannableType.ASSIGNMENT, PlannableType.SUB_ASSIGNMENT -> resources.getString(R.string.a11y_assignment)
            PlannableType.PLANNER_NOTE -> resources.getString(R.string.a11y_planner_note)
            PlannableType.QUIZ -> resources.getString(R.string.a11y_quiz)
            PlannableType.TODO -> resources.getString(R.string.a11y_todo)
            PlannableType.WIKI_PAGE -> resources.getString(R.string.a11y_page)
            PlannableType.ASSESSMENT_REQUEST -> resources.getString(R.string.a11y_assessment_request)
        }
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

                announceForA11y(
                        if (markedAsDone) resources.getString(R.string.a11y_schedule_marked_as_done, plannerItem.plannable.title)
                        else resources.getString(R.string.a11y_schedule_marked_as_not_done, plannerItem.plannable.title)
                )
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

                announceForA11y(resources.getString(R.string.a11y_error_occured))
            }
        }

    }

    private fun announceForA11y(announcement: String) {
        _events.postValue(Event(ScheduleAction.AnnounceForAccessibility(announcement)))
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
            PlannableType.ASSIGNMENT, PlannableType.SUB_ASSIGNMENT -> PlannerItemType.ASSIGNMENT
            PlannableType.ANNOUNCEMENT -> PlannerItemType.ANNOUNCEMENT
            PlannableType.QUIZ -> PlannerItemType.QUIZ
            PlannableType.WIKI_PAGE -> PlannerItemType.PAGE
            PlannableType.CALENDAR_EVENT -> PlannerItemType.CALENDAR_EVENT
            PlannableType.DISCUSSION_TOPIC -> PlannerItemType.DISCUSSION
            PlannableType.PLANNER_NOTE -> PlannerItemType.TO_DO
            PlannableType.TODO -> PlannerItemType.TO_DO
            PlannableType.ASSESSMENT_REQUEST -> PlannerItemType.ASSESSMENT_REQUEST
        }
    }

    private fun getPointsText(points: Double?, courseId: Long): String? {
        if (points == null) return null

        val course = coursesMap[courseId]
        if (course?.settings?.restrictQuantitativeData == true) return null

        val numberFormatter = DecimalFormat("##.##")
        return resources.getQuantityString(R.plurals.schedule_points, points.toInt(), numberFormatter.format(points))
    }

    private fun getDueText(plannerItem: PlannerItem): String {
        return when (plannerItem.plannableType) {
            PlannableType.ANNOUNCEMENT -> simpleDateFormat.format(plannerItem.plannableDate)
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

    private fun calculateTodayPosition(items: List<ScheduleDayGroupItemViewModel>): Int {
        var position = -1
        if (todayHeader != null) {
            items.forEach {
                position++
                if (it == todayHeader) return position
                position += getGroupOpenChildCount(it)
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
        return when {
            !course?.courseColor.isNullOrEmpty() -> course?.courseColor!!
            else -> ColorApiHelper.K5_DEFAULT_COLOR
        }
    }

    fun getTodayRange(): Range<Int>? {
        todayHeader?.let {
            return Range(todayPosition, todayPosition + getGroupOpenChildCount(it))
        }
        return null
    }
}