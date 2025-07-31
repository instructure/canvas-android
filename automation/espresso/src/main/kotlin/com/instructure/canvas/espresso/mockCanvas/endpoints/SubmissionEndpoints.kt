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
import com.instructure.canvas.espresso.mockCanvas.addSubmissionForAssignment
import com.instructure.canvas.espresso.mockCanvas.utils.Segment
import com.instructure.canvas.espresso.mockCanvas.utils.UserId
import com.instructure.canvas.espresso.mockCanvas.utils.successResponse
import com.instructure.canvas.espresso.mockCanvas.utils.unauthorizedResponse
import com.instructure.canvas.espresso.mockCanvas.utils.user
import com.instructure.canvasapi2.models.Attachment
import com.instructure.canvasapi2.models.Author
import com.instructure.canvasapi2.models.FileUploadParams
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.models.SubmissionComment
import com.instructure.canvasapi2.type.SubmissionType
import java.util.Calendar
import kotlin.random.Random

/**
 * Submission index for a specific course/assignment
 *
 * POST - Endpoint for submitting a submission, responds with the submission or 401 if not present in data
 *
 * ROUTES:
 * - `{userId}` -> [SubmissionUserEndpoint]
 */
object SubmissionIndexEndpoint : Endpoint(
    UserId() to SubmissionUserEndpoint,
    response = {
        POST {
            // Grab the assignment
            val assignment = data.assignments[pathVars.assignmentId]!!

            // Grab our query parameters
            val submissionUrl = request.url.queryParameter("submission[url]")
            val submissionType = request.url.queryParameter("submission[submission_type]")
            val submissionBody = request.url.queryParameter("submission[body]")
            val submissionFileId = request.url.queryParameter("submission[file_ids][]")

            // Construct a submission (including an attachment, if necessary)
            var attachment: Attachment? = null
            if(submissionType == SubmissionType.online_upload.rawValue) {
                val courseRootFolder = data.courseRootFolders[pathVars.courseId]
                // We've already uploaded a file for this submission; refer to it
                val file = data.folderFiles[courseRootFolder?.id]?.find { it.id == submissionFileId!!.toLong() }!!
                attachment = Attachment(
                        id = data.newItemId(),
                        contentType = file.contentType,
                        filename = file.displayName,
                        displayName = file.displayName,
                        url = file.url,
                        previewUrl = file.url,
                        createdAt = file.createdDate,
                        size = file.size
                )
            }
            val submission = data.addSubmissionForAssignment(
                    assignmentId = pathVars.assignmentId,
                    userId = request.user!!.id,
                    type = submissionType!!,
                    body = submissionBody,
                    url = submissionUrl,
                    attachment = attachment,
                    attempt = Random.nextLong()
            )

            assignment.submission = submission

            if(submission != null) {
                request.successResponse(submission)
            } else {
                request.unauthorizedResponse()
            }
        }

        GET {
            // Grab the submission for the specified assignment
            val submissions = data.submissions[pathVars.assignmentId] ?: listOf<Submission>()
            request.successResponse(submissions)
        }
    }
)

/**
 * GET - Endpoint for a specific submission for a student from a specific assignment from a specific
 * course
 * PUT - Add a submission comment or a grade
 *
 * ROUTES:
 * - `files` -> anonymous endpoint for posting a submission file

 */
object SubmissionUserEndpoint : Endpoint(
    Segment("files") to Endpoint(
            response = {
                POST {
                    val courseId = pathVars.courseId

                    // Compute the fileId and fileUrl so that we can include those in our response
                    val fileId = data.newItemId()
                    val fileUrl  = "https://mock-data.instructure.com/api/v1/courses/$courseId/files/$fileId"

                    // Copy the query parameters for this request to our response, to be used
                    // when the app uploads the actual file to the specified location.
                    val queryParams = mutableMapOf<String,String>()
                    for(index in 0..request.url.querySize - 1) {
                        request.url.queryParameterValue(index)?.let {
                            queryParams.put(request.url.queryParameterName(index), it)
                        }
                    }

                    // Our response is a FileUploadParams object
                    val response = FileUploadParams(
                            uploadUrl = fileUrl,
                            uploadParams = queryParams
                    )

                    request.successResponse(response)
                }

            }
    ),
    response = {
        GET {
            // We may need to tweak this later.
            val submission = data.submissions[pathVars.assignmentId]?.find { it.userId == pathVars.userId }
            if (submission != null) {
                Log.d("<--", "get-submission-user comments: ${submission.submissionComments.joinToString()}")
                request.successResponse(submission)
            } else {
                // Sigh... Unauthorized result blocks access to submission details screen.
                // Return empty submission?
                //request.unauthorizedResponse()
                request.successResponse(Submission(
                        id = data.newItemId(),
                        userId = request.user!!.id,
                        assignmentId = pathVars.assignmentId
                ))
            }
        }

        PUT { // add a comment or grade the submission
            val submission = data.submissions[pathVars.assignmentId]?.find { it.userId == pathVars.userId }
            if (submission != null) {
                val comment = request.url.queryParameter("comment[text_comment]")
                val attempt = request.url.queryParameter("comment[attempt]")
                val user = request.user!!
                val grade = request.url.queryParameter("submission[posted_grade]")
                val excused = request.url.queryParameter("submission[excuse]")
                if (comment != null && comment.length > 0) {
                    val newCommentList = mutableListOf<SubmissionComment>().apply { addAll(submission.submissionComments) }
                    newCommentList.add(SubmissionComment(
                            id = data.newItemId(),
                            authorId = user.id,
                            authorName = user.name,
                            comment = comment,
                            createdAt = Calendar.getInstance().time,
                            author = Author(
                                    id = user.id,
                                    displayName = user.shortName
                            ),
                            attempt = attempt?.toLongOrNull()
                    ))
                    submission.submissionComments = newCommentList
                    Log.d("<--", "put-submission-user comments: ${submission.submissionComments.joinToString()}")
                    request.successResponse(submission)
                } else if (grade != null && grade != "Not Graded") {
                    val assignment = data.assignments[pathVars.assignmentId]!!
                    val updatedSubmission = submission.copy(
                            grade = grade,
                            score = when (assignment.gradingType) {
                                "points" -> grade.toDouble() // For "points" and "percent" grades, let's be accurate
                                "percent" -> grade.toDouble()
                                "gpa_scale" -> grade.toDouble()
                                else -> 90.0 // for everything else, make something up for now
                            },
                            excused = false
                    )
                    data.submissions[pathVars.assignmentId]?.remove(submission)
                    data.submissions[pathVars.assignmentId]?.add(updatedSubmission)
                    request.successResponse(updatedSubmission)
                } else if (excused == "true") {
                    val updatedSubmission = submission.copy(
                            grade = null,
                            excused = true
                    )
                    data.submissions[pathVars.assignmentId]?.remove(submission)
                    data.submissions[pathVars.assignmentId]?.add(updatedSubmission)
                    request.successResponse(updatedSubmission)
                } else if (grade == "Not Graded") {
                    val updatedSubmission = submission.copy(
                            grade = null,
                            excused = false
                    )
                    data.submissions[pathVars.assignmentId]?.remove(submission)
                    data.submissions[pathVars.assignmentId]?.add(updatedSubmission)
                    request.successResponse(updatedSubmission)
                } else {
                    // We don't know why we're here
                    throw Exception("Unhandled submission-user-put")
                }
            }
            else {
                request.unauthorizedResponse()
            }
        }
    }
)