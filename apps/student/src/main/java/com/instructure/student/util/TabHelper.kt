/*
 * Copyright (C) 2016 - present Instructure, Inc.
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

package com.instructure.student.util

import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Page
import com.instructure.canvasapi2.models.Tab
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.canvasapi2.utils.validOrNull
import com.instructure.interactions.router.Route
import com.instructure.student.R
import com.instructure.student.fragment.*
import com.instructure.student.mobius.syllabus.ui.SyllabusFragment

object TabHelper {

    fun getHomePageDisplayString(canvasContext: CanvasContext): String? {
        if (canvasContext !is Course) return ContextKeeper.appContext.getString(R.string.homePageIdForNotifications) // Only Courses have a customizable home page, everything else goes to the Notification page (aka Recent Activity on the web)
        return when (canvasContext.homePageID) {
            Tab.NOTIFICATIONS_ID -> ContextKeeper.appContext.getString(R.string.homePageIdForNotifications)
            Tab.PAGES_ID -> ""
            Tab.MODULES_ID -> ContextKeeper.appContext.getString(R.string.homePageIdForModules)
            Tab.ASSIGNMENTS_ID -> ContextKeeper.appContext.getString(R.string.homePageIdForAssignments)
            Tab.SYLLABUS_ID -> ContextKeeper.appContext.getString(R.string.homePageIdForSyllabus)
            else -> null
        }
    }

    fun isHomeTabAPage(course: Course): Boolean = Tab.FRONT_PAGE_ID == course.homePageID

    /**
     * Check if the tab is the home tab. This will allow us to display "Home"
     * in the actionbar instead of the actual tab name
     * @param tab Tab that we are checking to see if it is the home tab
     * @param canvasContext Used to get the home tab id for the course/group
     * @return True if the tab is the home page, false otherwise
     */
    fun isHomeTab(tab: Tab, canvasContext: Course): Boolean = isHomeTab(tab.tabId, canvasContext)

    private fun isHomeTab(tabId: String, canvasContext: Course): Boolean =
        canvasContext.homePageID == tabId || "home".equals(tabId, ignoreCase = true)

    fun isHomeTab(tab: Tab): Boolean = "home".equals(tab.tabId, ignoreCase = true)

    fun getRouteByTabId(tabb: Tab?, canvasContext: CanvasContext): Route? {
        val tab = tabb ?: Tab(Tab.HOME_ID, "")

        val isCourse = canvasContext is Course
        var tabId = tab.tabId.validOrNull() ?: (canvasContext as Course).homePageID

        if (isCourse) { // Courses can have customized home pages
            if (tabId.equals((canvasContext as Course).homePageID, ignoreCase = true) || "home".equals(tabId, ignoreCase = true))
                tabId = canvasContext.homePageID
        } else if ("home".equals(tabId, ignoreCase = true)) {
            // Only Courses have a customizable home page, everything else goes to the Notification page (aka Recent Activity on the web)
            return NotificationListFragment.makeRoute(canvasContext)
        }

        return when (tabId.toLowerCase()) {
            Tab.ASSIGNMENTS_ID -> AssignmentListFragment.makeRoute(canvasContext)
            Tab.MODULES_ID -> ModuleListFragment.makeRoute(canvasContext)
            Tab.PAGES_ID -> PageListFragment.makeRoute(canvasContext, false)
            Tab.FRONT_PAGE_ID -> PageDetailsFragment.makeRoute(canvasContext, Page.FRONT_PAGE_NAME)
            Tab.DISCUSSIONS_ID -> DiscussionListFragment.makeRoute(canvasContext)
            Tab.PEOPLE_ID -> PeopleListFragment.makeRoute(canvasContext)
            Tab.FILES_ID -> FileListFragment.makeRoute(canvasContext)
            Tab.SYLLABUS_ID -> SyllabusFragment.makeRoute(canvasContext as Course)
            Tab.QUIZZES_ID -> QuizListFragment.makeRoute(canvasContext)
            Tab.OUTCOMES_ID -> UnsupportedTabFragment.makeRoute(canvasContext, tab.tabId)
            Tab.CONFERENCES_ID -> ConferencesFragment.makeRoute(canvasContext)
            Tab.COLLABORATIONS_ID -> UnsupportedTabFragment.makeRoute(canvasContext, tab.tabId)
            Tab.ANNOUNCEMENTS_ID -> AnnouncementListFragment.makeRoute(canvasContext)
            Tab.GRADES_ID -> GradesListFragment.makeRoute(canvasContext)
            Tab.SETTINGS_ID -> CourseSettingsFragment.makeRoute(canvasContext)
            Tab.NOTIFICATIONS_ID -> NotificationListFragment.makeRoute(canvasContext)
            else -> when {
            // We just care if it's external, some external tabs (Attendance) have an id after "external"
                tabId.contains(Tab.TYPE_EXTERNAL) -> LTIWebViewFragment.makeRoute(canvasContext, tab)
                else -> null
            }
        }
    }
}
