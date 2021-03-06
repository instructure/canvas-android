/*
 * Copyright (C) 2020 - present Instructure, Inc.
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
package com.instructure.teacher.features.syllabus

import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CanvasContextPermission
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.ScheduleItem
import com.instructure.canvasapi2.utils.DataResult

sealed class SyllabusEvent {
    object PullToRefresh : SyllabusEvent()

    data class DataLoaded(
        val course: DataResult<Course>,
        val events: DataResult<List<ScheduleItem>>,
        val permissionsResult: DataResult<CanvasContextPermission>,
        val summaryAllowed: Boolean = false
    ) : SyllabusEvent()

    data class SyllabusItemClicked(val itemId: String) : SyllabusEvent()
    data class SyllabusUpdatedEvent(val content: String, val summaryAllowed: Boolean) : SyllabusEvent()
    object EditClicked : SyllabusEvent()
}

sealed class SyllabusEffect {
    data class LoadData(val courseId: Long, val forceNetwork: Boolean) : SyllabusEffect()
    data class ShowAssignmentView(val assignment: Assignment, val course: Course) : SyllabusEffect()
    data class ShowScheduleItemView(val scheduleItem: ScheduleItem, val course: Course) : SyllabusEffect()
    data class OpenEditSyllabus(val course: Course, val summaryAllowed: Boolean) : SyllabusEffect()
}

data class SyllabusModel(
        val courseId: Long,
        val isLoading: Boolean = false,
        val course: DataResult<Course>? = null,
        val syllabus: ScheduleItem? = null,
        val events: DataResult<List<ScheduleItem>>? = null,
        val permissions: DataResult<CanvasContextPermission>? = null,
        val summaryAllowed: Boolean = false
)