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

import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import com.instructure.canvas.espresso.mockCanvas.addAssignment
import com.instructure.canvas.espresso.mockCanvas.addCoursePermissions
import com.instructure.canvas.espresso.mockCanvas.addSubmissionForAssignment
import com.instructure.canvas.espresso.mockCanvas.init
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Attachment
import com.instructure.canvasapi2.models.CanvasContextPermission
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.models.SubmissionComment
import com.instructure.espresso.randomString
import com.instructure.teacher.ui.utils.TeacherTest
import com.instructure.teacher.ui.utils.tokenLogin
import org.junit.Test

class SpeedGraderCommentsPageTest : TeacherTest() {

    // Just good enough to mock the *representation* of a file, not to mock the file itself.
    val attachment = Attachment(
            id = 131313,
            contentType = "text/plain",
            filename = "sampleFile",
            displayName = "sampleFile",
            url = "http://fake.blah/somePath" // Code/Test will crash w/o a non-null url
    )

    @Test
    override fun displaysPageObjects() {
        goToSpeedGraderCommentsPage(
                submissionType = Assignment.SubmissionType.ONLINE_TEXT_ENTRY
        )

        speedGraderCommentsPage.assertPageObjects()
    }

    @Test
    fun displaysAuthorName() {
        val submission = goToSpeedGraderCommentsPage(
                submissionType = Assignment.SubmissionType.ONLINE_TEXT_ENTRY,
                withComment = true
        )

        val authorName = submission!!.submissionComments[0].authorName!!
        speedGraderCommentsPage.assertDisplaysAuthorName(authorName)
    }

    @Test
    fun displaysCommentText() {
        val submission = goToSpeedGraderCommentsPage(
                submissionType = Assignment.SubmissionType.ONLINE_TEXT_ENTRY,
                withComment = true
        )

        val commentText = submission!!.submissionComments[0].comment!!
        speedGraderCommentsPage.assertDisplaysCommentText(commentText)
    }

    @Test
    fun displaysCommentAttachment() {
        val submission = goToSpeedGraderCommentsPage(
                submissionType = Assignment.SubmissionType.ONLINE_TEXT_ENTRY,
                withComment = true,
                attachment = attachment
        )

        val attachment = submission!!.submissionComments[0].attachments.get(0)
        speedGraderCommentsPage.assertDisplaysCommentAttachment(attachment)
    }

    @Test
    fun displaysSubmissionHistory() {
        goToSpeedGraderCommentsPage(
                submissionType = Assignment.SubmissionType.ONLINE_TEXT_ENTRY
        )

        speedGraderCommentsPage.assertDisplaysSubmission()
    }

    @Test
    fun displaysSubmissionFile() {
        val submission = goToSpeedGraderCommentsPage(
                submissionType = Assignment.SubmissionType.ONLINE_UPLOAD,
                attachment = attachment
        )

        val fileAttachments = submission!!.attachments.get(0)
        speedGraderCommentsPage.assertDisplaysSubmissionFile(fileAttachments)
    }

    @Test
    fun addsNewTextComment() {
        goToSpeedGraderCommentsPage(
                submissionType = Assignment.SubmissionType.ONLINE_TEXT_ENTRY
        )

        val newComment = randomString(32)
        speedGraderCommentsPage.addComment(newComment)
        speedGraderCommentsPage.assertDisplaysCommentText(newComment)
    }

    @Test
    fun showsNoCommentsMessage() {
        goToSpeedGraderCommentsPage(
                submissionCount = 0,
                submissionType = Assignment.SubmissionType.ON_PAPER
        )

        speedGraderCommentsPage.assertDisplaysEmptyState()
    }

    /**
     * Common setup routine
     *
     * [submissionCount] is the number of submissions for the created assignment.  Typically 0 or 1.
     * [submissionType] is the submission type for the assignment.
     * [withComment] if true, include a (student) comment with the submission.
     * [attachment] if non-null, is either a comment attachment (if withComment is true) or a submission
     * attachment (if withComment is false).
     *
     */
    private fun goToSpeedGraderCommentsPage(
            submissionCount: Int = 1,
            submissionType: Assignment.SubmissionType = Assignment.SubmissionType.ONLINE_TEXT_ENTRY,
            withComment: Boolean = false,
            attachment: Attachment? = null): Submission? {

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
                submissionType = submissionType
        )

        var submissionComment : SubmissionComment? = null
        if(withComment) {
            submissionComment = SubmissionComment(
                    id = data.newItemId(),
                    authorId = student.id,
                    // Allows Espresso to distinguish between this and the full name, which is elsewhere on the page
                    authorName = student.shortName,
                    authorPronouns = student.pronouns,
                    comment = "a comment",
                    attachments = if(attachment == null) arrayListOf<Attachment>() else arrayListOf(attachment)
            )
        }

        var submission: Submission? = null
        repeat(submissionCount) {
            submission = data.addSubmissionForAssignment(
                    assignmentId = assignment.id,
                    userId = student.id,
                    type = submissionType.apiString,
                    comment = submissionComment,
                    attachment = if (withComment) null else attachment
            )
        }

        val token = data.tokenFor(teacher)!!
        tokenLogin(data.domain, token, teacher)

        coursesListPage.openCourse(course)
        courseBrowserPage.openAssignmentsTab()
        assignmentListPage.clickAssignment(assignment)
        assignmentDetailsPage.openSubmissionsPage()
        assignmentSubmissionListPage.clickSubmission(student)
        speedGraderPage.selectCommentsTab()

        return submission
    }
}
