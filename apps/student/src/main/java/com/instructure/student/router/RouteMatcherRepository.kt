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

import android.net.Uri
import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.Tab
import com.instructure.interactions.router.Route
import com.instructure.pandautils.utils.orDefault

class RouteMatcherRepository(
    private val courseApi: CourseAPI.CoursesInterface
) {
    private suspend fun getCourseTabs(courseId: Long): List<Tab> {
        val params = RestParams()
        val course = courseApi.getCourse(courseId, params)
        val enabledTabs = course.dataOrNull?.tabs
            ?.filter { it.enabled && !it.isHidden }
            ?.map { // Replace wiki with pages to match the url scheme
                if (it.htmlUrl?.contains("wiki").orDefault()) {
                    it.copy(htmlUrl = it.htmlUrl?.replace("wiki", "pages"))
                } else {
                    it
                }
            }

        return enabledTabs ?: emptyList()
    }

    private suspend fun isPathTabEnabled(courseId: Long, uri: Uri): Boolean {
        val tabs = getCourseTabs(courseId)
        val pathSegments = uri.pathSegments
        val relativePath = uri.path?.replaceBefore("/courses/$courseId", "".orEmpty())
        // Details urls should be accepted, like /assignments/1, but assignments/syllabus should not
        return if (relativePath == "/courses/$courseId") { // handle home url which is prefix of every other urls
            return tabs.any { it.tabId == "home" }
        } else if (pathSegments.last() == "syllabus") { // handle syllabus which has the same url scheme as assignment details
            tabs.any { relativePath == it.htmlUrl }
        } else if (pathSegments.size == 3) { // tab urls
            tabs.any { relativePath?.contains(it.htmlUrl.orEmpty()).orDefault() && it.tabId != "home" }
        } else if (pathSegments.contains("external_tools") && pathSegments.size == 4) { // external tools
            return tabs.any { relativePath == it.htmlUrl }
        } else {
            true
        }
    }

    suspend fun isRouteNotAvailable(route: Route?): Boolean {
        route?.uri?.let { uri ->
            route.courseId?.let { courseId ->
                return !isPathTabEnabled(courseId, uri)
            }
            if (uri.pathSegments.contains("courses")) {
                val courseIdIndex = uri.pathSegments.indexOf("courses") + 1
                val courseId = uri.pathSegments[courseIdIndex]
                return !isPathTabEnabled(courseId.toLong(), uri)
            }

        }
        return false
    }
}