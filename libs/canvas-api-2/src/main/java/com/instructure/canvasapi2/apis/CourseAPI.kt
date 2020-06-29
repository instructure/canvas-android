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
import com.instructure.canvasapi2.utils.APIHelper
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*
import java.io.IOException


object CourseAPI {

    internal interface CoursesInterface {

        @get:GET("users/self/favorites/courses?include[]=term&include[]=total_scores&include[]=license&include[]=is_public&include[]=needs_grading_count&include[]=permissions&include[]=current_grading_period_scores&include[]=course_image&include[]=favorites")
        val favoriteCourses: Call<List<Course>>

        @get:GET("dashboard/dashboard_cards")
        val dashboardCourses: Call<List<DashboardCard>>

        @get:GET("courses?include[]=term&include[]=total_scores&include[]=license&include[]=is_public&include[]=needs_grading_count&include[]=permissions&include[]=favorites&include[]=current_grading_period_scores&include[]=course_image&include[]=sections&state[]=completed&state[]=available")
        val firstPageCourses: Call<List<Course>>

        @get:GET("courses?include[]=term&include[]=syllabus_body&include[]=total_scores&include[]=license&include[]=is_public&include[]=needs_grading_count&include[]=permissions&include[]=favorites&include[]=current_grading_period_scores&include[]=course_image&include[]=sections&state[]=completed&state[]=available&include[]=observed_users")
        val firstPageCoursesWithSyllabus: Call<List<Course>>

        @get:GET("courses?include[]=term&include[]=total_scores&include[]=license&include[]=is_public&include[]=needs_grading_count&include[]=permissions&include[]=favorites&include[]=current_grading_period_scores&include[]=course_image&include[]=sections&state[]=completed&state[]=available&state[]=unpublished")
        val firstPageCoursesTeacher: Call<List<Course>>

        @GET("courses/{courseId}?include[]=term&include[]=permissions&include[]=license&include[]=is_public&include[]=needs_grading_count&include[]=course_image")
        fun getCourse(@Path("courseId") courseId: Long): Call<Course>

        @GET("courses/{courseId}/settings")
        fun getCourseSettings(@Path("courseId") courseId: Long): Call<CourseSettings>

        @GET("courses/{courseId}?include[]=syllabus_body&include[]=term&include[]=license&include[]=is_public&include[]=permissions")
        fun getCourseWithSyllabus(@Path("courseId") courseId: Long): Call<Course>

        @GET("courses/{courseId}?include[]=term&include[]=permissions&include[]=license&include[]=is_public&include[]=needs_grading_count&include[]=total_scores&include[]=current_grading_period_scores&include[]=course_image")
        fun getCourseWithGrade(@Path("courseId") courseId: Long): Call<Course>

        @GET
        fun next(@Url nextURL: String): Call<List<Course>>

        @GET("courses?state[]=completed&state[]=available&state[]=unpublished")
        fun getCoursesByEnrollmentType(@Query("enrollment_type") type: String): Call<List<Course>>

        // TODO: Set up pagination when API is fixed and remove per_page query parameterø
        @GET("courses/{courseId}/grading_periods?per_page=100")
        fun getGradingPeriodsForCourse(@Path("courseId") courseId: Long): Call<GradingPeriodResponse>

        @GET("courses/{courseId}/users/{studentId}?include[]=avatar_url&include[]=enrollments&include[]=inactive_enrollments&include[]=current_grading_period_scores&include[]=email")
        fun getCourseStudent(@Path("courseId") courseId: Long, @Path("studentId") studentId: Long): Call<User>

        @POST("users/self/favorites/courses/{courseId}")
        fun addCourseToFavorites(@Path("courseId") courseId: Long): Call<Favorite>

        @DELETE("users/self/favorites/courses/{courseId}")
        fun removeCourseFromFavorites(@Path("courseId") courseId: Long): Call<Favorite>

        @PUT("courses/{course_id}")
        fun updateCourse(@Path("course_id") courseId: Long, @QueryMap params: Map<String, String>): Call<Course>

        @GET("courses/{courseId}/groups?include[]=users")
        fun getFirstPageGroups(@Path("courseId") courseId: Long): Call<List<Group>>

        @GET
        fun getNextPageGroups(@Url nextUrl: String): Call<List<Group>>

        @GET("courses/{courseId}/permissions")
        fun getCoursePermissions(@Path("courseId") courseId: Long, @Query("permissions[]") requestedPermissions: List<String>): Call<CanvasContextPermission>

        @GET("courses/{courseId}/enrollments?state[]=current_and_concluded")
        fun getUserEnrollmentsForGradingPeriod(@Path("courseId") courseId: Long, @Query("user_id") userId: Long, @Query("grading_period_id") gradingPeriodId: Long): Call<List<Enrollment>>

        @GET("courses/{courseId}/rubrics/{rubricId}")
        fun getRubricSettings(@Path("courseId") courseId: Long, @Path("rubricId") rubricId: Long): Call<RubricSettings>
    }

    @Throws(IOException::class)
    fun getCoursesSynchronously(adapter: RestBuilder, params: RestParams): List<Course>? {
        val firstPageResponse = adapter.build(CoursesInterface::class.java, params).firstPageCourses.execute()
        return getCoursesRecursive(adapter, params, firstPageResponse, firstPageResponse.body())
    }

    private fun getCoursesRecursive(adapter: RestBuilder, params: RestParams, response: Response<List<Course>>, data: List<Course>?): List<Course>? {
        val linkHeaders = APIHelper.parseLinkHeaderResponse(response.headers())
        val list = data?.toMutableList()

        return if (linkHeaders.nextUrl != null) {
            try {
                val nextPageResponse = adapter.build(CoursesInterface::class.java, params).next(linkHeaders.nextUrl!!).execute()
                nextPageResponse.body()?.let { courses ->
                    list?.addAll(courses)
                }
                getCoursesRecursive(adapter, params, nextPageResponse, data)
            } catch (e: IOException) {
                null
            }

        } else {
            data
        }

    }

    fun getFirstPageFavoriteCourses(adapter: RestBuilder, callback: StatusCallback<List<Course>>, params: RestParams) {
        callback.addCall(adapter.build(CoursesInterface::class.java, params).favoriteCourses).enqueue(callback)
    }

    fun getNextPageFavoriteCourses(forceNetwork: Boolean, nextUrl: String, adapter: RestBuilder, callback: StatusCallback<List<Course>>) {
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)
        callback.addCall(adapter.build(CoursesInterface::class.java, params).next(nextUrl)).enqueue(callback)
    }

    fun getCourses(adapter: RestBuilder, callback: StatusCallback<List<Course>>, params: RestParams) {
        if (StatusCallback.isFirstPage(callback.linkHeaders)) {
            callback.addCall(adapter.build(CoursesInterface::class.java, params).firstPageCourses).enqueue(callback)
        } else if (callback.linkHeaders != null && StatusCallback.moreCallsExist(callback.linkHeaders)) {
            callback.addCall(adapter.build(CoursesInterface::class.java, params).next(callback.linkHeaders!!.nextUrl!!)).enqueue(callback)
        }
    }

    fun getDashboardCourses(adapter: RestBuilder, callback: StatusCallback<List<DashboardCard>>, params: RestParams) {
        callback.addCall(adapter.build(CoursesInterface::class.java, params).dashboardCourses).enqueue(callback)
    }

    fun getFirstPageCourses(adapter: RestBuilder, callback: StatusCallback<List<Course>>, params: RestParams) {
        callback.addCall(adapter.build(CoursesInterface::class.java, params).firstPageCourses).enqueue(callback)
    }

    fun getFirstPageCoursesWithSyllabus(adapter: RestBuilder, callback: StatusCallback<List<Course>>, params: RestParams) {
        callback.addCall(adapter.build(CoursesInterface::class.java, params).firstPageCoursesWithSyllabus).enqueue(callback)
    }

    fun getFirstPageCoursesTeacher(adapter: RestBuilder, callback: StatusCallback<List<Course>>, params: RestParams) {
        callback.addCall(adapter.build(CoursesInterface::class.java, params).firstPageCoursesTeacher).enqueue(callback)
    }

    fun getNextPageCourses(forceNetwork: Boolean, nextUrl: String, adapter: RestBuilder, callback: StatusCallback<List<Course>>) {
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)
        callback.addCall(adapter.build(CoursesInterface::class.java, params).next(nextUrl)).enqueue(callback)
    }

    // TODO: Set up pagination when API is fixed. API currently sends pagination data in body instead of headers
    fun getGradingPeriodsForCourse(adapter: RestBuilder, callback: StatusCallback<GradingPeriodResponse>, params: RestParams, courseId: Long) {
        callback.addCall(adapter.build(CoursesInterface::class.java, params).getGradingPeriodsForCourse(courseId)).enqueue(callback)
    }

    fun getCourse(courseId: Long, adapter: RestBuilder, callback: StatusCallback<Course>, params: RestParams) {
        callback.addCall(adapter.build(CoursesInterface::class.java, params).getCourse(courseId)).enqueue(callback)
    }

    fun getCourseSettings(courseId: Long, adapter: RestBuilder, callback: StatusCallback<CourseSettings>, params: RestParams) {
        callback.addCall(adapter.build(CoursesInterface::class.java, params).getCourseSettings(courseId)).enqueue(callback)
    }

    fun getCourseWithSyllabus(courseId: Long, adapter: RestBuilder, callback: StatusCallback<Course>, params: RestParams) {
        callback.addCall(adapter.build(CoursesInterface::class.java, params).getCourseWithSyllabus(courseId)).enqueue(callback)
    }

    fun getCoursesByEnrollmentType(adapter: RestBuilder, callback: StatusCallback<List<Course>>, params: RestParams, type: String) {
        callback.addCall(adapter.build(CoursesInterface::class.java, params).getCoursesByEnrollmentType(type)).enqueue(callback)
    }

    fun getCourseWithGrade(courseId: Long, adapter: RestBuilder, callback: StatusCallback<Course>, params: RestParams) {
        callback.addCall(adapter.build(CoursesInterface::class.java, params).getCourseWithGrade(courseId)).enqueue(callback)
    }

    fun getCourseStudent(courseId: Long, studentId: Long, adapter: RestBuilder, callback: StatusCallback<User>, params: RestParams) {
        callback.addCall(adapter.build(CoursesInterface::class.java, params).getCourseStudent(courseId, studentId)).enqueue(callback)
    }

    fun addCourseToFavorites(courseId: Long, adapter: RestBuilder, callback: StatusCallback<Favorite>, params: RestParams) {
        callback.addCall(adapter.build(CoursesInterface::class.java, params).addCourseToFavorites(courseId)).enqueue(callback)
    }

    fun removeCourseFromFavorites(courseId: Long, adapter: RestBuilder, callback: StatusCallback<Favorite>, params: RestParams) {
        callback.addCall(adapter.build(CoursesInterface::class.java, params).removeCourseFromFavorites(courseId)).enqueue(callback)
    }

    /**
     * Updates a course
     *
     * @param courseId The id for the course
     * @param params   A map of the fields to change and the values they will change to
     */
    fun updateCourse(courseId: Long, queryParams: Map<String, String>, adapter: RestBuilder, callback: StatusCallback<Course>, params: RestParams) {
        callback.addCall(adapter.build(CoursesInterface::class.java, params).updateCourse(courseId, queryParams)).enqueue(callback)
    }

    fun getFirstPageGroups(courseId: Long, adapter: RestBuilder, callback: StatusCallback<List<Group>>, params: RestParams) {
        callback.addCall(adapter.build(CoursesInterface::class.java, params).getFirstPageGroups(courseId)).enqueue(callback)
    }

    fun getNextPageGroups(nextUrl: String, adapter: RestBuilder, callback: StatusCallback<List<Group>>, params: RestParams) {
        callback.addCall(adapter.build(CoursesInterface::class.java, params).getNextPageGroups(nextUrl)).enqueue(callback)
    }

    fun getCoursePermissions(courseId: Long, requestedPermissions: List<String>, adapter: RestBuilder, callback: StatusCallback<CanvasContextPermission>, params: RestParams) {
        callback.addCall(adapter.build(CoursesInterface::class.java, params).getCoursePermissions(courseId, requestedPermissions)).enqueue(callback)
    }

    fun getUserEnrollmentsForGradingPeriod(courseId: Long, userId: Long, gradingPeriodId: Long, adapter: RestBuilder, params: RestParams, callback: StatusCallback<List<Enrollment>>) {
        callback.addCall(adapter.build(CoursesInterface::class.java, params).getUserEnrollmentsForGradingPeriod(courseId, userId, gradingPeriodId)).enqueue(callback)
    }

    fun getRubricSettings(courseId: Long, rubricId: Long, adapter: RestBuilder, callback: StatusCallback<RubricSettings>, params: RestParams) {
        callback.addCall(adapter.build(CoursesInterface::class.java, params).getRubricSettings(courseId, rubricId)).enqueue(callback)
    }
}
