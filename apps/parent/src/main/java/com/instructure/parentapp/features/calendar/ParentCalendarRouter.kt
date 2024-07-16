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

    // This is a no-op in the Parent app, navigation drawer is already handled in the Activity
    override fun openNavigationDrawer() = Unit

    override fun openAssignment(canvasContext: CanvasContext, assignmentId: Long) {
        // TODO Implement when in the assignment details ticket
    }

    override fun openDiscussion(canvasContext: CanvasContext, discussionId: Long) {
        // TODO Implement when in the assignment details ticket
    }

    override fun openQuiz(canvasContext: CanvasContext, htmlUrl: String) {
        // TODO Implement when in the assignment details ticket
    }

    override fun openCalendarEvent(canvasContext: CanvasContext, eventId: Long) {
        navigation.navigate(activity, navigation.calendarEventRoute(canvasContext.id, eventId))
    }

    override fun openToDo(plannerItem: PlannerItem) {
        // TODO
    }

    override fun openCreateToDo(initialDateString: String?) {
        // TODO
    }

    override fun openCreateEvent(initialDateString: String?) {
        // TODO
    }

    // This is a no-op in the Parent app, navigation drawer is already handled in the Activity
    override fun attachNavigationDrawer() = Unit
}