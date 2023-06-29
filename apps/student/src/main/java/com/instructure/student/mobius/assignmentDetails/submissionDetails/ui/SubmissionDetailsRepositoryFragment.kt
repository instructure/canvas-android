/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
package com.instructure.student.mobius.assignmentDetails.submissionDetails.ui

import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.interactions.router.Route
import com.instructure.interactions.router.RouterParams
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.makeBundle
import com.instructure.pandautils.utils.withArgs
import com.instructure.student.mobius.assignmentDetails.submissionDetails.SubmissionDetailsRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SubmissionDetailsRepositoryFragment : SubmissionDetailsFragment() {

    @Inject
    lateinit var submissionDetailsRepository: SubmissionDetailsRepository

    override fun getRepository() = submissionDetailsRepository

    companion object {
        fun makeRoute(
            course: CanvasContext,
            assignmentId: Long,
            isObserver: Boolean = false,
            initialSelectedSubmissionAttempt: Long? = null
        ): Route {
            val bundle = course.makeBundle {
                putLong(Const.ASSIGNMENT_ID, assignmentId)
                putBoolean(Const.IS_OBSERVER, isObserver)
                initialSelectedSubmissionAttempt?.let { putLong(Const.SUBMISSION_ATTEMPT, it) }
            }
            return Route(null, SubmissionDetailsRepositoryFragment::class.java, course, bundle)
        }

        fun validRoute(route: Route): Boolean {
            return route.canvasContext is Course &&
                    (route.arguments.containsKey(Const.ASSIGNMENT_ID) ||
                            route.paramsHash.containsKey(RouterParams.ASSIGNMENT_ID))
        }

        fun newInstance(route: Route): SubmissionDetailsRepositoryFragment? {
            if (!validRoute(route)) return null

            // If routed from a URL, set the bundle's assignment ID from the url value
            if (route.paramsHash.containsKey(RouterParams.ASSIGNMENT_ID)) {
                val assignmentId = route.paramsHash[RouterParams.ASSIGNMENT_ID]?.toLong() ?: -1
                route.arguments.putLong(Const.ASSIGNMENT_ID, assignmentId)
            }

            if (route.paramsHash.containsKey(Const.SUBMISSION_ATTEMPT)) {
                val submissionAttempt = route.paramsHash[Const.SUBMISSION_ATTEMPT]?.toLong() ?: -1
                route.arguments.putLong(Const.SUBMISSION_ATTEMPT, submissionAttempt)
            }

            return SubmissionDetailsRepositoryFragment().withArgs(route.arguments)
        }
    }
}
