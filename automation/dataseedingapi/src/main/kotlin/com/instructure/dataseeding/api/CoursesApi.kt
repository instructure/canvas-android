//
// Copyright (C) 2018-present Instructure, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//


package com.instructure.dataseeding.api

import com.instructure.dataseeding.model.CourseApiModel
import com.instructure.dataseeding.model.CourseSettings
import com.instructure.dataseeding.model.CreateCourse
import com.instructure.dataseeding.model.CreateCourseWrapper
import com.instructure.dataseeding.model.FavoriteApiModel
import com.instructure.dataseeding.model.UpdateCourse
import com.instructure.dataseeding.model.UpdateCourseWrapper
import com.instructure.dataseeding.util.CanvasNetworkAdapter
import com.instructure.dataseeding.util.Randomizer
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.QueryMap

object CoursesApi {
    interface CoursesService {

        @POST("accounts/self/courses")
        fun createCourse(@Body createCourseApiModel: CreateCourseWrapper): Call<CourseApiModel>

        @POST("accounts/{account_id}/courses")
        fun createCourseInSubAccount(@Path("account_id") accountId: Long, @Body createCourseApiModel: CreateCourseWrapper): Call<CourseApiModel>

        @POST("users/self/favorites/courses/{courseId}")
        fun addCourseToFavorites(@Path("courseId") courseId: Long): Call<FavoriteApiModel>

        @DELETE("courses/{courseId}?event=conclude")
        fun concludeCourse(@Path("courseId") courseId: Long): Call<FavoriteApiModel>

        @PUT("courses/{course_id}/settings")
        fun updateCourseSettings(@Path("course_id") courseId: Long, @QueryMap params: Map<String, Boolean>): Call<CourseSettings>

        @PUT("courses/{course_id}")
        fun updateCourse(@Path("course_id") courseId: Long, @Body courseApiModel: UpdateCourseWrapper): Call<CourseApiModel>
    }

    private val adminCoursesService: CoursesService by lazy {
        CanvasNetworkAdapter.adminRetrofit.create(CoursesService::class.java)
    }

    private fun coursesService(token: String): CoursesService =
        CanvasNetworkAdapter.retrofitWithToken(token).create(CoursesService::class.java)

    fun createCourseInSubAccount(
        enrollmentTermId: Long? = null,
        publish: Boolean = true,
        coursesService: CoursesService = adminCoursesService,
        homeroomCourse: Boolean = false,
        accountId: Long? = null,
        syllabusBody: String? = null
    ): CourseApiModel {
        val randomCourseName = Randomizer.randomCourseName()
        val course = CreateCourseWrapper(
            offer = publish,
            course = CreateCourse(
                name = randomCourseName,
                courseCode = randomCourseName.substring(0, 2),
                enrollmentTermId = enrollmentTermId,
                homeroomCourse = homeroomCourse,
                accountId = accountId,
                syllabusBody = syllabusBody
            )
        )
        return coursesService
            .createCourseInSubAccount(accountId!!, course)
            .execute()
            .body()!!

    }

    fun createCourse(
        enrollmentTermId: Long? = null,
        publish: Boolean = true,
        coursesService: CoursesService = adminCoursesService,
        syllabusBody: String? = null
    ): CourseApiModel {
        val randomCourseName = Randomizer.randomCourseName()
        val course = CreateCourseWrapper(
            offer = publish,
            course = CreateCourse(
                name = randomCourseName,
                courseCode = randomCourseName.substring(0, 2),
                enrollmentTermId = enrollmentTermId,
                syllabusBody = syllabusBody
            )
        )
        return coursesService
            .createCourse(course)
            .execute()
            .body()!!
    }

    fun concludeCourse(courseId: Long) {
        adminCoursesService
            .concludeCourse(courseId)
            .execute()
    }

    fun addCourseToFavorites(courseId: Long, token: String): FavoriteApiModel
            = coursesService(token)
            .addCourseToFavorites(courseId)
            .execute()
            .body()!!

    fun updateCourseSettings(courseId: Long, params: Map<String, Boolean>
    ): CourseSettings {
        return adminCoursesService
            .updateCourseSettings(courseId, params)
            .execute()
            .body()!!
    }

    fun updateCourse(courseId: Long, updateCourse: UpdateCourse) : CourseApiModel {
        return adminCoursesService
            .updateCourse(courseId, UpdateCourseWrapper(updateCourse))
            .execute()
            .body()!!
    }
}
