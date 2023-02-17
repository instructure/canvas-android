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
package com.emeritus.student.mobius.elementary.homeroom

import androidx.fragment.app.FragmentActivity
import com.emeritus.student.fragment.AnnouncementListFragment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.pandautils.features.discussion.router.DiscussionRouterFragment
import com.instructure.pandautils.features.elementary.homeroom.HomeroomRouter
import com.emeritus.student.features.elementary.course.ElementaryCourseFragment
import com.emeritus.student.flutterChannels.FlutterComm
import com.emeritus.student.fragment.AssignmentListFragment
import com.emeritus.student.router.RouteMatcher

class StudentHomeroomRouter(private val activity: FragmentActivity) : HomeroomRouter {

    override fun openAnnouncements(canvasContext: CanvasContext) {
        val route = AnnouncementListFragment.makeRoute(canvasContext)
        RouteMatcher.route(activity, route)
    }

    override fun openCourse(course: Course) {
        RouteMatcher.route(activity, ElementaryCourseFragment.makeRoute(course))
    }

    override fun openAssignments(course: Course) {
        RouteMatcher.route(activity, AssignmentListFragment.makeRoute(course))
    }

    override fun openAnnouncementDetails(course: Course, announcement: DiscussionTopicHeader) {
        RouteMatcher.route(activity, DiscussionRouterFragment.makeRoute(course, announcement))
    }

    override fun updateColors() {
        FlutterComm.sendUpdatedTheme()
    }
}