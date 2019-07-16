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
 */
package com.instructure.teacher.ui

import com.instructure.dataseeding.api.SubmissionsApi
import com.instructure.dataseeding.model.SubmissionApiModel
import com.instructure.dataseeding.model.SubmissionListApiModel
import com.instructure.espresso.randomString
import com.instructure.dataseeding.model.FileType.TEXT
import com.instructure.dataseeding.model.SubmissionType.*
import com.instructure.teacher.ui.utils.*
import com.instructure.espresso.ditto.Ditto
import com.instructure.espresso.ditto.DittoMode
import org.junit.Test

class SpeedGraderCommentsPageTest : TeacherTest() {

    // MBL-11593: We set DittoMode to LIVE for all of these tests because they potentially
    //            involve the downloading of PDF files, which can mess up the Ditto/OkReplay
    //            yaml logic.

    @Test
    @Ditto(mode=DittoMode.LIVE)
    override fun displaysPageObjects() {
        goToSpeedGraderCommentsPage(
                submissions = listOf(
                        SubmissionsApi.SubmissionSeedInfo(
                                amount = 1,
                                submissionType = ONLINE_TEXT_ENTRY
                        )
                )
        )

        speedGraderCommentsPage.assertPageObjects()
    }

    @Test
    @Ditto(mode=DittoMode.LIVE)
    fun displaysAuthorName() {
        val submissions = goToSpeedGraderCommentsPage(
                submissions = listOf(
                        SubmissionsApi.SubmissionSeedInfo(
                                amount = 1,
                                submissionType = ONLINE_TEXT_ENTRY
                        )
                ),
                submissionComments = listOf(SubmissionsApi.CommentSeedInfo())
        )

        val authorName = submissions.submissionList[0].submissionComments[0].authorName
        speedGraderCommentsPage.assertDisplaysAuthorName(authorName)
    }

    @Test
    @Ditto(mode=DittoMode.LIVE)
    fun displaysCommentText() {
        val submissions = goToSpeedGraderCommentsPage(
                submissions = listOf(
                        SubmissionsApi.SubmissionSeedInfo(
                                amount = 1,
                                submissionType = ONLINE_TEXT_ENTRY
                        )
                ),
                submissionComments = listOf(SubmissionsApi.CommentSeedInfo())
        )

        val commentText = submissions.submissionList[0].submissionComments[0].comment
        speedGraderCommentsPage.assertDisplaysCommentText(commentText)
    }

    @Test
    @Ditto(mode=DittoMode.LIVE)
    fun displaysCommentAttachment() {
        val submissions = goToSpeedGraderCommentsPage(
                submissions = listOf(
                        SubmissionsApi.SubmissionSeedInfo(
                                amount = 1,
                                submissionType = ONLINE_TEXT_ENTRY
                        )
                ),
                submissionComments = listOf(SubmissionsApi.CommentSeedInfo(fileType = TEXT))
        )

        val attachment = submissions.submissionList[0].submissionComments[0].attachments?.get(0)
        speedGraderCommentsPage.assertDisplaysCommentAttachment(attachment!!)
    }

    @Test
    @Ditto(mode=DittoMode.LIVE)
    fun displaysSubmissionHistory() {
        goToSpeedGraderCommentsPage(
                submissions = listOf(
                        SubmissionsApi.SubmissionSeedInfo(
                                amount = 1,
                                submissionType = ONLINE_TEXT_ENTRY
                        )
                )
        )

        speedGraderCommentsPage.assertDisplaysSubmission()
    }
    @Test
    @Ditto(mode=DittoMode.LIVE)
    fun displaysSubmissionFile() {
        val submissions = goToSpeedGraderCommentsPage(
                submissions = listOf(
                        SubmissionsApi.SubmissionSeedInfo(
                                amount = 1,
                                submissionType = ONLINE_UPLOAD,
                                fileType = TEXT
                        )
                )
        )

        val fileAttachments = submissions.submissionList[0].submissionAttachments?.get(0)
        speedGraderCommentsPage.assertDisplaysSubmissionFile(fileAttachments!!)
    }

    @Test
    @Ditto(mode=DittoMode.LIVE, sequential = true)
    fun addsNewTextComment() {
        goToSpeedGraderCommentsPage(
                submissions = listOf(
                        SubmissionsApi.SubmissionSeedInfo(
                                amount = 1,
                                submissionType = ONLINE_TEXT_ENTRY
                        )
                )
        )

        val newComment = mockableString("new comment") { randomString(32) }
        speedGraderCommentsPage.addComment(newComment)
        speedGraderCommentsPage.assertDisplaysCommentText(newComment)
    }

    // MBL-12837: Commenting out for now due to Canvas bug.
//    @Test
//    @Ditto(mode=DittoMode.LIVE)
//    fun showsNoCommentsMessage() {
//        goToSpeedGraderCommentsPage(
//                submissions = listOf(
//                        SubmissionsApi.SubmissionSeedInfo(
//                                amount = 0,
//                                submissionType = ON_PAPER
//                        )
//                )
//        )
//
//        speedGraderCommentsPage.assertDisplaysEmptyState()
//    }

    private fun goToSpeedGraderCommentsPage(
            assignments: Int = 1,
            withDescription: Boolean = false,
            lockAt: String = "",
            unlockAt: String = "",
            submissions: List<SubmissionsApi.SubmissionSeedInfo> = emptyList(),
            submissionComments: List<SubmissionsApi.CommentSeedInfo> = emptyList()): SubmissionListApiModel {

        val data = seedData(teachers = 1, favoriteCourses = 1, students = 1)
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]
        val student = data.studentsList[0]
        val assignments = seedAssignments(
                assignments = assignments,
                courseId = course.id,
                withDescription = withDescription,
                lockAt = lockAt,
                unlockAt = unlockAt,
                submissionTypes = submissions.map { it.submissionType },
                teacherToken = teacher.token)

        val submissionList = seedAssignmentSubmission(
                submissionSeeds = submissions,
                assignmentId = assignments.assignmentList[0].id,
                courseId = course.id,
                studentToken = if (data.studentsList.isEmpty()) "" else data.studentsList[0].token,
                commentSeeds = submissionComments
        )

        tokenLogin(teacher)

        coursesListPage.openCourse(course)
        courseBrowserPage.openAssignmentsTab()
        assignmentListPage.clickAssignment(assignments.assignmentList[0])
        assignmentDetailsPage.openSubmissionsPage()
        assignmentSubmissionListPage.clickSubmission(student)
        speedGraderPage.selectCommentsTab()
        return submissionList
    }
}
