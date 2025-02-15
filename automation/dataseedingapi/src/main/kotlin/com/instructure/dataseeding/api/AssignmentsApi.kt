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

import com.instructure.dataseeding.model.AssignmentApiModel
import com.instructure.dataseeding.model.AssignmentOverrideApiModel
import com.instructure.dataseeding.model.CreateAssignmentOverrideForStudents
import com.instructure.dataseeding.model.CreateAssignmentOverrideForStudentsWrapper
import com.instructure.dataseeding.model.CreateAssignmentWrapper
import com.instructure.dataseeding.model.GradingType
import com.instructure.dataseeding.model.SubmissionType
import com.instructure.dataseeding.util.CanvasNetworkAdapter
import com.instructure.dataseeding.util.Randomizer
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

object AssignmentsApi {

    interface AssignmentsService {
        @POST("courses/{courseId}/assignments")
        fun createAssignment(@Path("courseId") courseId: Long, @Body createAssignment: CreateAssignmentWrapper): Call<AssignmentApiModel>

        @POST("courses/{courseId}/assignments/{assignmentId}/overrides")
        fun createAssignmentOverride(@Path("courseId") courseId: Long, @Path("assignmentId") assignmentId: Long, @Body createAssignmentOverride: CreateAssignmentOverrideForStudentsWrapper): Call<AssignmentOverrideApiModel>
    }

    private fun assignmentsService(token: String): AssignmentsService
            = CanvasNetworkAdapter.retrofitWithToken(token).create(AssignmentsService::class.java)

    data class CreateAssignmentRequest(
        val courseId: Long,
        val withDescription: Boolean = false,
        val lockAt: String = "",
        val unlockAt: String = "",
        val dueAt: String = "",
        val submissionTypes: List<SubmissionType>,
        val gradingType: GradingType = GradingType.POINTS,
        val allowedExtensions: List<String>? = null,
        val teacherToken: String,
        val assignmentName: String? = null,
        val groupCategoryId: Long? = null,
        val pointsPossible: Double? = null,
        val importantDate: Boolean? = null,
        val assignmentGroupId: Long? = null)

    fun createAssignment(request: CreateAssignmentRequest): AssignmentApiModel {
        return createAssignment(
                request.courseId,
                request.teacherToken,
                request.withDescription,
                request.lockAt,
                request.unlockAt,
                request.dueAt,
                request.submissionTypes,
                request.gradingType,
                request.allowedExtensions,
                request.assignmentName,
                request.groupCategoryId,
                request.pointsPossible,
                request.importantDate,
                request.assignmentGroupId
        )
    }

    fun createAssignment(
        courseId: Long,
        teacherToken: String,
        withDescription: Boolean = false,
        lockAt: String = "",
        unlockAt: String = "",
        dueAt: String = "",
        submissionTypes: List<SubmissionType> = emptyList(),
        gradingType: GradingType = GradingType.POINTS,
        allowedExtensions: List<String>? = null,
        assignmentName: String? = null,
        groupCategoryId: Long? = null,
        pointsPossible: Double? = null,
        importantDate: Boolean? = null,
        assignmentGroupId: Long? = null): AssignmentApiModel {
        val assignment = CreateAssignmentWrapper(Randomizer.randomAssignment(
                assignmentName,
                withDescription,
                lockAt,
                unlockAt,
                dueAt,
                submissionTypes,
                gradingType,
                groupCategoryId,
                pointsPossible,
                allowedExtensions,
                importantDate,
                assignmentGroupId))

        return assignmentsService(teacherToken).createAssignment(courseId, assignment).execute().body()!!
    }

    data class CreateAssignmentOverrideRequest(
            val courseId: Long,
            val assignmentId: Long,
            val token: String,
            val studentIds: List<Long>? = null,
            val groupId: Long? = null,
            val courseSectionId: Long? = null,
            val dueAt: String? = "",
            val unlockAt: String? = "",
            val lockAt: String? = ""
    )

    fun createAssignmentOverride(request: CreateAssignmentOverrideRequest) : AssignmentOverrideApiModel {
        return createAssignmentOverride(
                request.courseId,
                request.assignmentId,
                request.token,
                request.studentIds,
                request.groupId,
                request.courseSectionId,
                request.dueAt,
                request.unlockAt,
                request.lockAt
        )
    }

    fun createAssignmentOverride(
            courseId: Long,
            assignmentId: Long,
            token: String,
            studentIds: List<Long>?,
            groupId: Long?,
            courseSectionId: Long?,
            dueAt: String?,
            unlockAt: String?,
            lockAt: String?): AssignmentOverrideApiModel {
        val assignmentOverride = CreateAssignmentOverrideForStudentsWrapper(
                CreateAssignmentOverrideForStudents(
                        Randomizer.randomAssignmentOverrideTitle(),
                        studentIds,
                        groupId,
                        courseSectionId,
                        dueAt,
                        unlockAt,
                        lockAt))
        return assignmentsService(token).createAssignmentOverride(courseId, assignmentId, assignmentOverride).execute().body()!!
    }

    /** Seed some assignments, given a CreateAssignmentRequest and numAssignments.
     *     Returns a list of AssignmentApiModel objects.
     */
    fun seedAssignments(request: CreateAssignmentRequest, numAssignments: Int) : List<AssignmentApiModel> {
        val seededAssignments = mutableListOf<AssignmentApiModel>()

        seededAssignments.addAll(
                (0 until numAssignments).map {
                    createAssignment(request)
                })

        return seededAssignments
    }


}
