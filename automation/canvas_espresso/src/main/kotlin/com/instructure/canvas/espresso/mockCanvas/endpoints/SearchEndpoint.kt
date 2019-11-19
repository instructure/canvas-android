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
package com.instructure.canvas.espresso.mockCanvas.endpoints

import com.instructure.canvas.espresso.mockCanvas.Endpoint
import com.instructure.canvas.espresso.mockCanvas.endpoint
import com.instructure.canvas.espresso.mockCanvas.utils.Segment
import com.instructure.canvas.espresso.mockCanvas.utils.successResponse
import com.instructure.canvas.espresso.mockCanvas.utils.unauthorizedResponse

//https://mock-data.instructure.com/api/v1/search/recipients?synthetic_contexts=1&context=course_1&per_page=100
//https://mobiledev.instructure.com/api/v1/search/recipients?synthetic_contexts=1&context=course_1567973_students&per_page=100
object SearchEndpoint : Endpoint(
    Segment("recipients") to endpoint(
        configure = {
            GET {
                val contexts = request.url().queryParameter("context")
                if(contexts == null) request.unauthorizedResponse()
                var courseId = contexts!!.substringAfter("course_")


                if (contexts.contains("students")) {
                    courseId = courseId.substringBefore("_students")
                    if(data.studentRecipients.containsKey(courseId.toLong())) {
                        request.successResponse(data.studentRecipients[courseId.toLong()]!!)
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
            }
        }
    )
)
