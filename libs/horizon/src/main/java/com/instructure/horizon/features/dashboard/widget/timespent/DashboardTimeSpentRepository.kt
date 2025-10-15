/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.horizon.features.dashboard.widget.timespent

import com.instructure.canvasapi2.managers.graphql.horizon.CourseWithProgress
import com.instructure.canvasapi2.managers.graphql.horizon.HorizonGetCoursesManager
import com.instructure.canvasapi2.managers.graphql.horizon.journey.GetWidgetsManager
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.horizon.util.deserializeDynamicList
import javax.inject.Inject

class DashboardTimeSpentRepository @Inject constructor(
    private val getWidgetsManager: GetWidgetsManager,
    private val getCoursesManager: HorizonGetCoursesManager,
    private val apiPrefs: ApiPrefs,
) {
    suspend fun getTimeSpentData(courseId: Long? = null, forceNetwork: Boolean): TimeSpentWidgetData {
        val widgetData = getWidgetsManager.getTimeSpentWidgetData(courseId, forceNetwork)
        return TimeSpentWidgetData(
            lastModifiedDate = widgetData.lastModifiedDate,
            data = widgetData.data
                .deserializeDynamicList()
        )
    }

    suspend fun getCourses(forceNetwork: Boolean): List<CourseWithProgress> {
        return getCoursesManager.getCoursesWithProgress(apiPrefs.user!!.id, forceNetwork).dataOrThrow
    }
}
