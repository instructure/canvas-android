/*
 * Copyright (C) 2022 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.instructure.student.mobius.elementary.importantdates

import androidx.fragment.app.FragmentActivity
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.ScheduleItem
import com.instructure.pandautils.features.calendarevent.details.EventFragment
import com.instructure.pandautils.features.elementary.importantdates.ImportantDatesRouter
import com.instructure.pandautils.features.assignments.details.AssignmentDetailsFragment
import com.instructure.student.router.RouteMatcher

class StudentImportantDatesRouter(private val activity: FragmentActivity) : ImportantDatesRouter {
    override fun openCalendarEvent(canvasContext: CanvasContext, scheduleItem: ScheduleItem) {
        RouteMatcher.route(activity, EventFragment.makeRoute(canvasContext, scheduleItem))
    }

    override fun openAssignment(canvasContext: CanvasContext, assignmentId: Long) {
        RouteMatcher.route(activity, AssignmentDetailsFragment.makeRoute(canvasContext, assignmentId))
    }
}