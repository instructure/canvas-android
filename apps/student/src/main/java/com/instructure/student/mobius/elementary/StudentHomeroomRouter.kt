/*
 * Copyright (C) 2021 - present Instructure, Inc.
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
package com.instructure.student.mobius.elementary

import androidx.fragment.app.FragmentActivity
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.features.elementary.homeroom.HomeroomRouter
import com.instructure.student.flutterChannels.FlutterComm
import com.instructure.student.fragment.*
import com.instructure.student.mobius.assignmentDetails.ui.AssignmentDetailsFragment
import com.instructure.student.router.RouteMatcher

class StudentHomeroomRouter(private val activity: FragmentActivity) : HomeroomRouter {

    override fun canRouteInternally(url: String): Boolean {
        return RouteMatcher.canRouteInternally(activity, url, ApiPrefs.domain, routeIfPossible = false, allowUnsupported = false)
    }

    override fun routeInternally(url: String) {
        RouteMatcher.canRouteInternally(activity, url, ApiPrefs.domain, routeIfPossible = true, allowUnsupported = false)
    }

    override fun openMedia(url: String) {
        RouteMatcher.openMedia(activity, url)
    }

    override fun openAnnouncements(canvasContext: CanvasContext) {
        val route = AnnouncementListFragment.makeRoute(canvasContext)
        RouteMatcher.route(activity, route)
    }

    override fun openCourse(course: Course) {
        RouteMatcher.route(activity, CourseBrowserFragment.makeRoute(course))
    }

    override fun openAssignments(course: Course) {
        RouteMatcher.route(activity, AssignmentListFragment.makeRoute(course))
    }

    override fun openAssignment(canvasContext: CanvasContext, assignmentId: Long) {
        RouteMatcher.route(activity, AssignmentDetailsFragment.makeRoute(canvasContext, assignmentId))
    }

    override fun openCalendarEvent(canvasContext: CanvasContext, scheduleItemId: Long) {
        RouteMatcher.route(activity, CalendarEventFragment.makeRoute(canvasContext, scheduleItemId))
    }

    override fun openAnnouncementDetails(course: Course, announcement: DiscussionTopicHeader) {
        RouteMatcher.route(activity, DiscussionDetailsFragment.makeRoute(course, announcement))
    }

    override fun updateColors() {
        FlutterComm.sendUpdatedTheme()
    }

    override fun openQuiz(canvasContext: CanvasContext, htmlUrl: String) {
        RouteMatcher.route(activity, BasicQuizViewFragment.makeRoute(canvasContext, htmlUrl))
    }

    override fun openDiscussion(canvasContext: CanvasContext, discussionId: Long, discussionTitle: String) {
        RouteMatcher.route(activity, DiscussionDetailsFragment.makeRoute(canvasContext, discussionId, title = discussionTitle))
    }
}