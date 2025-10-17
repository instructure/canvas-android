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
package com.instructure.teacher.ui.interaction

import com.instructure.canvas.espresso.annotations.Stub
import com.instructure.canvas.espresso.mockcanvas.MockCanvas
import com.instructure.canvas.espresso.mockcanvas.addAssignment
import com.instructure.canvas.espresso.mockcanvas.addCoursePermissions
import com.instructure.canvas.espresso.mockcanvas.addSubmissionsForAssignment
import com.instructure.canvas.espresso.mockcanvas.fakes.FakeDifferentiationTagsManager
import com.instructure.canvas.espresso.mockcanvas.init
import com.instructure.canvasapi2.managers.graphql.DifferentiationTagsManager
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Attachment
import com.instructure.canvasapi2.models.CanvasContextPermission
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.models.SubmissionComment
import com.instructure.espresso.randomString
import com.instructure.pandautils.di.DifferentiationTagsModule
import com.instructure.teacher.ui.utils.TeacherComposeTest
import com.instructure.teacher.ui.utils.extensions.tokenLogin
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.Test

@HiltAndroidTest
@UninstallModules(DifferentiationTagsModule::class)
class SpeedGraderCommentsInteractionTest : TeacherComposeTest() {

    @BindValue
    @JvmField
    val differentiationTagsManager: DifferentiationTagsManager = FakeDifferentiationTagsManager()

    // Just good enough to mock the *representation* of a file, not to mock the file itself.
    val attachment = Attachment(
            id = 131313,
            contentType = "text/plain",
            filename = "sampleFile",
            displayName = "sampleFile",
            url = "http://fake.blah/somePath" // Code/Test will crash w/o a non-null url
    )

    @Stub
    @Test
    override fun displaysPageObjects() {
        goToSpeedGraderCommentsPage(
                submissionTypeList = listOf(Assignment.SubmissionType.ONLINE_TEXT_ENTRY)
        )

        speedGraderCommentsPage.assertPageObjects()
    }

    @Stub
    @Test
    fun displaysAuthorName() {
        val submissionList = goToSpeedGraderCommentsPage(
                submissionTypeList = listOf(Assignment.SubmissionType.ONLINE_TEXT_ENTRY),
                withComment = true
        )

        val authorName = submissionList?.get(0)!!.submissionComments[0].authorName!!
        speedGraderCommentsPage.assertDisplaysAuthorName(authorName)
    }

    @Stub
    @Test
    fun displaysCommentText() {
        val submissionList = goToSpeedGraderCommentsPage(
                submissionTypeList = listOf(Assignment.SubmissionType.ONLINE_TEXT_ENTRY),
                withComment = true
        )

        val commentText = submissionList?.get(0)!!.submissionComments[0].comment!!
        speedGraderCommentsPage.assertDisplaysCommentText(commentText)
    }

    @Stub
    @Test
    fun displaysCommentAttachment() {
        val submissionList = goToSpeedGraderCommentsPage(
                submissionTypeList = listOf(Assignment.SubmissionType.ONLINE_TEXT_ENTRY),
                withComment = true,
                attachment = attachment
        )

        val attachment = submissionList?.get(0)!!.submissionComments[0].attachments.get(0)
        speedGraderCommentsPage.assertDisplaysCommentAttachment(attachment)
    }

    @Stub
    @Test
    fun displaysSubmissionHistory() {
        goToSpeedGraderCommentsPage(
                submissionTypeList = listOf(Assignment.SubmissionType.ONLINE_TEXT_ENTRY)
        )

        speedGraderCommentsPage.assertDisplaysSubmission()
    }

    @Stub
    @Test
    fun displaysSubmissionFile() {
        val submissionList = goToSpeedGraderCommentsPage(
                submissionTypeList = listOf(Assignment.SubmissionType.ONLINE_UPLOAD),
                attachment = attachment
        )

        val fileAttachments = submissionList?.get(0)!!.attachments[0]
        speedGraderCommentsPage.assertDisplaysSubmissionFile(fileAttachments)
    }

    @Stub
    @Test
    fun addsNewTextComment() {
        goToSpeedGraderCommentsPage(
                submissionTypeList = listOf(Assignment.SubmissionType.ONLINE_TEXT_ENTRY)
        )

        val newComment = randomString(32)
        speedGraderCommentsPage.addComment(newComment)
        speedGraderCommentsPage.assertDisplaysCommentText(newComment)
    }

    @Stub
    @Test
    fun showsNoCommentsMessage() {
        goToSpeedGraderCommentsPage(
                submissionCount = 0,
                submissionTypeList = listOf(Assignment.SubmissionType.ON_PAPER)
        )

        speedGraderCommentsPage.assertDisplaysEmptyState()
    }

    /**
     * Common setup routine
     *
     * [submissionCount] is the number of submissions for the created assignment.  Typically 0 or 1.
     * [submissionTypeList] is the submission type for the assignment.
     * [withComment] if true, include a (student) comment with the submission.
     * [attachment] if non-null, is either a comment attachment (if withComment is true) or a submission
     * attachment (if withComment is false).
     *
     */
    private fun goToSpeedGraderCommentsPage(
        submissionCount: Int = 1,
        submissionTypeList: List<Assignment.SubmissionType> = listOf(Assignment.SubmissionType.ONLINE_TEXT_ENTRY),
        withComment: Boolean = false,
        attachment: Attachment? = null): MutableList<Submission>? {

        val data = MockCanvas.init(teacherCount = 1, studentCount = 1, courseCount = 1, favoriteCourseCount = 1)
        val teacher = data.teachers[0]
        val course = data.courses.values.first()
        val student = data.students[0]

        data.addCoursePermissions(
                course.id,
                CanvasContextPermission() // Just need to have some sort of permissions object registered
        )

        val assignment = data.addAssignment(
                courseId = course.id,
                submissionTypeList = submissionTypeList
        )

        var submissionComment : SubmissionComment? = null
        if(withComment) {
            submissionComment = SubmissionComment(
                    id = data.newItemId(),
                    authorId = student.id,
                    // Allows Espresso to distinguish between this and the full name, which is elsewhere on the page
                    authorName = student.shortName,
                    authorPronouns = student.pronouns,
                    attempt = 1L,
                    comment = "a comment",
                    attachments = if(attachment == null) arrayListOf<Attachment>() else arrayListOf(attachment)
            )
        }

        var submissionList = mutableListOf<Submission>()
        repeat(submissionCount) {
            val submissionTypesRaw = submissionTypeList.map { it.apiString }
            submissionList = data.addSubmissionsForAssignment(
                    assignmentId = assignment.id,
                    userId = student.id,
                    types = submissionTypesRaw,
                    comment = submissionComment,
                    attachment = if (withComment) null else attachment
            )
        }

        val token = data.tokenFor(teacher)!!
        tokenLogin(data.domain, token, teacher)

        dashboardPage.openCourse(course)
        courseBrowserPage.openAssignmentsTab()
        assignmentListPage.clickAssignment(assignment)
        assignmentDetailsPage.clickAllSubmissions()
        assignmentSubmissionListPage.clickSubmission(student)
        speedGraderPage.selectCommentsTab()
        speedGraderPage.swipeUpCommentsTab()

        return submissionList
    }
}
