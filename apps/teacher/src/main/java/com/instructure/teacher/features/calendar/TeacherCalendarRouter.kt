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
package com.instructure.teacher.features.calendar

import androidx.fragment.app.FragmentActivity
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.PlannerItem
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.features.assignments.list.AssignmentListFragment
import com.instructure.pandautils.features.calendar.CalendarRouter
import com.instructure.pandautils.features.calendarevent.createupdate.CreateUpdateEventFragment
import com.instructure.pandautils.features.calendarevent.details.EventFragment
import com.instructure.pandautils.features.calendartodo.createupdate.CreateUpdateToDoFragment
import com.instructure.pandautils.features.calendartodo.details.ToDoFragment
import com.instructure.pandautils.features.discussion.router.DiscussionRouterFragment
import com.instructure.teacher.activities.InitActivity
import com.instructure.teacher.features.assignment.details.AssignmentDetailsFragment
import com.instructure.teacher.fragments.DiscussionsListFragment
import com.instructure.teacher.router.RouteMatcher

class TeacherCalendarRouter(val activity: FragmentActivity) : CalendarRouter {
    override fun openNavigationDrawer() {
        (activity as? InitActivity)?.openNavigationDrawer()
    }

    override fun openAssignment(canvasContext: CanvasContext, assignmentId: Long) {
        RouteMatcher.route(activity, AssignmentDetailsFragment.makeRoute(canvasContext, assignmentId).copy(primaryClass = AssignmentListFragment::class.java))
    }

    override fun openDiscussion(canvasContext: CanvasContext, discussionId: Long, assignmentId: Long?) {
        val route = DiscussionRouterFragment.makeRoute(canvasContext, discussionId).copy(primaryClass = DiscussionsListFragment::class.java)
        RouteMatcher.route(activity, route)
    }

    override fun openQuiz(canvasContext: CanvasContext, htmlUrl: String) {
        RouteMatcher.canRouteInternally(activity, htmlUrl, ApiPrefs.domain, true)
    }

    override fun openCalendarEvent(canvasContext: CanvasContext, eventId: Long) {
        val route = EventFragment.makeRoute(canvasContext, eventId)
        RouteMatcher.route(activity, route)
    }

    override fun openToDo(plannerItem: PlannerItem) {
        val route = ToDoFragment.makeRoute(plannerItem)
        RouteMatcher.route(activity, route)
    }

    override fun openCreateToDo(initialDateString: String?) {
        val route = CreateUpdateToDoFragment.makeRoute(initialDateString)
        RouteMatcher.route(activity, route)
    }

    override fun openCreateEvent(initialDateString: String?) {
        val route = CreateUpdateEventFragment.makeRoute(initialDateString)
        RouteMatcher.route(activity, route)
    }

    override fun attachNavigationDrawer() {
        (activity as? InitActivity)?.attachNavigationDrawer()
    }
}