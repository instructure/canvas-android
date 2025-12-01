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
import com.instructure.dataseeding.model.AttachmentApiModel
import com.instructure.dataseeding.model.CreateSubmissionCommentWrapper
import com.instructure.dataseeding.model.FileType
import com.instructure.dataseeding.model.GradeSubmission
import com.instructure.dataseeding.model.GradeSubmissionWrapper
import com.instructure.dataseeding.model.SubmissionApiModel
import com.instructure.dataseeding.model.SubmissionType
import com.instructure.dataseeding.model.SubmitCourseAssignmentSubmissionWrapper
import com.instructure.dataseeding.util.CanvasNetworkAdapter
import com.instructure.dataseeding.util.Randomizer
import com.instructure.dataseeding.util.RetryBackoff
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

object SubmissionsApi {
    interface SubmissionsService {

        @POST("courses/{courseId}/assignments/{assignmentId}/submissions")
        fun submitCourseAssignment(
                @Path("courseId") courseId: Long,
                @Path("assignmentId") assignmentId: Long,
                @Body submitCourseAssignmentSubmission: SubmitCourseAssignmentSubmissionWrapper): Call<SubmissionApiModel>

        @PUT("courses/{courseId}/assignments/{assignmentId}/submissions/self")
        fun commentOnSubmission(
                @Path("courseId") courseId: Long,
                @Path("assignmentId") assignmentId: Long,
                @Body createSubmissionComment: CreateSubmissionCommentWrapper
        ): Call<AssignmentApiModel>

        @GET("courses/{courseId}/assignments/{assignmentId}/submissions/{studentId}")
        fun getSubmission(
                @Path("courseId") courseId: Long,
                @Path("assignmentId") assignmentId: Long,
                @Path("studentId") studentId: Long): Call<SubmissionApiModel>

        @PUT("courses/{courseId}/assignments/{assignmentId}/submissions/{studentId}")
        fun gradeSubmission(
                @Path("courseId") courseId: Long,
                @Path("assignmentId") assignmentId: Long,
                @Path("studentId") studentId: Long,
                @Body gradeSubmission: GradeSubmissionWrapper
        ): Call<SubmissionApiModel>

    }

    private fun submissionsService(token: String): SubmissionsService
            = CanvasNetworkAdapter.retrofitWithToken(token).create(SubmissionsService::class.java)

    fun submitCourseAssignment(courseId: Long,
                               studentToken: String,
                               assignmentId: Long,
                               submissionType: SubmissionType,
                               fileIds: MutableList<Long> = mutableListOf()
                               ): SubmissionApiModel {

        val submission = Randomizer.randomSubmission(submissionType, fileIds)

        return submissionsService(studentToken)
                .submitCourseAssignment(courseId, assignmentId, SubmitCourseAssignmentSubmissionWrapper(submission))
                .execute()
                .body()!!
    }

    fun commentOnSubmission(courseId: Long,
                            studentToken: String,
                            assignmentId: Long,
                            fileIds: MutableList<Long> = mutableListOf(),
                            attempt: Int = 1): AssignmentApiModel {

        val comment = Randomizer.randomSubmissionComment(fileIds, attempt)

        return submissionsService(studentToken)
                .commentOnSubmission(courseId, assignmentId, CreateSubmissionCommentWrapper(comment))
                .execute()
                .body()!!
    }

    fun getSubmission(studentToken: String,
                            courseId: Long,
                            assignmentId: Long,
                            studentId: Long): SubmissionApiModel {
        return submissionsService(studentToken)
                .getSubmission(courseId, assignmentId, studentId)
                .execute()
                .body()!!
    }

    fun gradeSubmission(teacherToken: String,
                        courseId: Long,
                        assignmentId: Long,
                        studentId: Long,
                        excused: Boolean = false,
                        postedGrade: String? = null,
                        customGradeStatusId: String? = null): SubmissionApiModel {

        return submissionsService(teacherToken)
                .gradeSubmission(courseId, assignmentId, studentId, GradeSubmissionWrapper(GradeSubmission(postedGrade, excused, customGradeStatusId)))
                .execute()
                .body()!!
    }

    //
    //
    // Seeding support
    //
    //

    /** Auxiliary data for SubmissionSeedRequest */
    data class CommentSeedInfo (
            val amount : Int = 0,
            val fileType : FileType = FileType.TEXT,
            var attachmentsList : MutableList<AttachmentApiModel> = mutableListOf()
    )

    /** Auxiliary data for SubmissionSeedRequest */
    data class SubmissionSeedInfo (
            val amount : Int,
            val submissionType : SubmissionType,
            val fileType : FileType = FileType.NONE,
            val checkForLateStatus: Boolean = false,
            var attachmentsList : MutableList<AttachmentApiModel> = mutableListOf()
    )

    /** Necessary info for seeding one or more submissions for an assignment */
    data class SubmissionSeedRequest (
            val commentSeedsList : List<SubmissionsApi.CommentSeedInfo> = listOf(),
            val submissionSeedsList : List<SubmissionsApi.SubmissionSeedInfo> = listOf(),
            val assignmentId : Long,
            val courseId : Long,
            val studentToken : String
    )

    /** Seed one or more submissions for an assignment.  Accepts a SubmissionSeedRequest, returns a
     * list of SubmissionApiModel objects.
     */
    fun seedAssignmentSubmission(courseId: Long, studentToken: String, assignmentId: Long, commentSeedsList: List<CommentSeedInfo> = listOf(), submissionSeedsList: List<SubmissionSeedInfo> = listOf()): List<SubmissionApiModel> {
        val submissionsList = mutableListOf<SubmissionApiModel>()

        for (seed in submissionSeedsList) {
            for (t in 0 until seed.amount) {

                // Submit an assignment

                // Canvas will only record submissions with unique "submitted_at" values.
                // Sleep for 1 second to ensure submissions are recorded!!!
                //
                // https://github.com/instructure/mobile_qa/blob/7f985a08161f457e9b5d60987bd6278d21e2557e/SoSeedy/lib/so_seedy/canvas_models/account_admin.rb#L357-L359
                Thread.sleep(1000)
                var submission = submitCourseAssignment(
                        submissionType = seed.submissionType,
                        courseId = courseId,
                        assignmentId = assignmentId,
                        fileIds = seed.attachmentsList.map { it.id }.toMutableList(),
                        studentToken = studentToken
                )

                if (seed.checkForLateStatus) {
                    val maxAttempts = 6
                    var attempts = 1
                    while (attempts < maxAttempts) {
                        val submissionResponse = getSubmission (
                                studentToken = studentToken,
                                courseId = courseId,
                                assignmentId = assignmentId,
                                studentId = submission.userId
                        )
                        if (submissionResponse.late) break
                        RetryBackoff.wait(attempts)
                        attempts++
                    }
                }

                // Create comments on the submitted assignment
                submission = commentSeedsList
                        .map {
                            // Create comments with any assigned upload file types
                            val assignment = commentOnSubmission(
                                    studentToken = studentToken,
                                    courseId = courseId,
                                    assignmentId = assignmentId,
                                    fileIds = it.attachmentsList.filter { it.id != -1L }.map { it.id }.toMutableList())

                            // Apparently, we only care about id and submissionComments
                            SubmissionApiModel(
                                    id = assignment.id,
                                    submissionComments = assignment.submissionComments!!,
                                    url = null,
                                    body = null,
                                    userId = 0,
                                    grade = null,
                                    attempt = assignment.attempt!!

                            )
                        }
                        .lastOrNull() ?: submission // Last one (if it exists) will have all the comments loaded up on it

                // Add submission to our collection
                submissionsList.add(submission)
            }
        }

        return submissionsList
    }

}
