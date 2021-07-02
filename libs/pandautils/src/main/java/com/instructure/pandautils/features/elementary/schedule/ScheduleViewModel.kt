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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.managers.AnnouncementManager
import com.instructure.canvasapi2.managers.CourseManager
import com.instructure.canvasapi2.managers.PlannerManager
import com.instructure.canvasapi2.managers.ToDoManager
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.canvasapi2.utils.toApiString
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
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

    init {
        viewModelScope.launch {
            val startDate = Date()
            val userTodos = toDoManager.getUserTodosAsync(true).await().dataOrNull

            val courses = courseManager.getCoursesAsync(true).await()
            val coursesMap = courses.dataOrThrow
                    .filter { !it.homeroomCourse }
                    .associateBy { it.id }
            val dashboardCourses = courseManager.getDashboardCoursesAsync(true)
                    .await()
                    .dataOrThrow
                    .mapNotNull { coursesMap[it.id] }
            val announcementMap = dashboardCourses.associateWith {
                announcementManager.getLatestAnnouncementAsync(it, true)
                        .await()
                        .dataOrNull
                        ?.firstOrNull()
            }
            val plannerItems = plannerManager.getPlannerItemsAsync(
                    true,
                    DateHelper.getLastSunday(startDate).toApiString(),
                    DateHelper.getNextSaturday(startDate).toApiString())
                    .await()
                    .dataOrNull
        }
    }
}