/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
package com.instructure.student.router

import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.Tab
import com.instructure.interactions.router.Route

class RouteMatcherRepository(
    private val courseApi: CourseAPI.CoursesInterface
) {
    private suspend fun getCourseTabs(courseId: Long): List<Tab> {
        val params = RestParams()
        val course = courseApi.getCourse(courseId, params)
        val enabledTabs = course.dataOrNull?.tabs?.filter { it.enabled && !it.isHidden }

        return enabledTabs ?: emptyList()
    }

    private suspend fun isPathTabEnabled(courseId: Long, path: String): Boolean {
        val tabUrls = getCourseTabs(courseId)
        val pathSegments = path.split("/")
        // Details urls should be accepted, like /assignments/1, but assignments/syllabus should not
        return if (pathSegments.last().toIntOrNull() != null) { // it's not a details url
            tabUrls.any { path.contains(it.htmlUrl.orEmpty()) && it.tabId != "home" }
        } else {
            val relativePath = path.replaceBefore("/", "")
            tabUrls.any { relativePath == it.htmlUrl }
        }
    }

    suspend fun isRouteNotAvailable(route: Route?): Boolean {
        route?.uri?.let { uri ->
            route.courseId?.let { courseId ->
                return !isPathTabEnabled(courseId, uri.path.orEmpty())
            }
            if (uri.pathSegments.contains("courses")) {
                val courseIdIndex = uri.pathSegments.indexOf("courses") + 1
                val courseId = uri.pathSegments[courseIdIndex]
                return !isPathTabEnabled(courseId.toLong(), uri.path.orEmpty())
            }

        }
        route?.routePath?.let { path ->
            route.courseId?.let { courseId ->
                return !isPathTabEnabled(courseId, path)
            }
            if (path.contains("courses")) {
                val pathSegments = path.split("/")
                val courseIdIndex = pathSegments.indexOf("courses") + 1
                val courseId = pathSegments[courseIdIndex]
                return !isPathTabEnabled(courseId.toLong(), path)
            }
        }
        return false
    }
}