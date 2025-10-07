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

package com.instructure.canvas.espresso.mockcanvas.endpoints

import com.instructure.canvas.espresso.mockcanvas.Endpoint
import com.instructure.canvas.espresso.mockcanvas.utils.LongId
import com.instructure.canvas.espresso.mockcanvas.utils.PathVars
import com.instructure.canvas.espresso.mockcanvas.utils.Segment
import com.instructure.canvas.espresso.mockcanvas.utils.successResponse
import com.instructure.canvasapi2.apis.EnrollmentAPI

object EnrollmentIndexEndpoint : Endpoint(
        LongId(PathVars::enrollmentId) to EnrollmentEndpoint,
        response = {
            GET {
                // TODO: Pay closer attention to query variables (e.g., "include[]=avatar_url", "state[]=active")
                val courseId = pathVars.courseId
                val courseEnrollments = data.enrollments.values.filter { e -> e.courseId == courseId }
                request.successResponse(courseEnrollments)
            }
        })


object EnrollmentEndpoint : Endpoint(
        Segment("accept") to EnrollmentAcceptEndpoint,
        Segment("reject") to EnrollmentDeclineEndpoint
)

/**
 * Endpoint for course enrollments
 */
object EnrollmentAcceptEndpoint: Endpoint(
        response = {
            POST {
                val enrollmentId = pathVars.enrollmentId
                data.enrollments.values.first { it.id == enrollmentId }.apply {
                    enrollmentState = EnrollmentAPI.STATE_ACTIVE
                }
                request.successResponse(Unit)
            }
        }
)

object EnrollmentDeclineEndpoint: Endpoint(
        response = {
            POST {
                val enrollmentId = pathVars.enrollmentId
                data.enrollments.values.first { it.id == enrollmentId }.apply {
                    enrollmentState = EnrollmentAPI.STATE_DELETED
                }
                request.successResponse(Unit)
            }
        }
)

