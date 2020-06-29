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
import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.models.postmodels.AssignmentPostBodyWrapper
import retrofit2.Call
import retrofit2.http.*


object AssignmentAPI {
    internal interface AssignmentInterface {
        @GET("courses/{courseId}/assignments/{assignmentId}?include[]=submission&include[]=rubric_assessment&needs_grading_count_by_section=true&override_assignment_dates=true&all_dates=true&include[]=overrides")
        fun getAssignment(@Path("courseId") courseId: Long, @Path("assignmentId") assignmentId: Long): Call<Assignment>

        @GET("courses/{courseId}/assignments/{assignmentId}?include[]=submission&include[]=rubric_assessment&needs_grading_count_by_section=true&override_assignment_dates=true&all_dates=true&include[]=overrides&include[]=observed_users")
        fun getAssignmentIncludeObservees(@Path("courseId") courseId: Long, @Path("assignmentId") assignmentId: Long): Call<ObserveeAssignment>

        @GET("courses/{courseId}/assignment_groups/{assignmentGroupId}")
        fun getAssignmentGroup(
                @Path("courseId") courseId: Long,
                @Path("assignmentGroupId") assignmentId: Long): Call<AssignmentGroup>

        @GET("courses/{courseId}/assignment_groups?include[]=assignments&include[]=discussion_topic&include[]=submission&override_assignment_dates=true&include[]=all_dates&include[]=overrides")
        fun getFirstPageAssignmentGroupListWithAssignments(@Path("courseId") courseId: Long): Call<List<AssignmentGroup>>

        @GET
        fun getNextPageAssignmentGroupListWithAssignments(@Url nextUrl: String): Call<List<AssignmentGroup>>

        // https://canvas.instructure.com/doc/api/all_resources.html#method.submissions_api.for_students
        @GET("courses/{courseId}/assignment_groups?include[]=assignments&include[]=discussion_topic&include[]=submission&override_assignment_dates=true&include[]=all_dates&include[]=overrides")
        fun getFirstPageAssignmentGroupListWithAssignmentsForGradingPeriod(@Path("courseId") courseId: Long, @Query("grading_period_id") gradingPeriodId: Long, @Query("scope_assignments_to_student") scopeToStudent: Boolean, @Query("order") order: String = "id"): Call<List<AssignmentGroup>>

        @GET
        fun getNextPageAssignmentGroupListWithAssignmentsForGradingPeriod(@Url nextUrl: String): Call<List<AssignmentGroup>>

        @GET
        fun getNextPage(@Url nextUrl: String): Call<List<AssignmentGroup>>

        @PUT("courses/{courseId}/assignments/{assignmentId}")
        fun editAssignment(
                @Path("courseId") courseId: Long,
                @Path("assignmentId") assignmentId: Long,
                @Body body: AssignmentPostBodyWrapper): Call<Assignment>

        @GET("courses/{courseId}/assignments/{assignmentId}/gradeable_students")
        fun getFirstPageGradeableStudentsForAssignment(@Path("courseId") courseId: Long, @Path("assignmentId") assignmentId: Long): Call<List<GradeableStudent>>

        @GET
        fun getNextPageGradeableStudents(@Url nextUrl: String): Call<List<GradeableStudent>>

        @GET("courses/{courseId}/assignments/{assignmentId}/submissions?include[]=rubric_assessment&include[]=submission_history&include[]=submission_comments&include[]=group")
        fun getFirstPageSubmissionsForAssignment(@Path("courseId") courseId: Long, @Path("assignmentId") assignmentId: Long): Call<List<Submission>>

        @GET
        fun getNextPageSubmissions(@Url nextUrl: String): Call<List<Submission>>

        @GET("courses/{courseId}/assignments?include[]=submission&include[]=rubric_assessment&needs_grading_count_by_section=true&override_assignment_dates=true&include[]=all_dates&include[]=overrides")
        fun getAssignments(@Path("courseId") courseId: Long): Call<List<Assignment>>

        @GET
        fun getNextPageAssignments(@Url nextUrl: String): Call<List<Assignment>>
    }

    fun getAssignment(courseId: Long, assignmentId: Long, adapter: RestBuilder, callback: StatusCallback<Assignment>, params: RestParams) {
        callback.addCall(adapter.build(AssignmentInterface::class.java, params).getAssignment(courseId, assignmentId)).enqueue(callback)
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

    fun editAssignmentAllowNullValues(courseId: Long, assignmentId: Long, body: AssignmentPostBodyWrapper, adapter: RestBuilder, callback: StatusCallback<Assignment>, params: RestParams) {
        callback.addCall(adapter.build(AssignmentInterface::class.java, params).editAssignment(courseId, assignmentId, body)).enqueue(callback)
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