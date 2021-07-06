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

import androidx.annotation.DrawableRes
import com.instructure.canvasapi2.models.Course
import com.instructure.pandautils.R
import com.instructure.pandautils.features.elementary.schedule.itemviewmodels.SchedulePlannerItemViewModel
import com.instructure.pandautils.mvvm.ItemViewModel
import java.util.*

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
        val points: Double?,
        val dueDate: String?,
        val openable: Boolean
)

enum class PlannerItemType(@DrawableRes val iconRes: Int) {
    ANNOUNCEMENT(R.drawable.ic_announcement),
    ASSIGNMENT(R.drawable.ic_assignment),
    QUIZ(R.drawable.ic_quiz),
    DISCUSSION(R.drawable.ic_discussion),
    PEER_REVIEW(R.drawable.ic_peer_review),
    CALENDAR_EVENT(R.drawable.ic_calendar),
    PAGE(R.drawable.ic_pages)
}

enum class ScheduleItemViewModelType(val viewType: Int) {
    COURSE(1),
    DAY_HEADER(2),
    PLANNER_ITEM(3),
    EMPTY(4)
}

sealed class ScheduleAction {

}