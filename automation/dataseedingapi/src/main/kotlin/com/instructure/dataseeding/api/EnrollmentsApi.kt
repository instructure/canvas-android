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

import com.instructure.dataseeding.model.CreateEnrollmentApiRequestModel
import com.instructure.dataseeding.model.EnrollmentApiModel
import com.instructure.dataseeding.model.EnrollmentApiRequestModel
import com.instructure.dataseeding.model.EnrollmentTypes.DESIGNER_ENROLLMENT
import com.instructure.dataseeding.model.EnrollmentTypes.OBSERVER_ENROLLMENT
import com.instructure.dataseeding.model.EnrollmentTypes.STUDENT_ENROLLMENT
import com.instructure.dataseeding.model.EnrollmentTypes.TA_ENROLLMENT
import com.instructure.dataseeding.model.EnrollmentTypes.TEACHER_ENROLLMENT
import com.instructure.dataseeding.util.CanvasNetworkAdapter
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

object EnrollmentsApi {
    interface EnrollmentsService {

        @POST("courses/{courseId}/enrollments")
        fun enrollUser(@Path("courseId") courseId: Long, @Body enrollmentApiModel: CreateEnrollmentApiRequestModel): Call<EnrollmentApiModel>

        @POST("sections/{sectionId}/enrollments")
        fun enrollUserInSection(@Path("sectionId") sectionId: Long, @Body enrollmentApiModel: CreateEnrollmentApiRequestModel): Call<EnrollmentApiModel>
    }

    private val enrollmentsService: EnrollmentsService by lazy {
        CanvasNetworkAdapter.adminRetrofit.create(EnrollmentsService::class.java)
    }

    // region Course Enrollments

    fun enrollUserAsTeacher(courseId: Long, userId: Long): EnrollmentApiModel
            = enrollUser(courseId, userId, TEACHER_ENROLLMENT)

    fun enrollUserAsStudent(courseId: Long, userId: Long): EnrollmentApiModel
            = enrollUser(courseId, userId, STUDENT_ENROLLMENT)

    fun enrollUserAsTA(courseId: Long, userId: Long): EnrollmentApiModel
            = enrollUser(courseId, userId, TA_ENROLLMENT)

    fun enrollUserAsObserver(courseId: Long, userId: Long, associatedUserId: Long): EnrollmentApiModel
            = enrollUser(courseId, userId, OBSERVER_ENROLLMENT, associatedUserId = associatedUserId.takeIf { it > 0 })

    fun enrollUserAsDesigner(courseId: Long, userId: Long): EnrollmentApiModel
            = enrollUser(courseId, userId, DESIGNER_ENROLLMENT)

    fun enrollUser(
            courseId: Long,
            userId: Long,
            enrollmentType: String,
            enrollmentService: EnrollmentsService = enrollmentsService,
            associatedUserId: Long? = null
    ): EnrollmentApiModel {
        val enrollment = EnrollmentApiRequestModel(userId, enrollmentType, enrollmentType, associatedUserId = associatedUserId)
        return enrollmentService
                .enrollUser(courseId, CreateEnrollmentApiRequestModel(enrollment))
                .execute()
                .body()!!
    }

    // endregion

    // region Section Enrollments

    fun enrollUserInSectionAsTeacher(sectionId: Long, userId: Long): EnrollmentApiModel
            = enrollUserInSection(sectionId, userId, TEACHER_ENROLLMENT)

    fun enrollUserInSectionAsStudent(sectionId: Long, userId: Long): EnrollmentApiModel
            = enrollUserInSection(sectionId, userId, STUDENT_ENROLLMENT)

    fun enrollUserInSectionAsTA(sectionId: Long, userId: Long): EnrollmentApiModel
            = enrollUserInSection(sectionId, userId, TA_ENROLLMENT)

    fun enrollUserInSectionAsObserver(sectionId: Long, userId: Long): EnrollmentApiModel
            = enrollUserInSection(sectionId, userId, OBSERVER_ENROLLMENT)

    fun enrollUserInSectionAsDesigner(sectionId: Long, userId: Long): EnrollmentApiModel
            = enrollUserInSection(sectionId, userId, DESIGNER_ENROLLMENT)

    fun enrollUserInSection(sectionId: Long, userId: Long, enrollmentType: String): EnrollmentApiModel {
        val enrollment = EnrollmentApiRequestModel(userId, enrollmentType, enrollmentType)
        return enrollmentsService
                .enrollUserInSection(sectionId, CreateEnrollmentApiRequestModel(enrollment))
                .execute()
                .body()!!
    }

    // endregion

}
