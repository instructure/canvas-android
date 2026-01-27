/*
 * Copyright (C) 2025 - present Instructure, Inc.
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

package com.instructure.student.features.dashboard.widget.forecast

import androidx.fragment.app.FragmentActivity
import com.instructure.canvasapi2.models.Course
import com.instructure.pandautils.features.assignments.details.AssignmentDetailsFragment
import com.instructure.pandautils.features.dashboard.widget.forecast.ForecastWidgetRouter
import com.instructure.student.router.RouteMatcher

class StudentForecastWidgetRouter : ForecastWidgetRouter {

    override fun routeToAssignmentDetails(activity: FragmentActivity, assignmentId: Long, courseId: Long) {
        val canvasContext = Course(id = courseId)
        val route = AssignmentDetailsFragment.makeRoute(canvasContext, assignmentId)
        RouteMatcher.route(activity, route)
    }
}