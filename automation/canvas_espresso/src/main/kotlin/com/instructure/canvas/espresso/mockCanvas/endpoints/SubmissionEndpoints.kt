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
import com.instructure.canvas.espresso.mockCanvas.utils.LongId
import com.instructure.canvas.espresso.mockCanvas.utils.PathVars
import com.instructure.canvas.espresso.mockCanvas.utils.successResponse
import com.instructure.canvas.espresso.mockCanvas.utils.unauthorizedResponse
import com.instructure.canvasapi2.models.Submission

/**
 * Submission index for a specific course/assignment
 *
 * PUT - Endpoint for submitting a submission, responds with the submission or 401 if not present in data
 *
 * ROUTES:
 * - `{userId}` -> [SubmissionUserEndpoint]
 */
object SubmissionIndexEndpoint : Endpoint(
        LongId(PathVars::userId) to SubmissionUserEndpoint,
    response = {
        POST {
            var submission: Submission? = null
            for(tempSubmission in data.submissions[pathVars.courseId]!!) {
                if(tempSubmission.assignmentId == pathVars.assignmentId) {
                    submission = tempSubmission
                    // Now we need to modify the assignment in the data value so it reflects this
                    for (group in data.assignmentGroups[pathVars.courseId]!!) {
                        for(assignment in group.assignments) {
                            if(assignment.id == pathVars.assignmentId) {
                                assignment.submission = tempSubmission
                            }
                        }
                    }
                }
            }

            if(submission != null) {
                request.successResponse(submission!!)
            } else {
                request.unauthorizedResponse()
            }
        }
    }
)

/**
 * GET - Endpoint for a specific submission for a student from a specific assignment from a specific
 * course
 *
 */
object SubmissionUserEndpoint : Endpoint(
    response = {
        GET {
            request.successResponse(Submission())
        }
    }
)