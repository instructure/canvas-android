/*
 * Copyright (C) 2019 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.student.mobius.syllabus

import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.ScheduleItem
import com.instructure.canvasapi2.utils.DataResult

sealed class SyllabusEvent {
    object PullToRefresh : SyllabusEvent()
    data class DataLoaded(val course: DataResult<Course>, val events: DataResult<List<ScheduleItem>>) : SyllabusEvent()
    data class SyllabusItemClicked(val itemId: String) : SyllabusEvent()
}

sealed class SyllabusEffect {
    data class LoadData(val courseId: Long, val forceNetwork: Boolean) : SyllabusEffect()
    data class ShowAssignmentView(val assignmentId: Long, val course: Course) : SyllabusEffect()
    data class ShowScheduleItemView(val scheduleItem: ScheduleItem, val course: Course) : SyllabusEffect()
}

data class SyllabusModel(
    val courseId: Long,
    val isLoading: Boolean = false,
    val course: DataResult<Course>? = null,
    val syllabus: ScheduleItem? = null,
    val events: DataResult<List<ScheduleItem>>? = null
)
