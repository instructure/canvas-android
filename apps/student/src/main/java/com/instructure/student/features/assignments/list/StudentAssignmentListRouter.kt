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
package com.instructure.student.features.assignments.list

import androidx.fragment.app.FragmentActivity
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.pandautils.features.assignments.details.AssignmentDetailsFragment
import com.instructure.pandautils.features.assignments.list.AssignmentListRouter
import com.instructure.student.router.RouteMatcher

class StudentAssignmentListRouter: AssignmentListRouter {
    override fun routeToAssignmentDetails(activity: FragmentActivity, canvasContext: CanvasContext, assignment: Assignment) {
        val route = AssignmentDetailsFragment.makeRoute(canvasContext, assignment.id)
        RouteMatcher.route(activity, route)
    }

    override fun navigateBack(activity: FragmentActivity) {
        activity.onBackPressed()
    }
}