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
package com.instructure.parentapp.features.calendar

import androidx.fragment.app.FragmentActivity
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.PlannerItem
import com.instructure.pandautils.features.calendar.CalendarRouter
import com.instructure.parentapp.util.navigation.Navigation

class ParentCalendarRouter(
    private val activity: FragmentActivity,
    private val navigation: Navigation
) : CalendarRouter {

    // This is a no-op in the Parent app, navigation drawer is already handled in the DashboardFragment
    override fun openNavigationDrawer() = Unit

    override fun openAssignment(canvasContext: CanvasContext, assignmentId: Long) {
        navigation.navigate(activity, navigation.assignmentDetailsRoute(canvasContext.id, assignmentId))
    }

    override fun openDiscussion(canvasContext: CanvasContext, discussionId: Long, assignmentId: Long?) {
        assignmentId?.let { navigation.navigate(activity, navigation.assignmentDetailsRoute(canvasContext.id, it)) }
    }

    override fun openQuiz(canvasContext: CanvasContext, htmlUrl: String) {
        navigation.navigate(activity, navigation.internalWebViewRoute(htmlUrl, htmlUrl))
    }

    override fun openCalendarEvent(canvasContext: CanvasContext, eventId: Long) {
        navigation.navigate(activity, navigation.calendarEventRoute(canvasContext.type.apiString, canvasContext.id, eventId))
    }

    override fun openToDo(plannerItem: PlannerItem) {
        navigation.navigate(activity, navigation.toDoRoute(plannerItem))
    }

    override fun openCreateToDo(initialDateString: String?) {
        navigation.navigate(activity, navigation.createToDoRoute(initialDateString))
    }

    override fun openCreateEvent(initialDateString: String?) {
        navigation.navigate(activity, navigation.createEventRoute(initialDateString))
    }

    // This is a no-op in the Parent app, navigation drawer is already handled in the DashboardFragment
    override fun attachNavigationDrawer() = Unit
}