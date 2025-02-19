package com.instructure.student.router

import android.net.Uri
import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.Tab
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.depaginate
import com.instructure.interactions.router.Route

interface EnabledTabs {
    fun isPathTabNotEnabled(route: Route?): Boolean
    suspend fun initTabs()
}

class EnabledTabsImpl(
    private val courseApi: CourseAPI.CoursesInterface
): EnabledTabs {
    private var enabledTabs: Map<Long, List<Tab>>? = null

    private fun isPathTabEnabled(courseId: Long, uri: Uri): Boolean {
        val tabs = enabledTabs?.get(courseId) ?: return true
        if (tabs.isEmpty()) return true

        val pathSegments = uri.pathSegments
        val relativePath = uri.path?.replaceBefore("/courses/$courseId", "")
        // Details urls should be accepted, like /assignments/1, but assignments/syllabus should not
        return when {
            pathSegments.last() == "grades" -> true
            pathSegments.last() == "discussion_topics" -> true
            pathSegments.last() == Tab.SYLLABUS_ID -> tabs.any { relativePath == it.htmlUrl }
            pathSegments.size == 3 -> tabs.any { relativePath == it.htmlUrl }
            pathSegments.contains("external_tools") && pathSegments.size == 4 -> tabs.any { relativePath == it.htmlUrl }
            else -> true
        }
    }

    override fun isPathTabNotEnabled(route: Route?): Boolean {
        route?.uri?.let { uri ->
            route.courseId?.let { courseId ->
                return !isPathTabEnabled(courseId, uri)
            }
            if (uri.pathSegments.contains("courses")) {
                val courseIdIndex = uri.pathSegments.indexOf("courses") + 1
                var courseId = uri.pathSegments[courseIdIndex]
                if (courseId.contains("~")) {
                    val parts = courseId.split("~")
                    val length = parts[0].length + parts[1].length
                    val padding = 18 - length
                    courseId = parts[0] + "0".repeat(padding) + parts[1]
                }
                return !isPathTabEnabled(courseId.toLong(), uri)
            }

        }
        return false
    }

    override suspend fun initTabs() {
        enabledTabs = courseApi.getFirstPageCourses(RestParams(usePerPageQueryParam = true))
            .depaginate {
                courseApi.next(it, RestParams(usePerPageQueryParam = true))
            }.dataOrNull?.associate { it.id to (it.tabs ?: emptyList()) } ?: emptyMap()
        enabledTabs?.forEach { entry ->
            entry.value.find { tab -> tab.tabId == Tab.ASSIGNMENTS_ID }?.domain?.let { domain ->
                ApiPrefs.overrideDomains[entry.key] = domain
            }
        }
    }
}