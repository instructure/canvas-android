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
import com.instructure.canvasapi2.models.AssignmentGroup
import com.instructure.canvasapi2.models.GradeableStudent
import com.instructure.canvasapi2.models.LTITool
import com.instructure.canvasapi2.models.ObserveeAssignment
import com.instructure.canvasapi2.models.ObserveeAssignmentGroup
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.models.postmodels.AssignmentPostBodyWrapper
import com.instructure.canvasapi2.models.postmodels.QuizAssignmentPostBodyWrapper
import com.instructure.canvasapi2.utils.DataResult
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Tag
import retrofit2.http.Url


object AssignmentAPI {
    interface AssignmentInterface {
        @GET("courses/{courseId}/external_tools/sessionless_launch")
        fun getExternalToolLaunchUrl(@Path("courseId") courseId: Long, @Query("id") externalToolId: Long, @Query("assignment_id") assignmentId: Long, @Query("launch_type") launchType: String = "assessment"): Call<LTITool>

        @GET("courses/{courseId}/external_tools/sessionless_launch")
        suspend fun getExternalToolLaunchUrl(
            @Path("courseId") courseId: Long,
            @Query("id") externalToolId: Long,
            @Query("assignment_id") assignmentId: Long,
            @Query("launch_type") launchType: String = "assessment",
            @Tag restParams: RestParams
        ): DataResult<LTITool>

        @GET("courses/{courseId}/assignments/{assignmentId}?include[]=submission&include[]=rubric_assessment&needs_grading_count_by_section=true&override_assignment_dates=true&all_dates=true&include[]=overrides&include[]=score_statistics")
        fun getAssignment(@Path("courseId") courseId: Long, @Path("assignmentId") assignmentId: Long): Call<Assignment>

        @GET("courses/{courseId}/assignments/{assignmentId}?include[]=submission&include[]=rubric_assessment&needs_grading_count_by_section=true&override_assignment_dates=true&all_dates=true&include[]=overrides&include[]=score_statistics")
        suspend fun getAssignment(
            @Path("courseId") courseId: Long,
            @Path("assignmentId") assignmentId: Long,
            @Tag params: RestParams
        ): DataResult<Assignment>

        @GET("courses/{courseId}/assignments/{assignmentId}?include[]=submission&include[]=rubric_assessment&needs_grading_count_by_section=true&override_assignment_dates=true&all_dates=true&include[]=overrides&include[]=score_statistics&include[]=submission_history")
        fun getAssignmentWithHistory(@Path("courseId") courseId: Long, @Path("assignmentId") assignmentId: Long): Call<Assignment>

        @GET("courses/{courseId}/assignments/{assignmentId}?include[]=submission&include[]=rubric_assessment&needs_grading_count_by_section=true&override_assignment_dates=true&all_dates=true&include[]=overrides&include[]=score_statistics&include[]=submission_history&include[]=checkpoints&include[]=discussion_topic&include[]=sub_assignment_submissions")
        suspend fun getAssignmentWithHistory(
            @Path("courseId") courseId: Long,
            @Path("assignmentId") assignmentId: Long,
            @Tag restParams: RestParams
        ): DataResult<Assignment>

        @GET("courses/{courseId}/assignments/{assignmentId}?include[]=submission&include[]=rubric_assessment&needs_grading_count_by_section=true&override_assignment_dates=true&all_dates=true&include[]=overrides&include[]=observed_users&include[]=score_statistics&include[]=submission_history")
        fun getAssignmentIncludeObservees(@Path("courseId") courseId: Long, @Path("assignmentId") assignmentId: Long): Call<ObserveeAssignment>

        @GET("courses/{courseId}/assignments/{assignmentId}?include[]=submission&include[]=rubric_assessment&needs_grading_count_by_section=true&override_assignment_dates=true&all_dates=true&include[]=overrides&include[]=observed_users&include[]=score_statistics&include[]=submission_history&include[]=checkpoints&include[]=discussion_topic&include[]=sub_assignment_submissions")
        suspend fun getAssignmentIncludeObservees(
            @Path("courseId") courseId: Long,
            @Path("assignmentId") assignmentId: Long,
            @Tag restParams: RestParams
        ): DataResult<ObserveeAssignment>

        @GET("courses/{courseId}/assignment_groups/{assignmentGroupId}")
        fun getAssignmentGroup(
                @Path("courseId") courseId: Long,
                @Path("assignmentGroupId") assignmentId: Long): Call<AssignmentGroup>

        @GET("courses/{courseId}/assignment_groups?include[]=assignments&include[]=discussion_topic&include[]=submission&override_assignment_dates=true&include[]=all_dates&include[]=overrides")
        fun getFirstPageAssignmentGroupListWithAssignments(@Path("courseId") courseId: Long): Call<List<AssignmentGroup>>

        @GET("courses/{courseId}/assignment_groups?include[]=assignments&include[]=discussion_topic&include[]=submission&include[]=rubric_assessment&override_assignment_dates=true&include[]=all_dates&include[]=overrides&include[]=submission_history&include[]=submission_comments&include[]=score_statistics&include[]=checkpoints&include[]=discussion_topic&include[]=sub_assignment_submissions")
        suspend fun getFirstPageAssignmentGroupListWithAssignments(@Path("courseId") courseId: Long, @Tag restParams: RestParams): DataResult<List<AssignmentGroup>>

        @GET
        fun getNextPageAssignmentGroupListWithAssignments(@Url nextUrl: String): Call<List<AssignmentGroup>>

        @GET
        suspend fun getNextPageAssignmentGroupListWithAssignments(@Url nextUrl: String, @Tag restParams: RestParams): DataResult<List<AssignmentGroup>>

        @GET
        suspend fun getNextPageAssignmentGroupListWithAssignmentsForObserver(@Url nextUrl: String, @Tag restParams: RestParams): DataResult<List<ObserveeAssignmentGroup>>

        // https://canvas.instructure.com/doc/api/all_resources.html#method.submissions_api.for_students
        @GET("courses/{courseId}/assignment_groups?include[]=assignments&include[]=discussion_topic&include[]=submission&override_assignment_dates=true&include[]=all_dates&include[]=overrides")
        fun getFirstPageAssignmentGroupListWithAssignmentsForGradingPeriod(@Path("courseId") courseId: Long, @Query("grading_period_id") gradingPeriodId: Long, @Query("scope_assignments_to_student") scopeToStudent: Boolean, @Query("order") order: String = "id"): Call<List<AssignmentGroup>>

        @GET("courses/{courseId}/assignment_groups?include[]=assignments&include[]=discussion_topic&include[]=submission&override_assignment_dates=true&include[]=all_dates&include[]=overrides")
        suspend fun getFirstPageAssignmentGroupListWithAssignmentsForGradingPeriod(
            @Path("courseId") courseId: Long,
            @Query("grading_period_id") gradingPeriodId: Long,
            @Query("scope_assignments_to_student") scopeToStudent: Boolean,
            @Query("order") order: String = "id",
            @Tag restParams: RestParams
        ): DataResult<List<AssignmentGroup>>

        @GET("courses/{courseId}/assignment_groups?include[]=assignments&include[]=discussion_topic&include[]=submission&include[]=all_dates&include[]=overrides&include[]=observed_users&override_assignment_dates=true&include[]=checkpoints&include[]=discussion_topic&include[]=sub_assignment_submissions")
        suspend fun getFirstPageAssignmentGroupListWithAssignmentsForObserver(
            @Path("courseId") courseId: Long,
            @Query("grading_period_id") gradingPeriodId: Long?,
            @Tag restParams: RestParams
        ): DataResult<List<ObserveeAssignmentGroup>>

        @GET
        fun getNextPageAssignmentGroupListWithAssignmentsForGradingPeriod(@Url nextUrl: String): Call<List<AssignmentGroup>>

        @GET
        suspend fun getNextPageAssignmentGroupListWithAssignmentsForGradingPeriod(
            @Url nextUrl: String,
            @Tag restParams: RestParams
        ): DataResult<List<AssignmentGroup>>

        @GET
        fun getNextPage(@Url nextUrl: String): Call<List<AssignmentGroup>>

        @PUT("courses/{courseId}/assignments/{assignmentId}")
        fun editAssignment(
                @Path("courseId") courseId: Long,
                @Path("assignmentId") assignmentId: Long,
                @Body body: AssignmentPostBodyWrapper): Call<Assignment>

        @PUT("courses/{courseId}/assignments/{assignmentId}")
        fun editQuizAssignment(
            @Path("courseId") courseId: Long,
            @Path("assignmentId") assignmentId: Long,
            @Body body: QuizAssignmentPostBodyWrapper
        ): Call<Assignment>

        @GET("courses/{courseId}/assignments/{assignmentId}/gradeable_students")
        fun getFirstPageGradeableStudentsForAssignment(@Path("courseId") courseId: Long, @Path("assignmentId") assignmentId: Long): Call<List<GradeableStudent>>

        @GET("courses/{courseId}/assignments/{assignmentId}/gradeable_students")
        suspend fun getFirstPageGradeableStudentsForAssignment(
            @Path("courseId") courseId: Long,
            @Path("assignmentId") assignmentId: Long,
            @Tag restParams: RestParams
        ): DataResult<List<GradeableStudent>>

        @GET
        fun getNextPageGradeableStudents(@Url nextUrl: String): Call<List<GradeableStudent>>

        @GET
        suspend fun getNextPageGradeableStudents(@Url nextUrl: String, @Tag restParams: RestParams): DataResult<List<GradeableStudent>>

        @GET("courses/{courseId}/assignments/{assignmentId}/submissions?include[]=rubric_assessment&include[]=submission_history&include[]=submission_comments&include[]=group")
        fun getFirstPageSubmissionsForAssignment(@Path("courseId") courseId: Long, @Path("assignmentId") assignmentId: Long): Call<List<Submission>>

        @GET("courses/{courseId}/assignments/{assignmentId}/submissions?include[]=rubric_assessment&include[]=submission_history&include[]=submission_comments&include[]=group")
        suspend fun getFirstPageSubmissionsForAssignment(@Path("courseId") courseId: Long, @Path("assignmentId") assignmentId: Long, @Tag params: RestParams): DataResult<List<Submission>>

        @GET
        fun getNextPageSubmissions(@Url nextUrl: String): Call<List<Submission>>

        @GET
        suspend fun getNextPageSubmissions(@Url nextUrl: String, @Tag restParams: RestParams): DataResult<List<Submission>>

        @GET("courses/{courseId}/assignments?include[]=submission&include[]=rubric_assessment&needs_grading_count_by_section=true&override_assignment_dates=true&include[]=all_dates&include[]=overrides")
        fun getAssignments(@Path("courseId") courseId: Long): Call<List<Assignment>>

        @GET
        fun getNextPageAssignments(@Url nextUrl: String): Call<List<Assignment>>
    }

    fun getExternalToolLaunchUrl(courseId: Long, externalToolId: Long, assignmentId: Long, adapter: RestBuilder, callback: StatusCallback<LTITool>, params: RestParams) {
        callback.addCall(adapter.build(AssignmentInterface::class.java, params).getExternalToolLaunchUrl(courseId, externalToolId, assignmentId)).enqueue(callback)
    }

    fun getAssignment(courseId: Long, assignmentId: Long, adapter: RestBuilder, callback: StatusCallback<Assignment>, params: RestParams) {
        callback.addCall(adapter.build(AssignmentInterface::class.java, params).getAssignment(courseId, assignmentId)).enqueue(callback)
    }

    fun getAssignmentWithHistory(courseId: Long, assignmentId: Long, adapter: RestBuilder, callback: StatusCallback<Assignment>, params: RestParams) {
        callback.addCall(adapter.build(AssignmentInterface::class.java, params).getAssignmentWithHistory(courseId, assignmentId)).enqueue(callback)
    }

    fun getAssignmentIncludeObservees(courseId: Long, assignmentId: Long, adapter: RestBuilder, callback: StatusCallback<ObserveeAssignment>, params: RestParams) {
        callback.addCall(adapter.build(AssignmentInterface::class.java, params).getAssignmentIncludeObservees(courseId, assignmentId)).enqueue(callback)
    }

    fun getAssignmentGroup(courseId: Long, assignmentGroupId: Long, adapter: RestBuilder, callback: StatusCallback<AssignmentGroup>, params: RestParams) {
        callback.addCall(adapter.build(AssignmentInterface::class.java, params).getAssignmentGroup(courseId, assignmentGroupId)).enqueue(callback)
    }

    fun getFirstPageAssignmentGroupsWithAssignments(courseId: Long, adapter: RestBuilder, callback: StatusCallback<List<AssignmentGroup>>, params: RestParams) {
        callback.addCall(adapter.build(AssignmentInterface::class.java, params).getFirstPageAssignmentGroupListWithAssignments(courseId)).enqueue(callback)
    }

    fun getNextPageAssignmentGroupsWithAssignments(forceNetwork: Boolean, nextUrl: String, adapter: RestBuilder, callback: StatusCallback<List<AssignmentGroup>>) {
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)
        callback.addCall(adapter.build(AssignmentInterface::class.java, params).getNextPageAssignmentGroupListWithAssignments(nextUrl)).enqueue(callback)
    }

    fun getFirstPageAssignmentGroupsWithAssignmentsForGradingPeriod(courseId: Long, gradingPeriodId: Long, scopeToStudent: Boolean, adapter: RestBuilder, callback: StatusCallback<List<AssignmentGroup>>, params: RestParams) {
        callback.addCall(adapter.build(AssignmentInterface::class.java, params).getFirstPageAssignmentGroupListWithAssignmentsForGradingPeriod(courseId, gradingPeriodId, scopeToStudent)).enqueue(callback)
    }

    fun getNextPageAssignmentGroupsWithAssignmentsForGradingPeriod(forceNetwork: Boolean, nextUrl: String, adapter: RestBuilder, callback: StatusCallback<List<AssignmentGroup>>) {
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)
        callback.addCall(adapter.build(AssignmentInterface::class.java, params).getNextPageAssignmentGroupListWithAssignmentsForGradingPeriod(nextUrl)).enqueue(callback)
    }

    fun editAssignment(courseId: Long, assignmentId: Long, body: AssignmentPostBodyWrapper, adapter: RestBuilder, callback: StatusCallback<Assignment>, params: RestParams, serializeNulls: Boolean) {
        if (serializeNulls) {
            callback.addCall(adapter.buildSerializeNulls(AssignmentInterface::class.java, params).editAssignment(courseId, assignmentId, body)).enqueue(callback)
        } else {
            callback.addCall(adapter.build(AssignmentInterface::class.java, params).editAssignment(courseId, assignmentId, body)).enqueue(callback)
        }
    }

    fun editQuizAssignment(courseId: Long, assignmentId: Long, body: QuizAssignmentPostBodyWrapper, adapter: RestBuilder, callback: StatusCallback<Assignment>, params: RestParams) {
        callback.addCall(adapter.buildSerializeNulls(AssignmentInterface::class.java, params).editQuizAssignment(courseId, assignmentId, body)).enqueue(callback)
    }

    fun getFirstPageGradeableStudentsForAssignment(courseId: Long, assignmentId: Long, adapter: RestBuilder, callback: StatusCallback<List<GradeableStudent>>) {
        val params = RestParams(usePerPageQueryParam = true)
        callback.addCall(adapter.build(AssignmentInterface::class.java, params).getFirstPageGradeableStudentsForAssignment(courseId, assignmentId)).enqueue(callback)
    }

    fun getNextPageGradeableStudents(forceNetwork: Boolean, nextUrl: String, adapter: RestBuilder, callback: StatusCallback<List<GradeableStudent>>) {
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)
        callback.addCall(adapter.build(AssignmentInterface::class.java, params).getNextPageGradeableStudents(nextUrl)).enqueue(callback)
    }

    fun getFirstPageSubmissionsForAssignment(courseId: Long, assignmentId: Long, forceNetwork: Boolean, adapter: RestBuilder, callback: StatusCallback<List<Submission>>) {
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)
        callback.addCall(adapter.build(AssignmentInterface::class.java, params).getFirstPageSubmissionsForAssignment(courseId, assignmentId)).enqueue(callback)
    }

    fun getNextPageSubmissions(nextUrl: String, adapter: RestBuilder, forceNetwork: Boolean, callback: StatusCallback<List<Submission>>) {
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)
        callback.addCall(adapter.build(AssignmentInterface::class.java, params).getNextPageSubmissions(nextUrl)).enqueue(callback)
    }

    fun getFirstPageAssignments(courseId: Long, forceNetwork: Boolean, adapter: RestBuilder, callback: StatusCallback<List<Assignment>>) {
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)
        callback.addCall(adapter.build(AssignmentInterface::class.java, params).getAssignments(courseId)).enqueue(callback)
    }

    fun getNextPageAssignments(nextUrl: String, adapter: RestBuilder, forceNetwork: Boolean, callback: StatusCallback<List<Assignment>>) {
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)
        callback.addCall(adapter.build(AssignmentInterface::class.java, params).getNextPageAssignments(nextUrl)).enqueue(callback)
    }
}