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
import com.instructure.canvasapi2.models.Enrollment
import com.instructure.canvasapi2.utils.DataResult
import retrofit2.Call
import retrofit2.http.*

object EnrollmentAPI {

    const val STUDENT_ENROLLMENT = "StudentEnrollment"

    const val STATE_ACTIVE = "active"
    const val STATE_INVITED = "invited"
    const val STATE_COMPLETED = "completed"
    const val STATE_DELETED = "deleted"
    const val STATE_CREATION_PENDING = "creation_pending"
    const val STATE_CURRENT_AND_FUTURE = "current_and_future"

    interface EnrollmentInterface {

        @get:GET("users/self/enrollments?include[]=observed_users&include[]=avatar_url&state[]=creation_pending&state[]=invited&state[]=active&state[]=completed")
        val firstPageObserveeEnrollments: Call<List<Enrollment>>

        @GET("users/self/enrollments?include[]=observed_users&include[]=avatar_url&state[]=creation_pending&state[]=invited&state[]=active&state[]=completed")
        suspend fun firstPageObserveeEnrollments(@Tag params: RestParams): DataResult<List<Enrollment>>

        @GET("users/self/enrollments?include[]=observed_users&include[]=avatar_url&state[]=creation_pending&state[]=invited&state[]=active&state[]=completed&state[]=current_and_future")
        suspend fun firstPageObserveeEnrollmentsParent(@Tag params: RestParams): DataResult<List<Enrollment>>

        @GET("courses/{courseId}/enrollments?include[]=avatar_url&state[]=active")
        fun getFirstPageEnrollmentsForCourse(
                @Path("courseId") courseId: Long,
                @Query("type[]") enrollmentType: String?): Call<List<Enrollment>>

        @GET("courses/{courseId}/enrollments?include[]=avatar_url&state[]=active")
        suspend fun getFirstPageEnrollmentsForCourse(
            @Path("courseId") courseId: Long,
            @Query("type[]") enrollmentType: String?,
            @Tag restParams: RestParams): DataResult<List<Enrollment>>

        @GET("courses/{courseId}/enrollments?userId")
        fun getFirstPageEnrollmentsForUserInCourse(
                @Path("courseId") courseId: Long,
                @Query("userId") userId: Long,
                @Query("type[]") enrollmentTypes: Array<String>): Call<List<Enrollment>>

        @GET("courses/{courseId}/enrollments?include[]=current_points")
        suspend fun getEnrollmentsForUserInCourse(
            @Path("courseId") courseId: Long,
            @Query("user_id") userId: Long,
            @Tag restParams: RestParams
        ): DataResult<List<Enrollment>>

        @GET
        fun getNextPage(@Url nextUrl: String): Call<List<Enrollment>>

        @GET
        suspend fun getNextPage(@Url nextUrl: String, @Tag params: RestParams): DataResult<List<Enrollment>>

        @GET("users/self/enrollments")
        fun getFirstPageSelfEnrollments(
                @Query("type[]") types: List<String>?,
                @Query("state[]") states: List<String>?): Call<List<Enrollment>>

        @POST("courses/{courseId}/enrollments/{enrollmentId}/{action}")
        fun handleInvite(@Path("courseId") courseId: Long, @Path("enrollmentId") enrollmentId: Long, @Path("action") action: String): Call<Unit>

        @POST("courses/{courseId}/enrollments/{enrollmentId}/accept")
        suspend fun acceptInvite(@Path("courseId") courseId: Long, @Path("enrollmentId") enrollmentId: Long, @Tag params: RestParams): DataResult<Unit>

        @GET("users/self/enrollments?state[]=active&type[]=StudentEnrollment")
        fun getFirstPageEnrollmentsForGradingPeriod(@Query("grading_period_id") gradingPeriodId: Long): Call<List<Enrollment>>
    }

    fun getFirstPageEnrollmentsForCourse(
            adapter: RestBuilder,
            params: RestParams,
            courseId: Long,
            enrollmentType: String?,
            callback: StatusCallback<List<Enrollment>>) {
        callback.addCall(adapter.build(EnrollmentInterface::class.java, params)
                .getFirstPageEnrollmentsForCourse(courseId, enrollmentType)).enqueue(callback)
    }

    fun getFirstPageEnrollmentsForUserInCourse(
            adapter: RestBuilder,
            params: RestParams,
            courseId: Long, userId: Long,
            callback: StatusCallback<List<Enrollment>>) {

        val enrollmentTypes = arrayOf("TeacherEnrollment", "TaEnrollment", "DesignerEnrollment")

        callback.addCall(adapter.build(EnrollmentInterface::class.java, params).getFirstPageEnrollmentsForUserInCourse(courseId, userId, enrollmentTypes)).enqueue(callback)
    }

    fun getNextPageEnrollments(forceNetwork: Boolean, nextUrl: String, adapter: RestBuilder, callback: StatusCallback<List<Enrollment>>) {
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)
        callback.addCall(adapter.build(EnrollmentInterface::class.java, params).getNextPage(nextUrl)).enqueue(callback)
    }

    fun getSelfEnrollments(
            types: List<String>?,
            states: List<String>?,
            adapter: RestBuilder,
            params: RestParams,
            callback: StatusCallback<List<Enrollment>>) {
        if (StatusCallback.isFirstPage(callback.linkHeaders)) {
            callback.addCall(adapter.build(EnrollmentInterface::class.java, params).getFirstPageSelfEnrollments(types, states)).enqueue(callback)
        } else if (callback.linkHeaders != null && StatusCallback.moreCallsExist(callback.linkHeaders)) {
            callback.addCall(adapter.build(EnrollmentInterface::class.java, params).getNextPage(callback.linkHeaders!!.nextUrl!!)).enqueue(callback)
        }
    }

    fun getObserveeEnrollments(
            adapter: RestBuilder,
            params: RestParams,
            callback: StatusCallback<List<Enrollment>>) {
        if (StatusCallback.isFirstPage(callback.linkHeaders)) {
            callback.addCall(adapter.build(EnrollmentInterface::class.java, params).firstPageObserveeEnrollments).enqueue(callback)
        } else if (callback.linkHeaders != null && StatusCallback.moreCallsExist(callback.linkHeaders)) {
            callback.addCall(adapter.build(EnrollmentInterface::class.java, params).getNextPage(callback.linkHeaders!!.nextUrl!!)).enqueue(callback)
        }
    }

    fun handleInvite(courseId: Long, enrollmentId: Long, acceptInvite: Boolean, adapter: RestBuilder, params: RestParams, callback: StatusCallback<Unit>) {
        callback.addCall(adapter.build(EnrollmentInterface::class.java, params).handleInvite(courseId, enrollmentId, if (acceptInvite) "accept" else "reject")).enqueue(callback)
    }

    fun getEnrollmentsForGradingPeriod(
        gradingPeriodId: Long,
        adapter: RestBuilder,
        params: RestParams,
        callback: StatusCallback<List<Enrollment>>) {
        if (StatusCallback.isFirstPage(callback.linkHeaders)) {
            callback.addCall(adapter.build(EnrollmentInterface::class.java, params).getFirstPageEnrollmentsForGradingPeriod(gradingPeriodId)).enqueue(callback)
        } else if (callback.linkHeaders != null && StatusCallback.moreCallsExist(callback.linkHeaders)) {
            callback.addCall(adapter.build(EnrollmentInterface::class.java, params).getNextPage(callback.linkHeaders!!.nextUrl!!)).enqueue(callback)
        }
    }
}
