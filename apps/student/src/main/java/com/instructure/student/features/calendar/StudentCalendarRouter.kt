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
package com.instructure.student.features.calendar

import androidx.fragment.app.FragmentActivity
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.PlannerItem
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.features.calendar.CalendarRouter
import com.instructure.pandautils.features.calendarevent.details.EventFragment
import com.instructure.pandautils.features.calendartodo.createupdate.CreateUpdateToDoFragment
import com.instructure.pandautils.features.calendartodo.details.ToDoFragment
import com.instructure.pandautils.features.discussion.router.DiscussionRouterFragment
import com.instructure.student.activity.NavigationActivity
import com.instructure.student.features.assignments.details.AssignmentDetailsFragment
import com.instructure.student.fragment.BasicQuizViewFragment
import com.instructure.student.router.RouteMatcher

class StudentCalendarRouter(private val activity: FragmentActivity) : CalendarRouter {
    override fun openNavigationDrawer() {
        (activity as? NavigationActivity)?.openNavigationDrawer()
    }

    override fun openAssignment(canvasContext: CanvasContext, assignmentId: Long) {
        val route = AssignmentDetailsFragment.makeRoute(canvasContext, assignmentId)
        RouteMatcher.route(activity, route)
    }

    override fun openDiscussion(canvasContext: CanvasContext, discussionId: Long) {
        val route = DiscussionRouterFragment.makeRoute(canvasContext, discussionId)
        RouteMatcher.route(activity, route)
    }

    override fun openQuiz(canvasContext: CanvasContext, htmlUrl: String) {
        if (!RouteMatcher.canRouteInternally(activity, htmlUrl, ApiPrefs.domain, true)) {
            val route = BasicQuizViewFragment.makeRoute(canvasContext, htmlUrl)
            RouteMatcher.route(activity, route)
        }
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

    override fun attachNavigationDrawer() {
        // This is a no-op in the Student app, navigation drawer is already handled in the Activty
    }
}