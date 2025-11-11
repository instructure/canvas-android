/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.student.features.todolist

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.instructure.pandautils.features.calendar.CalendarFragment
import com.instructure.pandautils.features.calendar.CalendarSharedEvents
import com.instructure.pandautils.features.calendar.SharedCalendarAction
import com.instructure.pandautils.features.todolist.ToDoListFragment
import com.instructure.pandautils.features.todolist.ToDoListRouter
import com.instructure.student.activity.NavigationActivity
import com.instructure.student.router.RouteMatcher
import org.threeten.bp.LocalDate

class StudentToDoListRouter(
    private val activity: FragmentActivity,
    private val fragment: Fragment,
    private val calendarSharedEvents: CalendarSharedEvents
) : ToDoListRouter {

    override fun openNavigationDrawer() {
        (activity as? NavigationActivity)?.openNavigationDrawer()
    }

    override fun attachNavigationDrawer() {
        val toDoListFragment = fragment as? ToDoListFragment
        if (toDoListFragment != null) {
            (activity as? NavigationActivity)?.attachNavigationDrawer(toDoListFragment, null)
        }
    }

    override fun openToDoItem(htmlUrl: String) {
        RouteMatcher.routeUrl(activity, htmlUrl)
    }

    override fun openCalendar(date: LocalDate) {
        val route = CalendarFragment.makeRoute()
        RouteMatcher.route(activity, route)

        calendarSharedEvents.sendEvent(activity.lifecycleScope, SharedCalendarAction.SelectDay(date))
    }
}
