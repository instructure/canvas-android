/*
 * Copyright (C) 2019 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */
package com.instructure.canvas.espresso.mockCanvas.endpoints

import com.instructure.canvas.espresso.mockCanvas.Endpoint
import com.instructure.canvas.espresso.mockCanvas.utils.*

/**
 * Endpoint that can return a list of courses in which the request user is enrolled.
 *
 * ROUTES:
 * - `{courseId}` -> [CourseEndpoint]
 */
object CourseListEndpoint : Endpoint(
    LongId(PathVars::courseId) to CourseEndpoint,
    response = {
        GET {
            val user = request.user!!
            val courses = data.enrollments
                .values
                .filter { it.userId == user.id }
                .map { data.courses[it.courseId]!! }
            request.successPaginatedResponse(courses)
        }
    }
)

/**
 * Endpoint that can return the course specified by [PathVars.courseId]. If the request user is not enrolled in
 * the requested course, an unauthorized response is returned.
 *
 * ROUTES:
 * - `tabs` -> [CourseTabsEndpoint]
 */
object CourseEndpoint : Endpoint(
        Segment("tabs") to CourseTabsEndpoint,
        Segment("assignments") to AssignmentIndexEndpoint,
        Segment("assignment_groups") to AssignmentGroupListEndpoint,
        Segment("external_tools") to ExternalToolsEndpoint,
        response = {
            GET {
                val course = data.courses[pathVars.courseId]!!
//                val userId = pathVars.userId
                val userId = request.user!!.id
                if (data.enrollments.values.any { it.courseId == course.id && it.userId == userId }) {
                    request.successResponse(course)
                } else {
                    request.unauthorizedResponse()
                }
            }
        }
)

/**
 * Endpoint that returns the tabs for a course
 */
object CourseTabsEndpoint : Endpoint( response = {
    GET {
        val course = data.courses[pathVars.courseId]!!
        request.successResponse(data.courseTabs[course.id]!!) // returns a list of tabs
    }
})
