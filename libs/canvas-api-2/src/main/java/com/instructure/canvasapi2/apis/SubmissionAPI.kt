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

package com.instructure.canvasapi2.apis

import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.builders.RestBuilder
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.LTITool
import com.instructure.canvasapi2.models.RubricCriterionAssessment
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.models.SubmissionSummary
import com.instructure.canvasapi2.utils.DataResult
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.QueryMap
import retrofit2.http.Tag
import retrofit2.http.Url


object SubmissionAPI {

    private const val assessmentPrefix = "rubric_assessment["
    private const val ratingIdPostFix = "][rating_id]"
    private const val pointsPostFix = "][points]"
    private const val commentsPostFix = "][comments]"

    interface SubmissionInterface {

        @GET("courses/{courseId}/assignments/{assignmentId}/submissions/{studentId}?include[]=rubric_assessment&include[]=submission_history&include[]=submission_comments&include[]=group")
        fun getSingleSubmission(
                @Path("courseId") courseId: Long,
                @Path("assignmentId") assignmentId: Long,
                @Path("studentId") studentId: Long): Call<Submission>

        @GET("courses/{courseId}/assignments/{assignmentId}/submissions/{studentId}?include[]=rubric_assessment&include[]=submission_history&include[]=submission_comments&include[]=group")
        suspend fun getSingleSubmission(
            @Path("courseId") courseId: Long,
            @Path("assignmentId") assignmentId: Long,
            @Path("studentId") studentId: Long,
            @Tag restParams: RestParams
        ): DataResult<Submission>

        @GET("courses/{courseId}/students/submissions?include[]=assignment&include[]=rubric_assessment&include[]=submission_history&include[]=submission_comments&include[]=group")
        fun getSubmissionsForMultipleAssignments(
                @Path("courseId") courseId: Long,
                @Query("student_ids[]") studentId: Long,
                @Query("assignment_ids[]") assignmentIds: List<Long>): Call<List<Submission>>

        @GET("courses/{courseId}/students/submissions?include[]=assignment&include[]=rubric_assessment&include[]=submission_history&include[]=submission_comments&include[]=group")
        suspend fun getSubmissionsForMultipleAssignments(
            @Path("courseId") courseId: Long,
            @Query("student_ids[]") studentId: Long,
            @Query("assignment_ids[]") assignmentIds: List<Long>,
            @Tag restParams: RestParams
        ): DataResult<List<Submission>>

        @GET
        fun getNextPageSubmissions(@Url nextUrl: String): Call<List<Submission>>

        @GET
        suspend fun getNextPageSubmissions(@Url nextUrl: String, @Tag restParams: RestParams): DataResult<List<Submission>>

        @PUT("courses/{courseId}/assignments/{assignmentId}/submissions/{userId}")
        fun postSubmissionRubricAssessmentMap(
                @Path("courseId") courseId: Long,
                @Path("assignmentId") assignmentId: Long,
                @Path("userId") userId: Long,
                @QueryMap rubricAssessment: Map<String, String>
        ): Call<Submission>

        @PUT("courses/{courseId}/assignments/{assignmentId}/submissions/{userId}")
        fun postSubmissionComment(
                @Path("courseId") courseId: Long,
                @Path("assignmentId") assignmentId: Long,
                @Path("userId") userId: Long,
                @Query("comment[text_comment]") comment: String,
                @Query("comment[group_comment]") isGroupComment: Boolean,
                @Query("comment[file_ids][]") attachments: List<Long>
        ): Call<Submission>

        @PUT("courses/{courseId}/assignments/{assignmentId}/submissions/{userId}")
        fun postSubmissionComment(
            @Path("courseId") courseId: Long,
            @Path("assignmentId") assignmentId: Long,
            @Path("userId") userId: Long,
            @Query("comment[text_comment]") comment: String,
            @Query("comment[attempt]") attemptId: Long?,
            @Query("comment[group_comment]") isGroupComment: Boolean,
            @Query("comment[file_ids][]") attachments: List<Long>
        ): Call<Submission>

        @POST("{contextId}/assignments/{assignmentId}/submissions")
        fun postTextSubmission(
                @Path("contextId") contextId: Long,
                @Path("assignmentId") assignmentId: Long,
                @Query("submission[submission_type]") submissionType: String,
                @Query(value = "submission[body]", encoded = true) text: String): Call<Submission>

        @POST("{contextId}/assignments/{assignmentId}/submissions")
        fun postUrlSubmission(
                @Path("contextId") contextId: Long,
                @Path("assignmentId") assignmentId: Long,
                @Query("submission[submission_type]") submissionType: String,
                @Query("submission[url]") url: String): Call<Submission>

        @PUT("{contextId}/assignments/{assignmentId}/submissions/{userId}")
        fun postMediaSubmissionComment(
                @Path("contextId") contextId: Long,
                @Path("assignmentId") assignmentId: Long,
                @Path("userId") userId: Long,
                @Query("comment[attempt]") attemptId: Long?,
                @Query("comment[media_comment_id]") mediaId: String,
                @Query("comment[media_comment_type]") commentType: String,
                @Query("comment[group_comment]") isGroupComment: Boolean): Call<Submission>

        @PUT("courses/{courseId}/assignments/{assignmentId}/submissions/{userId}")
        suspend fun postMediaSubmissionComment(
            @Path("courseId") courseId: Long,
            @Path("assignmentId") assignmentId: Long,
            @Path("userId") userId: Long,
            @Query("comment[attempt]") attemptId: Long?,
            @Query("comment[media_comment_id]") mediaId: String,
            @Query("comment[media_comment_type]") commentType: String,
            @Query("comment[group_comment]") isGroupComment: Boolean,
            @Tag restParams: RestParams
        ): DataResult<Submission>

        @POST("{contextId}/assignments/{assignmentId}/submissions")
        fun postMediaSubmission(
                @Path("contextId") contextId: Long,
                @Path("assignmentId") assignmentId: Long,
                @Query("submission[submission_type]") submissionType: String,
                @Query("submission[media_comment_id]") notoriousId: String,
                @Query("submission[media_comment_type]") mediaType: String): Call<Submission>

        @POST("courses/{courseId}/assignments/{assignmentId}/submissions")
        fun postSubmissionAttachments(
                @Path("courseId") courseId: Long,
                @Path("assignmentId") assignmentId: Long,
                @Query("submission[submission_type]") submissionType: String,
                @Query("submission[file_ids][]") attachments: List<Long>): Call<Submission>

        @FormUrlEncoded
        @POST("{contextId}/assignments/{assignmentId}/submissions")
        fun postStudentAnnotationSubmission(
            @Path("contextId") contextId: Long,
            @Path("assignmentId") assignmentId: Long,
            @Field("submission[submission_type]") submissionType: String,
            @Field("submission[annotatable_attachment_id]") annotatableAttachmentId: Long
        ): Call<Submission>

        @GET
        fun getLtiFromAuthenticationUrl(@Url url: String): Call<LTITool>

        @GET
        suspend fun getLtiFromAuthenticationUrl(@Url url: String, @Tag restParams: RestParams): DataResult<LTITool>

        @PUT("courses/{contextId}/assignments/{assignmentId}/submissions/{userId}")
        fun postSubmissionGrade(@Path("contextId") contextId: Long,
                                @Path("assignmentId") assignmentId: Long, @Path("userId") userId: Long,
                                @Query("submission[posted_grade]") assignmentScore: String,
                                @Query("submission[excuse]") isExcused: Boolean): Call<Submission>

        @PUT("courses/{contextId}/assignments/{assignmentId}/submissions/{userId}")
        fun postSubmissionExcusedStatus(@Path("contextId") contextId: Long,
                                        @Path("assignmentId") assignmentId: Long, @Path("userId") userId: Long,
                                        @Query("submission[excuse]") isExcused: Boolean): Call<Submission>

        @GET("courses/{courseId}/assignments/{assignmentId}/submission_summary")
        fun getSubmissionSummary(@Path("courseId") courseId: Long,
                                 @Path("assignmentId") assignmentId: Long): Call<SubmissionSummary>

        @PUT("courses/{courseId}/assignments/{assignmentId}/submissions/self/read")
        fun markSubmissionAsRead(
            @Path("courseId") courseId: Long,
            @Path("assignmentId") assignmentId: Long
        ): Call<Void>
    }

    fun getSingleSubmission(courseId: Long, assignmentId: Long, studentId: Long, adapter: RestBuilder, callback: StatusCallback<Submission>, params: RestParams) {
        callback.addCall(adapter.build(SubmissionInterface::class.java, params).getSingleSubmission(courseId, assignmentId, studentId)).enqueue(callback)
    }

    fun getSubmissionsForMultipleAssignments(courseId: Long, studentId: Long, assignmentIds: List<Long>, adapter: RestBuilder, callback: StatusCallback<List<Submission>>, params: RestParams) {
        if (StatusCallback.isFirstPage(callback.linkHeaders)) {
            callback.addCall(adapter.build(SubmissionInterface::class.java, params).getSubmissionsForMultipleAssignments(courseId, studentId, assignmentIds)).enqueue(callback)
        } else if (callback.linkHeaders != null && StatusCallback.moreCallsExist(callback.linkHeaders)) {
            callback.addCall(adapter.build(SubmissionInterface::class.java, params).getNextPageSubmissions(callback.linkHeaders!!.nextUrl!!)).enqueue(callback)
        }
    }

    fun postTextSubmission(contextId: Long, assignmentId: Long, text: String, adapter: RestBuilder, params: RestParams, callback: StatusCallback<Submission>) {
        callback.addCall(adapter.build(SubmissionInterface::class.java, params).postTextSubmission(contextId, assignmentId, "online_text_entry", text)).enqueue(callback)
    }

    fun postUrlSubmission(contextId: Long, assignmentId: Long, submissionType: String, url: String, adapter: RestBuilder, params: RestParams, callback: StatusCallback<Submission>) {
        callback.addCall(adapter.build(SubmissionInterface::class.java, params).postUrlSubmission(contextId, assignmentId, submissionType, url)).enqueue(callback)
    }

    fun getLtiFromAuthenticationUrl(url: String, adapter: RestBuilder, params: RestParams, callback: StatusCallback<LTITool>) {
        callback.addCall(adapter.build(SubmissionInterface::class.java, params).getLtiFromAuthenticationUrl(url)).enqueue(callback)
    }

    fun postSubmissionGrade(courseId: Long, assignmentId: Long, userId: Long, assignmentScore: String, isExcused: Boolean, adapter: RestBuilder, callback: StatusCallback<Submission>, params: RestParams) {
        callback.addCall(adapter.build(SubmissionInterface::class.java, params).postSubmissionGrade(courseId, assignmentId, userId, assignmentScore, isExcused)).enqueue(callback)
    }

    fun postSubmissionComment(courseId: Long, assignmentID: Long, userID: Long, comment: String, isGroupMessage: Boolean, attachmentsIds: List<Long>, adapter: RestBuilder, callback: StatusCallback<Submission>, params: RestParams) {
        callback.addCall(adapter.build(SubmissionInterface::class.java, params).postSubmissionComment(courseId, assignmentID, userID, comment, isGroupMessage, attachmentsIds)).enqueue(callback)
    }

    fun postSubmissionComment(
        courseId: Long,
        assignmentID: Long,
        userID: Long,
        comment: String,
        isGroupMessage: Boolean,
        attachmentsIds: List<Long>,
        attemptId: Long?,
        adapter: RestBuilder,
        callback: StatusCallback<Submission>,
        params: RestParams
    ) {
        callback.addCall(
            adapter.build(SubmissionInterface::class.java, params).postSubmissionComment(
                courseId,
                assignmentID,
                userID,
                comment,
                attemptId,
                isGroupMessage,
                attachmentsIds
            )
        ).enqueue(callback)
    }

    fun postMediaSubmissionComment(canvasContextId: Long, assignmentId: Long, studentId: Long, mediaId: String, mediaType: String, attemptId: Long?, isGroupComment: Boolean, adapter: RestBuilder, params: RestParams, callback: StatusCallback<Submission>) {
        callback.addCall(adapter.build(SubmissionInterface::class.java, params).postMediaSubmissionComment(canvasContextId, assignmentId, studentId, attemptId, mediaId, mediaType, isGroupComment)).enqueue(callback)
    }

    fun postMediaSubmission(canvasContextId: Long, assignmentId: Long, submissionType: String, mediaId: String, mediaType: String, adapter: RestBuilder, params: RestParams, callback: StatusCallback<Submission>) {
        callback.addCall(adapter.build(SubmissionInterface::class.java, params).postMediaSubmission(canvasContextId, assignmentId, submissionType, mediaId, mediaType)).enqueue(callback)
    }

    fun postSubmissionExcusedStatus(courseId: Long, assignmentId: Long, userId: Long, isExcused: Boolean, adapter: RestBuilder, callback: StatusCallback<Submission>, params: RestParams) {
        callback.addCall(adapter.build(SubmissionInterface::class.java, params).postSubmissionExcusedStatus(courseId, assignmentId, userId, isExcused)).enqueue(callback)
    }

    fun updateRubricAssessment(courseId: Long, assignmentId: Long, userId: Long, rubricAssessment: Map<String, RubricCriterionAssessment>, adapter: RestBuilder, callback: StatusCallback<Submission>, params: RestParams) {
        val assessmentParamMap = generateRubricAssessmentQueryMap(rubricAssessment)
        callback.addCall(adapter.build(SubmissionInterface::class.java, params).postSubmissionRubricAssessmentMap(courseId, assignmentId, userId, assessmentParamMap)).enqueue(callback)
    }

    fun getSubmissionSummary(courseId: Long, assignmentId: Long, adapter: RestBuilder, params: RestParams, callback: StatusCallback<SubmissionSummary>) {
        callback.addCall(adapter.build(SubmissionInterface::class.java, params).getSubmissionSummary(courseId, assignmentId)).enqueue(callback)
    }

    fun postSubmissionAttachmentsSynchronous(courseId: Long, assignmentId: Long, attachmentsIds: List<Long>, adapter: RestBuilder, params: RestParams): Submission? {
        return try {
            adapter.build(SubmissionInterface::class.java, params).postSubmissionAttachments(courseId, assignmentId, "online_upload", attachmentsIds).execute().body()
        } catch (e: Exception) {
            null
        }
    }

    fun postStudentAnnotationSubmission(
        canvasContextId: Long,
        assignmentId: Long,
        annotatableAttachmentId: Long,
        adapter: RestBuilder,
        params: RestParams,
        callback: StatusCallback<Submission>
    ) {
        callback.addCall(
            adapter.build(SubmissionInterface::class.java, params).postStudentAnnotationSubmission(
                canvasContextId,
                assignmentId,
                Assignment.SubmissionType.STUDENT_ANNOTATION.apiString,
                annotatableAttachmentId
            )
        ).enqueue(callback)
    }

    fun markSubmissionAsRead(
        adapter: RestBuilder,
        params: RestParams,
        courseId: Long,
        assignmentId: Long,
        callback: StatusCallback<Void>
    ) {
        callback.addCall(adapter.build(SubmissionInterface::class.java, params).markSubmissionAsRead(courseId, assignmentId)).enqueue(callback)
    }

    private fun generateRubricAssessmentQueryMap(rubricAssessment: Map<String, RubricCriterionAssessment>): Map<String, String> {
        val map = mutableMapOf<String, String>()
        for ((criterionIdKey, ratingValue) in rubricAssessment) {
            ratingValue.points?.let { map[assessmentPrefix + criterionIdKey + pointsPostFix] = it.toString() }
            ratingValue.ratingId?.let { map[assessmentPrefix + criterionIdKey + ratingIdPostFix] = it }
            map[assessmentPrefix + criterionIdKey + commentsPostFix] = ratingValue.comments?.let { it } ?: ""
        }
        return map
    }
}
