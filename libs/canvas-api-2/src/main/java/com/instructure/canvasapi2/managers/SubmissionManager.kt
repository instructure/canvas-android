/*
 * Copyright (C) 2017 - present Instructure, Inc.
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
package com.instructure.canvasapi2.managers

import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.apis.SubmissionAPI
import com.instructure.canvasapi2.builders.RestBuilder
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.utils.ExhaustiveListCallback
import com.instructure.canvasapi2.utils.weave.apiAsync

object SubmissionManager {

    fun getSingleSubmission(
        courseId: Long,
        assignmentId: Long,
        studentId: Long,
        callback: StatusCallback<Submission>,
        forceNetwork: Boolean
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams(isForceReadFromNetwork = forceNetwork)

        SubmissionAPI.getSingleSubmission(courseId, assignmentId, studentId, adapter, callback, params)
    }

    fun getSingleSubmissionAsync(
        courseId: Long,
        assignmentId: Long,
        studentId: Long,
        forceNetwork: Boolean
    ) = apiAsync<Submission> { getSingleSubmission(courseId, assignmentId, studentId, it, forceNetwork) }

    fun getSubmissionsForMultipleAssignments(
        studentId: Long,
        courseId: Long,
        assignmentIds: List<Long>,
        callback: StatusCallback<List<Submission>>,
        forceNetwork: Boolean
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)

        val depaginatedCallback = object : ExhaustiveListCallback<Submission>(callback) {
            override fun getNextPage(callback: StatusCallback<List<Submission>>, nextUrl: String, isCached: Boolean) {
                SubmissionAPI.getSubmissionsForMultipleAssignments(
                    courseId,
                    studentId,
                    assignmentIds,
                    adapter,
                    callback,
                    params
                )
            }
        }

        adapter.statusCallback = depaginatedCallback
        SubmissionAPI.getSubmissionsForMultipleAssignments(
            courseId,
            studentId,
            assignmentIds,
            adapter,
            depaginatedCallback,
            params
        )
    }

    fun updateRubricAssessment(
        courseId: Long,
        assignmentId: Long,
        studentId: Long,
        assessmentMap: Map<String, RubricCriterionAssessment>,
        callback: StatusCallback<Submission>
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams()
        SubmissionAPI.updateRubricAssessment(
            courseId,
            assignmentId,
            studentId,
            assessmentMap,
            adapter,
            callback,
            params
        )
    }

    fun postSubmissionComment(
        courseId: Long,
        assignmentId: Long,
        userId: Long,
        commentText: String,
        isGroupMessage: Boolean,
        attachments: List<Long>,
        callback: StatusCallback<Submission>
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams()
        SubmissionAPI.postSubmissionComment(
            courseId,
            assignmentId,
            userId,
            commentText,
            isGroupMessage,
            attachments,
            adapter,
            callback,
            params
        )
    }

    fun postSubmissionGrade(
        courseId: Long,
        assignmentId: Long,
        userId: Long,
        score: String,
        isExcused: Boolean,
        forceNetwork: Boolean,
        callback: StatusCallback<Submission>
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams(isForceReadFromNetwork = forceNetwork)

        SubmissionAPI.postSubmissionGrade(courseId, assignmentId, userId, score, isExcused, adapter, callback, params)
    }

    fun postSubmissionExcusedStatus(
        courseId: Long,
        assignmentId: Long,
        userId: Long,
        isExcused: Boolean,
        forceNetwork: Boolean,
        callback: StatusCallback<Submission>
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams(isForceReadFromNetwork = forceNetwork)

        SubmissionAPI.postSubmissionExcusedStatus(courseId, assignmentId, userId, isExcused, adapter, callback, params)
    }

    fun getSubmissionSummary(
        courseId: Long,
        assignmentId: Long,
        forceNetwork: Boolean,
        callback: StatusCallback<SubmissionSummary>
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams(isForceReadFromNetwork = forceNetwork)

        SubmissionAPI.getSubmissionSummary(courseId, assignmentId, adapter, params, callback)
    }

    fun postTextSubmission(
        canvasContext: CanvasContext,
        assignmentId: Long,
        text: String,
        callback: StatusCallback<Submission>
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams(canvasContext = canvasContext)

        SubmissionAPI.postTextSubmission(canvasContext.id, assignmentId, text, adapter, params, callback)
    }

    fun postUrlSubmission(
        canvasContext: CanvasContext,
        assignmentId: Long,
        url: String,
        isLti: Boolean,
        callback: StatusCallback<Submission>
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams(canvasContext = canvasContext)
        val type = if (isLti) "basic_lti_launch" else "online_url"

        SubmissionAPI.postUrlSubmission(canvasContext.id, assignmentId, type, url, adapter, params, callback)
    }

    fun getLtiFromAuthenticationUrl(url: String, callback: StatusCallback<LTITool>, forceNetwork: Boolean) {
        val adapter = RestBuilder(callback)
        val params = RestParams(isForceReadFromNetwork = forceNetwork)

        SubmissionAPI.getLtiFromAuthenticationUrl(url, adapter, params, callback)
    }

    fun getLtiFromAuthenticationUrlAsync(url: String, forceNetwork: Boolean) =
        apiAsync<LTITool> { getLtiFromAuthenticationUrl(url, it, forceNetwork) }

    fun postMediaSubmissionComment(
        canvasContext: CanvasContext,
        assignmentId: Long,
        studentId: Long,
        mediaId: String,
        mediaType: String,
        isGroupComment: Boolean,
        callback: StatusCallback<Submission>
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams(canvasContext = canvasContext)

        SubmissionAPI.postMediaSubmissionComment(
            canvasContext.id,
            assignmentId,
            studentId,
            mediaId,
            mediaType,
            isGroupComment,
            adapter,
            params,
            callback
        )
    }

    fun postMediaSubmission(
        canvasContext: CanvasContext,
        assignmentId: Long,
        submissionType: String,
        mediaId: String,
        mediaType: String,
        callback: StatusCallback<Submission>
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams(canvasContext = canvasContext)

        SubmissionAPI.postMediaSubmission(
            canvasContext.id,
            assignmentId,
            submissionType,
            mediaId,
            mediaType,
            adapter,
            params,
            callback
        )
    }

    fun postSubmissionAttachmentsSynchronous(
        courseId: Long,
        assignmentId: Long,
        attachmentsIds: List<Long>
    ): Submission? {
        val adapter = RestBuilder()
        val params = RestParams()

        return SubmissionAPI.postSubmissionAttachmentsSynchronous(
            courseId,
            assignmentId,
            attachmentsIds,
            adapter,
            params
        )
    }

    fun postStudentAnnotationSubmissionAsync(
        canvasContext: CanvasContext,
        assignmentId: Long,
        annotatableAttachmentId: Long
    ) = apiAsync<Submission> {
        postStudentAnnotationSubmission(canvasContext, assignmentId, annotatableAttachmentId, it)
    }

    private fun postStudentAnnotationSubmission(
        canvasContext: CanvasContext,
        assignmentId: Long,
        annotatableAttachmentId: Long,
        callback: StatusCallback<Submission>
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams(canvasContext = canvasContext)

        SubmissionAPI.postStudentAnnotationSubmission(
            canvasContext.id,
            assignmentId,
            annotatableAttachmentId,
            adapter,
            params,
            callback
        )
    }

}
