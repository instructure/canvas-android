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

import android.util.Log
import com.instructure.canvas.espresso.mockCanvas.Endpoint
import com.instructure.canvas.espresso.mockCanvas.utils.LongId
import com.instructure.canvas.espresso.mockCanvas.utils.PathVars
import com.instructure.canvas.espresso.mockCanvas.utils.grabJsonFromMultiPartBody
import com.instructure.canvas.espresso.mockCanvas.utils.noContentResponse
import com.instructure.canvas.espresso.mockCanvas.utils.successResponse
import com.instructure.canvas.espresso.mockCanvas.utils.unauthorizedResponse
import com.instructure.canvas.espresso.mockCanvas.utils.user
import com.instructure.canvasapi2.models.Author
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.models.SubmissionComment
import com.instructure.canvasapi2.type.SubmissionType
import java.util.*

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
            val sub = request.url().queryParameter("submission")
            val type = request.url().queryParameter("submission[submission_type]")
            Log.d("<--", "SubmissionIndex submission parameter: $sub")
            Log.d("<--", "SubmissionIndex submission_type parameter: $type")

            // Grab the assignment
            val assignment = data.assignmentGroups[pathVars.courseId]!!
                    .flatMap { it.assignments }
                    .find { it.id == pathVars.assignmentId }!!

            val submissionUrl = request.url().queryParameter("submission[url]")
            val submissionType = request.url().queryParameter("submission[submission_type]")
            val submissionBody = request.url().queryParameter("submission[body]")
            val submission = Submission(
                    id = data.newItemId(),
                    submittedAt = Calendar.getInstance().time,
                    body = submissionBody,
                    assignmentId = pathVars.assignmentId,
                    //assignment = assignment,
                    submissionType = submissionType,
                    previewUrl = submissionUrl,
                    url = submissionUrl,
                    workflowState = "submitted", // Is this the right setting
                    userId = request.user!!.id

            )

            Log.d("<--", "submissionType=$submissionType, submission = $submission")

            var submissionList = data.submissions[pathVars.assignmentId]
            if(submissionList == null) {
                submissionList = mutableListOf<Submission>()
                data.submissions[pathVars.assignmentId] = submissionList!!
            }
            submissionList!!.add(submission)
//            val submission: Submission? = data.submissions[pathVars.courseId]!!
//                    .find { it.assignmentId == pathVars.assignmentId }


            // Now we need to modify the assignment in the data value so it reflects this
            assignment.submission = submission

            if(submission != null) {
                request.successResponse(submission)
            } else {
                request.unauthorizedResponse()
            }
        }
    }
)

/**
 * GET - Endpoint for a specific submission for a student from a specific assignment from a specific
 * course
 * PUT - Add a submission comment or a grade
 */
object SubmissionUserEndpoint : Endpoint(
    response = {
        GET {
            // We may need to tweak this later.
            val submission = data.submissions[pathVars.assignmentId]?.find {it.userId == request.user!!.id}
            if(submission != null) {
                Log.d("<--", "get-submission-user comments: ${submission.submissionComments.joinToString()}")
                request.successResponse(submission)
            }
            else {
                request.unauthorizedResponse()
            }
        }

        PUT { // add a comment or grade the submission
            val submission = data.submissions[pathVars.assignmentId]?.find {it.userId == request.user!!.id}
            if(submission != null) {
                val comment = request.url().queryParameter("comment[text_comment]")
                val user = request.user!!
                if(comment != null && comment.length > 0) {
                    val newCommentList = mutableListOf<SubmissionComment>().apply {addAll(submission.submissionComments)}
                    newCommentList.add(SubmissionComment(
                            id = data.newItemId(),
                            authorId = user.id,
                            authorName = user.name,
                            comment = comment,
                            createdAt = Calendar.getInstance().time,
                            author = Author(
                                    id = user.id,
                                    displayName = user.shortName
                            )
                    ))
                    submission.submissionComments = newCommentList
                    Log.d("<--", "put-submission-user comments: ${submission.submissionComments.joinToString()}")
                    request.successResponse(submission)
                }
                else {
                    // grade?
                    throw Exception("Unhandled submission-user-put")
                }
            }
            else {
                request.unauthorizedResponse()
            }
        }
    }
)