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
package com.instructure.student.mobius.elementary.homeroom

import androidx.fragment.app.FragmentActivity
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.pandautils.features.assignments.list.AssignmentListFragment
import com.instructure.pandautils.features.discussion.router.DiscussionRouterFragment
import com.instructure.pandautils.features.elementary.homeroom.HomeroomRouter
import com.instructure.student.features.elementary.course.ElementaryCourseFragment
import com.instructure.student.fragment.AnnouncementListFragment
import com.instructure.student.router.RouteMatcher

class StudentHomeroomRouter(private val activity: FragmentActivity) : HomeroomRouter {

    override fun openAnnouncements(canvasContext: CanvasContext) {
        val route = AnnouncementListFragment.makeRoute(canvasContext)
        RouteMatcher.route(activity, route)
    }

    override fun openCourse(course: Course) {
        RouteMatcher.route(activity, ElementaryCourseFragment.makeRoute(course))
    }

    override fun openAssignments(course: Course) {
        RouteMatcher.route(activity, AssignmentListFragment.makeRoute(course.id))
    }

    override fun openAnnouncementDetails(course: Course, announcement: DiscussionTopicHeader) {
        RouteMatcher.route(activity, DiscussionRouterFragment.makeRoute(course, announcement))
    }
}