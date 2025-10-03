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
 */
package com.instructure.canvas.espresso.mockcanvas.endpoints

import com.instructure.canvas.espresso.mockcanvas.Endpoint
import com.instructure.canvas.espresso.mockcanvas.endpoint
import com.instructure.canvas.espresso.mockcanvas.utils.Segment
import com.instructure.canvas.espresso.mockcanvas.utils.successResponse
import com.instructure.canvas.espresso.mockcanvas.utils.unauthorizedResponse

/**
 * Base endpoint for searches
 *
 * ROUTES:
 * - `recipients` -> list of recipients or recipient groups by context query param
 */
object SearchEndpoint : Endpoint(
    Segment("recipients") to endpoint(
        configure = {
            GET {
                val contexts = request.url.queryParameter("context")
                if(contexts != null) {
                    var courseId = contexts.substringAfter("course_")

                    if (contexts.contains("students")) {
                        courseId = courseId.substringBefore("_students")
                        if(data.studentRecipients.containsKey(courseId.toLong())) {
                            if(data.coursePermissions[courseId.toLong()]!!.send_messages) {
                                request.successResponse(data.studentRecipients[courseId.toLong()]!!)
                            } else {
                                // To emulate the recipient end point returning just the author, we'll return the first element
                                request.successResponse(listOf(data.studentRecipients[courseId.toLong()]!!.first()))
                            }
                        } else {
                            request.unauthorizedResponse()
                        }
                    } else if (contexts.contains("teachers")) {
                        courseId = courseId.substringBefore("_teachers")
                        if(data.teacherRecipients.containsKey(courseId.toLong())) {
                            request.successResponse(data.teacherRecipients[courseId.toLong()]!!)
                        } else {
                            request.unauthorizedResponse()
                        }
                    } else {
                        if(data.recipientGroups.containsKey(courseId.toLong())) {
                            request.successResponse(data.recipientGroups[courseId.toLong()]!!)
                        } else {
                            request.unauthorizedResponse()
                        }
                    }
                } else {
                    // No context provided
                    request.unauthorizedResponse()
                }
            }
        }
    )
)
