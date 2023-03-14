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

import androidx.annotation.*
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.pandautils.R
import com.instructure.pandautils.features.elementary.schedule.itemviewmodels.ScheduleDayGroupItemViewModel
import com.instructure.pandautils.features.elementary.schedule.itemviewmodels.SchedulePlannerItemTagItemViewModel
import com.instructure.pandautils.features.elementary.schedule.itemviewmodels.SchedulePlannerItemViewModel
import com.instructure.pandautils.mvvm.ItemViewModel

data class ScheduleViewData(val itemViewModels: List<ScheduleDayGroupItemViewModel>)

data class ScheduleCourseViewData(
    val courseName: String,
    val openable: Boolean,
    @ColorInt val courseColor: Int,
    val imageUrl: String,
    val plannerItems: List<SchedulePlannerItemViewModel>
)

data class SchedulePlannerItemData(
        val title: String,
        val type: PlannerItemType,
        val points: String?,
        val dueDate: String?,
        val openable: Boolean,
        val contentDescription: String,
        val chips: List<SchedulePlannerItemTagItemViewModel>
)

data class ScheduleEmptyViewData(
        val title: String
)

data class SchedulePlannerItemTag(
        val text: String,
        @ColorInt val color: Int
)

data class ScheduleMissingItemData(
    val title: String?,
    val dueString: String?,
    val points: String?,
    val type: PlannerItemType,
    val courseName: String?,
    @ColorInt val courseColor: Int,
    val contentDescription: String
)

enum class PlannerItemType(@DrawableRes val iconRes: Int) {
    ANNOUNCEMENT(R.drawable.ic_announcement),
    ASSIGNMENT(R.drawable.ic_assignment),
    QUIZ(R.drawable.ic_quiz),
    DISCUSSION(R.drawable.ic_discussion),
    PEER_REVIEW(R.drawable.ic_peer_review),
    CALENDAR_EVENT(R.drawable.ic_calendar),
    PAGE(R.drawable.ic_pages),
    TO_DO(R.drawable.ic_calendar)
}

enum class ScheduleItemViewModelType(val viewType: Int) {
    COURSE(1),
    DAY_HEADER(2),
    PLANNER_ITEM(3),
    EMPTY(4),
    MISSING_HEADER(5),
    MISSING_ITEM(6)
}

sealed class PlannerItemTag(val text: Int, @ColorRes val color: Int) {
    object Excused: PlannerItemTag(R.string.schedule_tag_excused, R.color.textDark)
    object Graded : PlannerItemTag(R.string.schedule_tag_graded, R.color.textDark)
    data class Replies(val replyCount: Int) : PlannerItemTag(R.plurals.schedule_tag_replies, R.color.textDark)
    object Feedback : PlannerItemTag(R.string.schedule_tag_feedback, R.color.textDark)
    object Late : PlannerItemTag(R.string.schedule_tag_late, R.color.textDanger)
    object Redo : PlannerItemTag(R.string.schedule_tag_redo, R.color.textDanger)
    object Missing : PlannerItemTag(R.string.schedule_tag_missing, R.color.textDanger)
}

sealed class ScheduleAction {
    data class OpenCourse(val course: Course) : ScheduleAction()
    data class OpenAssignment(val canvasContext: CanvasContext, val assignmentId: Long) : ScheduleAction()
    data class OpenCalendarEvent(val canvasContext: CanvasContext, val scheduleItemId: Long) : ScheduleAction()
    data class OpenQuiz(val canvasContext: CanvasContext, val htmlUrl: String) : ScheduleAction()
    data class OpenDiscussion(val canvasContext: CanvasContext, val id: Long, val title: String) : ScheduleAction()
    data class AnnounceForAccessibility(val announcement: String): ScheduleAction()
    object JumpToToday : ScheduleAction()
}