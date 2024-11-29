package com.instructure.student.router

import android.net.Uri
import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.Tab
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.depaginate
import com.instructure.interactions.router.Route
import com.instructure.pandautils.utils.orDefault

class EnabledTabs(
    private val courseApi: CourseAPI.CoursesInterface
) {
    private var enabledTabs: List<Tab>? = null

    private fun isPathTabEnabled(courseId: Long, uri: Uri): Boolean {
        val tabs = enabledTabs ?: return true
        if (tabs.isEmpty()) return true

        val pathSegments = uri.pathSegments
        val relativePath = uri.path?.replaceBefore("/courses/$courseId", "")
        // Details urls should be accepted, like /assignments/1, but assignments/syllabus should not
        return if (pathSegments.last() == Tab.SYLLABUS_ID) { // handle syllabus which has the same url scheme as assignment details
            tabs.any { relativePath == it.htmlUrl }
        } else if (pathSegments.size == 3) { // tab urls
            tabs.any { relativePath?.contains(it.htmlUrl.orEmpty()).orDefault() && it.tabId != Tab.HOME_ID}
        } else if (pathSegments.contains("external_tools") && pathSegments.size == 4) { // external tools
            return tabs.any { relativePath == it.htmlUrl }
        } else {
            true
        }
    }

    fun isPathTabNotEnabled(route: Route?): Boolean {
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

    suspend fun initTabs() {
        val tabs = courseApi.getFirstPageCourses(RestParams(usePerPageQueryParam = true)).depaginate {
            courseApi.next(it, RestParams(usePerPageQueryParam = true))
        }.dataOrNull?.associate { it.id to it.tabs.orEmpty() } ?: emptyMap()
        tabs.forEach { entry ->
            entry.value.find { tab -> tab.tabId == Tab.ASSIGNMENTS_ID }?.domain?.let { domain ->
                ApiPrefs.overrideDomains[entry.key] = domain
            }
        }
        enabledTabs = tabs.values.flatten()
    }
}