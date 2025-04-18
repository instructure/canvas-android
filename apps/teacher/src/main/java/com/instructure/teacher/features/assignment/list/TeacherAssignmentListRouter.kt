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
package com.instructure.teacher.features.assignment.list

import androidx.fragment.app.FragmentActivity
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.interactions.router.Route
import com.instructure.pandautils.features.assignments.list.AssignmentListRouter
import com.instructure.teacher.features.assignment.details.AssignmentDetailsFragment
import com.instructure.teacher.fragments.QuizDetailsFragment
import com.instructure.teacher.router.RouteMatcher

class TeacherAssignmentListRouter: AssignmentListRouter {
    override fun routeToAssignmentDetails(activity: FragmentActivity, canvasContext: CanvasContext, assignment: Assignment) {
        if (assignment.getSubmissionTypes().contains(Assignment.SubmissionType.ONLINE_QUIZ)) {
            val args = QuizDetailsFragment.makeBundle(assignment.quizId)
            RouteMatcher.route(activity, Route(null, QuizDetailsFragment::class.java, canvasContext, args))
        } else {
            RouteMatcher.route(activity, AssignmentDetailsFragment.makeRoute(canvasContext, assignment.id))
        }
    }

    override fun navigateBack(activity: FragmentActivity) {
        activity.finish()
    }
}