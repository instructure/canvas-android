/*
 * Copyright (C) 2019 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.student.ui.interaction

import android.os.SystemClock.sleep
import androidx.test.espresso.web.webdriver.Locator
import com.instructure.canvas.espresso.Stub
import com.instructure.canvas.espresso.mockCanvas.*
import com.instructure.canvasapi2.models.*
import com.instructure.panda_annotations.FeatureCategory
import com.instructure.panda_annotations.Priority
import com.instructure.panda_annotations.TestCategory
import com.instructure.panda_annotations.TestMetaData
import com.instructure.student.ui.pages.WebViewTextCheck
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test
import java.util.*

@HiltAndroidTest
class SubmissionDetailsInteractionTest : StudentTest() {
    override fun displaysPageObjects() = Unit // Not used for interaction tests

    private lateinit var course: Course

    // Clicking the "Description" button on a rubric criterion item should show a new page with the full description
    // Also checks to see that the rubric criterion is displayed correctly, and responds to clicks correctly
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.SUBMISSIONS, TestCategory.INTERACTION, false)
    fun testRubrics_showCriterionDescription() {
        val data = getToCourse()
        val assignment = data.addAssignment(
                courseId = course.id,
                submissionType = Assignment.SubmissionType.ONLINE_TEXT_ENTRY,
                pointsPossible = 10
        )

        // Create our rubric criterion and keep a reference to it
        val rubricCriterion = RubricCriterion(
                id = data.newItemId().toString(),
                description = "Description of criterion",
                longDescription = "0, 3, 7 or 10 points",
                points = 10.0,
                ratings = mutableListOf(
                        RubricCriterionRating(id="1",points=0.0,description="No Marks", longDescription = "Really?"),
                        RubricCriterionRating(id="2",points=3.0,description="Meh", longDescription = "You're better than this!"),
                        RubricCriterionRating(id="3",points=7.0,description="Passable", longDescription = "Getting there!"),
                        RubricCriterionRating(id="4",points=10.0,description="Full Marks", longDescription = "Way to go!")
                )
        )

        // Add the rubric to the assignment
        data.addRubricToAssignment(
                assignmentId = assignment.id,
                criteria = listOf(rubricCriterion)
        )

        // Now navigate to the assignment and its rubric
        courseBrowserPage.selectAssignments()
        assignmentListPage.clickAssignment(assignment)
        assignmentDetailsPage.goToSubmissionDetails()
        submissionDetailsPage.openRubric()

        // Asserts that all ratings show up and behave as expected
        submissionDetailsPage.assertRubricCriterionDisplayed(rubricCriterion)

        // Asserts that pressing the "Description" button shows a webview displaying the long description
        submissionDetailsPage.assertRubricDescriptionDisplays(rubricCriterion)
    }

    // Should be able to add a comment on a submission
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.SUBMISSIONS, TestCategory.INTERACTION, false)
    fun testComments_addCommentToSingleAttemptSubmission() {

        val data = getToCourse()
        val assignment = data.addAssignment(
                courseId =  course.id,
                submissionType = Assignment.SubmissionType.ONLINE_URL
        )

        courseBrowserPage.selectAssignments()
        assignmentListPage.clickAssignment(assignment)
        assignmentDetailsPage.clickSubmit()
        urlSubmissionUploadPage.submitText("https://google.com")
        sleep(1000) // Allow some time for the submission to propagate
        assignmentDetailsPage.assertAssignmentSubmitted()
        assignmentDetailsPage.goToSubmissionDetails()
        submissionDetailsPage.openComments()
        submissionDetailsPage.addAndSendComment("Hey!")
        submissionDetailsPage.assertCommentDisplayed("Hey!", data.users.values.first())
    }

    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.SUBMISSIONS, TestCategory.INTERACTION)
    fun testComments_addCommentToMultipleAttemptSubmission() {

        val data = getToCourse()
        val assignment = data.addAssignment(
            courseId =  course.id,
            submissionType = Assignment.SubmissionType.ONLINE_URL,
            userSubmitted = true
        )

        courseBrowserPage.selectAssignments()
        assignmentListPage.clickAssignment(assignment)
        assignmentDetailsPage.clickSubmit()
        urlSubmissionUploadPage.submitText("https://google.com")
        sleep(1000) // Allow some time for the submission to propagate
        assignmentDetailsPage.assertAssignmentSubmitted()
        assignmentDetailsPage.goToSubmissionDetails()
        submissionDetailsPage.openComments()
        submissionDetailsPage.addAndSendComment("Hey!")
        submissionDetailsPage.assertCommentDisplayed("Hey!", data.users.values.first())
        submissionDetailsPage.selectAttempt("Attempt 1")
        submissionDetailsPage.assertSelectedAttempt("Attempt 1")
        submissionDetailsPage.assertCommentNotDisplayed("Hey!", data.users.values.first())
    }

    // Student can preview an assignment comment attachment
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.SUBMISSIONS, TestCategory.INTERACTION, false)
    fun testComments_previewAttachment() {

        val data = getToCourse()
        val user = data.users.values.first()
        val assignment = data.addAssignment(
                courseId = course.id,
                submissionType = Assignment.SubmissionType.ONLINE_TEXT_ENTRY
        )

        // Some html for an attachment
        val html = """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
        </head>

        <body>
        <h1 id="header1">Famous Quote</h1>
        <p id="p1">Who knew? -- Seinfeld</p>
        </body>
        </html>
        """.trimIndent()

        // Create an attachment
        val attachment = this.createHtmlAttachment(data, html)

        // The accompanying comment text
        val commentText = "Here's an attachment"

        // Create a submission comment containing our attachment
        val submissionComment = SubmissionComment(
                id =  data.newItemId(),
                authorId = user.id,
                authorName = user.name,
                comment = commentText,
                createdAt = Date(),
                attachments = arrayListOf(attachment),
                author = Author(id = user.id, displayName=user.shortName),
                attempt = 1
        )

        // Create/add a submission for our assignment containing our submissionComment
        val submission = data.addSubmissionForAssignment(
                assignmentId = assignment.id,
                userId = data.users.values.first().id,
                type = Assignment.SubmissionType.ONLINE_TEXT_ENTRY.apiString,
                body = "Some Text!",
                comment = submissionComment
        )

        courseBrowserPage.selectAssignments()
        assignmentListPage.clickAssignment(assignment)
        assignmentDetailsPage.goToSubmissionDetails()
        submissionDetailsPage.openComments()
        submissionDetailsPage.assertCommentDisplayed(commentText, user)
        submissionDetailsPage.assertCommentAttachmentDisplayed(attachment.filename!!, user)
        submissionDetailsPage.openCommentAttachment(attachment.filename!!, user)
        canvasWebViewPage.runTextChecks(
                WebViewTextCheck(Locator.ID, "p1", "Who knew?")
        )
    }

    @Stub
    @Test
    @TestMetaData(Priority.COMMON, FeatureCategory.SUBMISSIONS, TestCategory.INTERACTION, true)
    fun testComments_videoCommentPlayback() {
        // After recording a video comment, user should be able to view a replay
    }

    @Stub
    @Test
    @TestMetaData(Priority.COMMON, FeatureCategory.SUBMISSIONS, TestCategory.INTERACTION, true)
    fun testComments_audioCommentPlayback() {
        // After recording an audio comment, user should be able to hear an audio playback
    }


    // Creates an HTML attachment/file which can then be attached to a comment.
    private fun createHtmlAttachment(data: MockCanvas, html: String, name: String = "CommentAttachment.html"): Attachment {
        val course1 = data.courses.values.first()
        val fileId = data.addFileToCourse(
                courseId = course1.id,
                displayName = name,
                contentType = "text/html",
                fileContent = html
        )

        val mockUrl = "https://mock-data.instructure.com/files/$fileId/preview"
        val attachment = Attachment(
                id = data.newItemId(),
                contentType = "text/html",
                displayName = name,
                filename = name,
                url = mockUrl,
                previewUrl = mockUrl,
                createdAt = Date(),
                size = html.length.toLong()
        )

        return attachment
    }

    // Mock a specified number of students and courses, sign in, then navigate to course browser page for
    // first course.
    private fun getToCourse(
            studentCount: Int = 1,
            courseCount: Int = 1): MockCanvas {

        // Basic info
        val data = MockCanvas.init(
                studentCount = studentCount,
                courseCount = courseCount,
                favoriteCourseCount = courseCount)
        course = data.courses.values.first()

        // Sign in
        val student = data.students[0]
        val token = data.tokenFor(student)!!
        tokenLogin(data.domain, token, student)
        dashboardPage.waitForRender()

        // Navigate to the (first) course
        dashboardPage.selectCourse(course)

        return data
    }
}
