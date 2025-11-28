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
import com.instructure.canvas.espresso.mockcanvas.addSubmissionForAssignment
import com.instructure.canvas.espresso.mockcanvas.fakes.FakeAssignmentDetailsManager
import com.instructure.canvas.espresso.mockcanvas.fakes.FakeCommentLibraryManager
import com.instructure.canvas.espresso.mockcanvas.fakes.FakeCustomGradeStatusesManager
import com.instructure.canvas.espresso.mockcanvas.fakes.FakeDifferentiationTagsManager
import com.instructure.canvas.espresso.mockcanvas.fakes.FakeInboxSettingsManager
import com.instructure.canvas.espresso.mockcanvas.fakes.FakePostPolicyManager
import com.instructure.canvas.espresso.mockcanvas.fakes.FakeStudentContextManager
import com.instructure.canvas.espresso.mockcanvas.fakes.FakeSubmissionCommentsManager
import com.instructure.canvas.espresso.mockcanvas.fakes.FakeSubmissionContentManager
import com.instructure.canvas.espresso.mockcanvas.fakes.FakeSubmissionDetailsManager
import com.instructure.canvas.espresso.mockcanvas.fakes.FakeSubmissionGradeManager
import com.instructure.canvas.espresso.mockcanvas.fakes.FakeSubmissionRubricManager
import com.instructure.canvas.espresso.mockcanvas.init
import com.instructure.canvasapi2.di.GraphQlApiModule
import com.instructure.canvasapi2.di.graphql.CustomGradeStatusModule
import com.instructure.canvasapi2.managers.CommentLibraryManager
import com.instructure.canvasapi2.managers.InboxSettingsManager
import com.instructure.canvasapi2.managers.PostPolicyManager
import com.instructure.canvasapi2.managers.StudentContextManager
import com.instructure.canvasapi2.managers.SubmissionRubricManager
import com.instructure.canvasapi2.managers.graphql.AssignmentDetailsManager
import com.instructure.canvasapi2.managers.graphql.CustomGradeStatusesManager
import com.instructure.canvasapi2.managers.graphql.DifferentiationTagsManager
import com.instructure.canvasapi2.managers.graphql.SubmissionCommentsManager
import com.instructure.canvasapi2.managers.graphql.SubmissionContentManager
import com.instructure.canvasapi2.managers.graphql.SubmissionDetailsManager
import com.instructure.canvasapi2.managers.graphql.SubmissionGradeManager
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
@UninstallModules(
    GraphQlApiModule::class,
    DifferentiationTagsModule::class,
    CustomGradeStatusModule::class
)
class SpeedGraderCommentsInteractionTest : TeacherComposeTest() {

    override fun displaysPageObjects() = Unit

    @BindValue
    @JvmField
    val commentLibraryManager: CommentLibraryManager = FakeCommentLibraryManager()

    @BindValue
    @JvmField
    val postPolicyManager: PostPolicyManager = FakePostPolicyManager()

    @BindValue
    @JvmField
    val inboxSettingsManager: InboxSettingsManager = FakeInboxSettingsManager()

    @BindValue
    @JvmField
    val personContextManager: StudentContextManager = FakeStudentContextManager()

    @BindValue
    @JvmField
    val assignmentDetailsManager: AssignmentDetailsManager = FakeAssignmentDetailsManager()

    @BindValue
    @JvmField
    val submissionContentManager: SubmissionContentManager = FakeSubmissionContentManager()

    @BindValue
    @JvmField
    val submissionGradeManager: SubmissionGradeManager = FakeSubmissionGradeManager()

    @BindValue
    @JvmField
    val submissionDetailsManager: SubmissionDetailsManager = FakeSubmissionDetailsManager()

    @BindValue
    @JvmField
    val submissionRubricManager: SubmissionRubricManager = FakeSubmissionRubricManager()

    @BindValue
    @JvmField
    val submissionCommentsManager: SubmissionCommentsManager = FakeSubmissionCommentsManager()

    @BindValue
    @JvmField
    val customGradeStatusesManager: CustomGradeStatusesManager = FakeCustomGradeStatusesManager()

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

    @Test
    fun assertCommentWithAuthorAndTextDisplayed() {
        val submission = goToSpeedGraderCommentsPage(commentText = "Important comment")

        val authorName = submission.submissionComments[0].authorName!!
        val commentText = submission.submissionComments[0].comment!!
        speedGraderPage.assertCommentsLabelDisplayed(1)
        speedGraderPage.assertCommentDisplayed(commentText,authorName)
    }

    @Test
    fun assertCommentsLabelDisplayed() {
        val submission = goToSpeedGraderCommentsPage(commentText = "Important comment")

        val commentCount = submission.submissionComments.size
        speedGraderPage.assertCommentsLabelDisplayed(commentCount)
    }

    @Test
    fun addsNewTextComment() {
        goToSpeedGraderCommentsPage()

        val newComment = randomString(32)

        speedGraderPage.typeCommentInInputField(newComment)
        speedGraderPage.clickSendCommentButton()
        speedGraderPage.assertCommentDisplayed(newComment, author = null)
    }

    @Test
    fun showsNoComments() {
        goToSpeedGraderCommentsPage()

        speedGraderPage.assertCommentsLabelDisplayed(0)
    }

    @Test
    fun displaysOwnCommentText() {
        val submission = goToSpeedGraderCommentsPage(commentText = "teacher's comment", isTeacherComment = true)

        val commentText = submission.submissionComments[0].comment!!
        speedGraderPage.assertCommentsLabelDisplayed(1)
        speedGraderPage.assertCommentDisplayed(commentText, author = null)
        speedGraderPage.assertCommentAuthorNameNotDisplayed()
    }

    @Test
    fun sendsCommentFromCommentLibrary() {
        goToSpeedGraderCommentsPage(seedCommentLibrary = true)

        speedGraderPage.clickCommentLibraryButton()
        speedGraderPage.assertCommentLibraryTitle()
        speedGraderPage.assertCommentLibraryItemCount(3)

        speedGraderPage.selectCommentLibraryResultItem(0)
        speedGraderPage.clickSendCommentButton(commentLibraryOpened = true)
        speedGraderPage.assertCommentDisplayed("Great work!", author = null)
    }

    @Test
    fun displaysMultipleCommentsInOrder() {
        val submission = goToSpeedGraderCommentsPage(commentCount = 4)

        val commentText1 = submission.submissionComments[0].comment!!
        val commentText2 = submission.submissionComments[1].comment!!
        val commentText3 = submission.submissionComments[2].comment!!
        val commentText4 = submission.submissionComments[3].comment!!

        speedGraderPage.assertCommentsLabelDisplayed(4)

        speedGraderPage.assertCommentTextDisplayed(commentText1, isOwnComment = false)
        speedGraderPage.assertCommentTextDisplayed(commentText2, isOwnComment = true)
        speedGraderPage.assertCommentTextDisplayed(commentText3, isOwnComment = false)
        speedGraderPage.assertCommentTextDisplayed(commentText4, isOwnComment = true)

    }

    @Stub
    @Test
    fun displaysCommentWithAttachment() {
        val submission = goToSpeedGraderCommentsPage(
            commentText = "Please check this file",
            isTeacherComment = false,
            attachment = attachment
        )

        val commentText = submission.submissionComments[0].comment!!
        val authorName = submission.submissionComments[0].authorName!!
        val attachmentName = submission.submissionComments[0].attachments[0].displayName

        speedGraderPage.assertCommentsLabelDisplayed(1)
        speedGraderPage.assertCommentDisplayed(commentText, authorName)
        speedGraderPage.assertCommentAttachmentDisplayed(attachmentName.toString())
    }

    /**
     * Common setup routine for SpeedGrader comments tests.
     * Always creates an assignment with ONLINE_TEXT_ENTRY submission type.
     *
     * @param commentText if not null, include a single comment with the submission containing this text (ignored if commentCount > 0)
     * @param isTeacherComment if true, the comment will be authored by the teacher (own comment), otherwise by the student
     * @param attachment if non-null, is either a comment attachment (if commentText is not null) or a submission attachment (if commentText is null)
     * @param seedCommentLibrary if true, seeds the comment library with predefined comments
     * @param commentCount if > 0, creates multiple alternating student/teacher comments (overrides commentText parameter)
     * @return the created submission
     */
    private fun goToSpeedGraderCommentsPage(
        commentText: String? = null,
        isTeacherComment: Boolean = false,
        attachment: Attachment? = null,
        seedCommentLibrary: Boolean = false,
        commentCount: Int = 0
    ): Submission {

        val data = MockCanvas.init(teacherCount = 1, studentCount = 1, courseCount = 1, favoriteCourseCount = 1)
        val teacher = data.teachers[0]
        val course = data.courses.values.first()
        val student = data.students[0]

        if (seedCommentLibrary) {
            data.commentLibraryItems[teacher.id] = listOf(
                "Great work!",
                "Please review the instructions",
                "Well done on this assignment"
            )
        }

        data.addCoursePermissions(
                course.id,
                CanvasContextPermission()
        )

        val assignment = data.addAssignment(
                courseId = course.id,
                submissionTypeList = listOf(Assignment.SubmissionType.ONLINE_TEXT_ENTRY)
        )

        val allComments = when {
            commentCount > 0 -> {
                List(commentCount) { i ->
                    val isStudentComment = i % 2 == 0
                    val author = if (isStudentComment) student else teacher
                    val commentNumber = if (isStudentComment) (i / 2) + 1 else (i / 2) + 1

                    SubmissionComment(
                        id = data.newItemId(),
                        authorId = author.id,
                        authorName = author.shortName,
                        authorPronouns = author.pronouns,
                        attempt = 1L,
                        comment = if (isStudentComment) {
                            when (commentNumber) {
                                1 -> "First student comment"
                                2 -> "Second student comment"
                                else -> "Student comment #$commentNumber"
                            }
                        } else {
                            when (commentNumber) {
                                1 -> "Teacher reply"
                                2 -> "Another teacher reply"
                                else -> "Teacher reply #$commentNumber"
                            }
                        },
                        attachments = arrayListOf()
                    )
                }
            }
            commentText != null -> {
                val author = if (isTeacherComment) teacher else student
                listOf(
                    SubmissionComment(
                        id = data.newItemId(),
                        authorId = author.id,
                        authorName = author.shortName,
                        authorPronouns = author.pronouns,
                        attempt = 1L,
                        comment = commentText,
                        attachments = if (attachment == null) arrayListOf() else arrayListOf(attachment)
                    )
                )
            }
            else -> emptyList()
        }

        val submission = data.addSubmissionForAssignment(
                assignmentId = assignment.id,
                userId = student.id,
                type = Assignment.SubmissionType.ONLINE_TEXT_ENTRY.apiString,
                body = "This is a test submission",
                attachment = if (commentText == null && commentCount == 0) attachment else null,
                comment = allComments.firstOrNull()
        )

        if (allComments.isNotEmpty()) {
            submission.submissionComments = allComments
        }

        val token = data.tokenFor(teacher)!!
        tokenLogin(data.domain, token, teacher)

        dashboardPage.openCourse(course)
        courseBrowserPage.openAssignmentsTab()
        assignmentListPage.clickAssignment(assignment)
        assignmentDetailsPage.clickAllSubmissions()
        assignmentSubmissionListPage.clickSubmission(student)
        composeTestRule.waitForIdle()
        if (isCompactDevice()) speedGraderPage.clickExpandPanelButton()
        speedGraderPage.selectTab("Grade & Rubric")
        composeTestRule.waitForIdle()

        return submission
    }
}
