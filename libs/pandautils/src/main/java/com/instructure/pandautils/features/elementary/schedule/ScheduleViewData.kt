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
import com.instructure.pandautils.features.elementary.schedule.itemviewmodels.SchedulePlannerItemTagItemViewModel
import com.instructure.pandautils.features.elementary.schedule.itemviewmodels.SchedulePlannerItemViewModel
import com.instructure.pandautils.mvvm.ItemViewModel

data class ScheduleViewData(val itemViewModels: List<ItemViewModel>)

data class ScheduleCourseViewData(
        val courseName: String,
        val openable: Boolean,
        val courseColor: String,
        val imageUrl: String,
        val plannerItems: List<SchedulePlannerItemViewModel>
)

data class SchedulePlannerItemData(
        val title: String,
        val type: PlannerItemType,
        val points: String?,
        val dueDate: String?,
        val openable: Boolean,
        val chips: List<SchedulePlannerItemTagItemViewModel>
)

data class ScheduleEmptyViewData(
        val title: String
)

data class SchedulePlannerItemTag(
        val text: String,
        @ColorInt val color: Int
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

enum class PlannerItemTag(@StringRes val text: Int, @ColorRes val color: Int) {
    EXCUSED(R.string.schedule_tag_excused, R.color.textLightGray),
    GRADED(R.string.schedule_tag_graded, R.color.textLightGray),
    REPLIES(R.string.schedule_tag_replies, R.color.textLightGray),
    FEEDBACK(R.string.schedule_tag_feedback, R.color.textLightGray),
    LATE(R.string.schedule_tag_late, R.color.canvasRed),
    REDO(R.string.schedule_tag_redo, R.color.canvasRed)
}

sealed class ScheduleAction {
    data class OpenCourse(val course: Course) : ScheduleAction()
    data class OpenAssignment(val canvasContext: CanvasContext, val assignmentId: Long) : ScheduleAction()
    data class OpenCalendarEvent(val canvasContext: CanvasContext, val scheduleItemId: Long) : ScheduleAction()
    data class OpenQuiz(val canvasContext: CanvasContext, val htmlUrl: String) : ScheduleAction()
    data class OpenDiscussion(val canvasContext: CanvasContext, val id: Long, val title: String) : ScheduleAction()
    data class JumpToToday(val position: Int) : ScheduleAction()
}