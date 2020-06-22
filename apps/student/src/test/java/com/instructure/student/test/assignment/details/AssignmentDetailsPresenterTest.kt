/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
package com.instructure.student.test.assignment.details

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.canvasapi2.CanvasRestAdapter
import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.isRtl
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.interactions.router.Route
import com.instructure.interactions.router.RouterParams
import com.instructure.student.R
import com.instructure.student.mobius.assignmentDetails.AssignmentDetailsModel
import com.instructure.student.mobius.assignmentDetails.AssignmentDetailsPresenter
import com.instructure.student.mobius.assignmentDetails.ui.AssignmentDetailsFragment
import com.instructure.student.mobius.assignmentDetails.ui.AssignmentDetailsViewState
import com.instructure.student.mobius.assignmentDetails.ui.AssignmentDetailsVisibilities
import com.instructure.student.mobius.assignmentDetails.ui.DiscussionHeaderViewState
import io.mockk.*
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.threeten.bp.OffsetDateTime
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import java.util.Locale
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class AssignmentDetailsPresenterTest : Assert() {

    private lateinit var context: Context
    private lateinit var baseModel: AssignmentDetailsModel
    private lateinit var baseAssignment: Assignment
    private lateinit var baseVisibilities: AssignmentDetailsVisibilities
    private lateinit var baseSubmission: Submission
    private lateinit var baseDiscussion: DiscussionTopicHeader
    private lateinit var baseQuiz: Quiz

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        baseModel = AssignmentDetailsModel(0, Course())
        baseAssignment = Assignment(
            id = 123,
            name = "Assignment Name",
            description = "This is a description",
            pointsPossible = 35.0
        )
        baseVisibilities = AssignmentDetailsVisibilities(
            title = true,
            dueDate = true,
            submissionTypes = true,
            submissionStatus = true,
            description = true,
            submissionAndRubricButton = true,
            submitButtonEnabled = true
        )
        baseSubmission = Submission(
            attempt = 1,
            submittedAt = Date(),
            workflowState = "submitted"
        )
        baseQuiz = Quiz(
            id = 123L
        )
        baseDiscussion = DiscussionTopicHeader(
            id = 123L,
            message = "discussion message",
            author = DiscussionParticipant(
                displayName = "Hodor",
                avatarImageUrl = "pretty-hodor.com"
            ),
            postedDate = Date()
        )
    }

    @Test
    fun `Returns Loading state when model is loading`() {
        val expectedState = AssignmentDetailsViewState.Loading
        val model = baseModel.copy(isLoading = true)
        val actualState = AssignmentDetailsPresenter.present(model, context)
        assertEquals(expectedState, actualState)
    }

    @Test
    fun `Returns failed state when model result is failed`() {
        val expectedState = AssignmentDetailsViewState.Error
        val model = baseModel.copy(assignmentResult = DataResult.Fail())
        val actualState = AssignmentDetailsPresenter.present(model, context)
        assertEquals(expectedState, actualState)
    }

    @Test
    fun `Shows only title content, submission and rubric button, and lock views when assignment locked by date`() {
        val unlockDate = Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(10))
        val assignment = baseAssignment.copy(
            lockInfo = LockInfo(unlockAt = unlockDate.toApiString()),
            unlockAt = unlockDate.toApiString()
        )
        val model = baseModel.copy(assignmentResult = DataResult.Success(assignment))
        val expected = AssignmentDetailsVisibilities(
            title = true,
            lockedMessage = true,
            lockedImage = true,
            submissionAndRubricButton = true
        )
        val actual = AssignmentDetailsPresenter.present(model, context).visibilities
        assertEquals(expected, actual)
    }

    @Test
    fun `Shows only title content, submission and rubric button, and lock views when assignment is in a locked module`() {
        val assignment = baseAssignment.copy(
            lockInfo = LockInfo(
                contextModule = LockedModule(name = "Locked Module")
            )
        )
        val model = baseModel.copy(assignmentResult = DataResult.Success(assignment))
        val expected = AssignmentDetailsVisibilities(
            title = true,
            lockedMessage = true,
            lockedImage = true,
            submissionAndRubricButton = true
        )
        val actual = AssignmentDetailsPresenter.present(model, context).visibilities
        assertEquals(expected, actual)
    }

    @Test
    fun `Shows lock explanation when assignment availability date has passed`() {
        val lockDate = Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(10))
        val lockMessage = "Locked by test"
        val assignment = baseAssignment.copy(
            lockAt = lockDate.toApiString(),
            lockedForUser = true,
            lockExplanation = lockMessage,
            submissionTypesRaw = listOf("online_upload")
        )
        val model = baseModel.copy(assignmentResult = DataResult.Success(assignment))
        val expected = baseVisibilities.copy(
            lockedMessage = true,
            lockedImage = false
        )
        val actual = AssignmentDetailsPresenter.present(model, context).visibilities
        assertEquals(expected, actual)
    }

    @Test
    fun `Shows allowed file types when available`() {
        val assignment = baseAssignment.copy(
            submissionTypesRaw = listOf("online_upload"),
            allowedExtensions = listOf("pdf", "JPG", "PNG", "zip")
        )
        val model = baseModel.copy(assignmentResult = DataResult.Success(assignment))
        val actual = AssignmentDetailsPresenter.present(model, context).visibilities
        assertTrue(actual.fileTypes)
    }

    @Test
    fun `Does not show allowed file types when not an online submission`() {
        val assignment = baseAssignment.copy(
            allowedExtensions = listOf("pdf", "JPG", "PNG", "zip")
        )
        val model = baseModel.copy(assignmentResult = DataResult.Success(assignment))
        val expected = baseVisibilities.copy(fileTypes = false, submissionTypes = false, submissionStatus = false)
        val actual = AssignmentDetailsPresenter.present(model, context).visibilities
        assertEquals(expected, actual)
    }

    @Test
    fun `Uses correct icon for submitted status`() {
        val assignment = baseAssignment.copy(submission = baseSubmission)
        val model = baseModel.copy(assignmentResult = DataResult.Success(assignment))
        val state = AssignmentDetailsPresenter.present(model, context) as AssignmentDetailsViewState.Loaded
        assertEquals(R.drawable.vd_submitted, state.submittedStateIcon)
    }

    @Test
    fun `Uses correct label text for submitted status`() {
        val assignment = baseAssignment.copy(submission = baseSubmission)
        val model = baseModel.copy(assignmentResult = DataResult.Success(assignment))
        val state = AssignmentDetailsPresenter.present(model, context) as AssignmentDetailsViewState.Loaded
        assertEquals("Submitted", state.submittedStateLabel)
    }

    @Test
    fun `Uses correct label text for submitted status when submission is graded`() {
        val submission = baseSubmission.copy(grade = "8", postedAt = Date(), workflowState = "graded")
        val assignment = baseAssignment.copy(submission = submission)
        val model = baseModel.copy(assignmentResult = DataResult.Success(assignment))
        val state = AssignmentDetailsPresenter.present(model, context) as AssignmentDetailsViewState.Loaded
        assertEquals("Graded", state.submittedStateLabel)
    }

    @Test
    fun `Uses correct label text for submitted status when submission is missing`() {
        val submission = baseSubmission.copy(attempt = 0, missing = true, workflowState = "unsubmitted", submittedAt = null)
        val assignment = baseAssignment.copy(submission = submission)
        val model = baseModel.copy(assignmentResult = DataResult.Success(assignment))
        val state = AssignmentDetailsPresenter.present(model, context) as AssignmentDetailsViewState.Loaded
        assertEquals("Missing", state.submittedStateLabel)
    }

    @Test
    fun `Uses correct label text for submitted status when submission is past due`() {
        val calendar = Calendar.getInstance().apply { set(2000, 0, 31, 23, 59, 0) }

        val submission = baseSubmission.copy(attempt = 0, workflowState = "unsubmitted", submittedAt = null)
        val assignment = baseAssignment.copy(submission = submission, dueAt = calendar.time.toApiString(), submissionTypesRaw = listOf(Assignment.SubmissionType.ONLINE_UPLOAD.apiString))
        val model = baseModel.copy(assignmentResult = DataResult.Success(assignment))
        val state = AssignmentDetailsPresenter.present(model, context) as AssignmentDetailsViewState.Loaded
        assertEquals("Missing", state.submittedStateLabel)
    }

    @Test
    fun `Uses correct label text for submitted status when submission is past due and null`() {
        val calendar = Calendar.getInstance().apply { set(2000, 0, 31, 23, 59, 0) }

        val assignment = baseAssignment.copy(submission = null, dueAt = calendar.time.toApiString(), submissionTypesRaw = listOf(Assignment.SubmissionType.ONLINE_UPLOAD.apiString))
        val model = baseModel.copy(assignmentResult = DataResult.Success(assignment))
        val state = AssignmentDetailsPresenter.present(model, context) as AssignmentDetailsViewState.Loaded
        assertEquals("Missing", state.submittedStateLabel)
    }

    @Test
    fun `Uses correct label text for submitted status when submission is past due for an LTI assignment`() {
        val calendar = Calendar.getInstance().apply { set(2000, 0, 31, 23, 59, 0) }

        val submission = baseSubmission.copy(attempt = 0, workflowState = "unsubmitted", submittedAt = null)
        val assignment = baseAssignment.copy(submission = submission, dueAt = calendar.time.toApiString(), submissionTypesRaw = listOf("basic_lti_launch"))
        val model = baseModel.copy(assignmentResult = DataResult.Success(assignment))
        val state = AssignmentDetailsPresenter.present(model, context) as AssignmentDetailsViewState.Loaded
        assertEquals("Not Submitted", state.submittedStateLabel)
    }

    @Test
    fun `Uses correct label text for submitted status when submission is past due for an external assignment`() {
        val calendar = Calendar.getInstance().apply { set(2000, 0, 31, 23, 59, 0) }

        val submission = baseSubmission.copy(attempt = 0, workflowState = "unsubmitted", submittedAt = null)
        val assignment = baseAssignment.copy(submission = submission, dueAt = calendar.time.toApiString(), submissionTypesRaw = listOf("external_tool"))
        val model = baseModel.copy(assignmentResult = DataResult.Success(assignment))
        val state = AssignmentDetailsPresenter.present(model, context) as AssignmentDetailsViewState.Loaded
        assertEquals("Not Submitted", state.submittedStateLabel)
    }

    @Test
    fun `Uses correct label text for submitted status when submission is null`() {
        val assignment = baseAssignment.copy(submission = null, dueAt = null)
        val model = baseModel.copy(assignmentResult = DataResult.Success(assignment))
        val state = AssignmentDetailsPresenter.present(model, context) as AssignmentDetailsViewState.Loaded
        assertEquals("Not Submitted", state.submittedStateLabel)
    }

    @Test
    fun `Uses correct label text for submitted status when submission is not submitted`() {
        val submission = baseSubmission.copy(attempt = 0, workflowState = "unsubmitted", submittedAt = null)
        val assignment = baseAssignment.copy(submission = submission)
        val model = baseModel.copy(assignmentResult = DataResult.Success(assignment))
        val state = AssignmentDetailsPresenter.present(model, context) as AssignmentDetailsViewState.Loaded
        assertEquals("Not Submitted", state.submittedStateLabel)
    }

    @Test
    fun `Uses correct label text for submitted status when submission is graded but not submitted`() {
        val submission = baseSubmission.copy(attempt = 0, submittedAt = null, grade = "8", postedAt = Date(), workflowState = "graded")
        val assignment = baseAssignment.copy(submission = submission)
        val model = baseModel.copy(assignmentResult = DataResult.Success(assignment))
        val state = AssignmentDetailsPresenter.present(model, context) as AssignmentDetailsViewState.Loaded
        assertEquals("Not Submitted", state.submittedStateLabel)
    }

    @Test
    fun `Uses green color for submitted status`() {
        val assignment = baseAssignment.copy(submission = baseSubmission)
        val model = baseModel.copy(assignmentResult = DataResult.Success(assignment))
        val state = AssignmentDetailsPresenter.present(model, context) as AssignmentDetailsViewState.Loaded
        assertEquals(0xFF00AC18.toInt(), state.submittedStateColor)
    }

    @Test
    fun `uses correct icon for not-submitted status`() {
        val model = baseModel.copy(assignmentResult = DataResult.Success(baseAssignment))
        val state = AssignmentDetailsPresenter.present(model, context) as AssignmentDetailsViewState.Loaded
        assertEquals(R.drawable.vd_unsubmitted, state.submittedStateIcon)
    }

    @Test
    fun `Uses correct text for non-submitted status`() {
        val model = baseModel.copy(assignmentResult = DataResult.Success(baseAssignment))
        val state = AssignmentDetailsPresenter.present(model, context) as AssignmentDetailsViewState.Loaded
        assertEquals("Not Submitted", state.submittedStateLabel)
    }

    @Test
    fun `Uses gray color for non-submitted status`() {
        val model = baseModel.copy(assignmentResult = DataResult.Success(baseAssignment))
        val state = AssignmentDetailsPresenter.present(model, context) as AssignmentDetailsViewState.Loaded
        assertEquals(0xFF8B969E.toInt(), state.submittedStateColor)
    }

    @Test
    fun `Uses assignment name`() {
        val assignment = baseAssignment
        val model = baseModel.copy(assignmentResult = DataResult.Success(assignment))
        val state = AssignmentDetailsPresenter.present(model, context) as AssignmentDetailsViewState.Loaded
        assertEquals(assignment.name, state.assignmentName)
    }

    @Test
    fun `Formats points correctly`() {
        val assignment = baseAssignment.copy(pointsPossible = 15.0)
        val model = baseModel.copy(assignmentResult = DataResult.Success(assignment))
        val state = AssignmentDetailsPresenter.present(model, context) as AssignmentDetailsViewState.Loaded
        assertEquals("15 pts", state.assignmentPoints)
    }

    @Test
    fun `Formats points with decimal correctly`() {
        val assignment = baseAssignment.copy(pointsPossible = 12.50)
        val model = baseModel.copy(assignmentResult = DataResult.Success(assignment))
        val state = AssignmentDetailsPresenter.present(model, context) as AssignmentDetailsViewState.Loaded
        assertEquals("12.5 pts", state.assignmentPoints)
    }

    @Test
    fun `Uses a11y-friendly content description on points`() {
        val assignment = baseAssignment.copy(pointsPossible = 15.0)
        val model = baseModel.copy(assignmentResult = DataResult.Success(assignment))
        val state = AssignmentDetailsPresenter.present(model, context) as AssignmentDetailsViewState.Loaded
        assertEquals("15 points", state.assignmentPointsA11yText)
    }

    @Test
    fun `Uses RTL content description on RTL devices`() {
        val description = "<body dir=\"rtl\">This is a description</body>"

        mockkStatic("com.instructure.canvasapi2.utils.KotlinUtilsKt")
        every { any<Locale>().isRtl } returns true

        val assignment = baseAssignment.copy(pointsPossible = 15.0)
        val model = baseModel.copy(assignmentResult = DataResult.Success(assignment))
        val state = AssignmentDetailsPresenter.present(model, context) as AssignmentDetailsViewState.Loaded
        assertEquals(description, state.description)

        unmockkStatic("com.instructure.canvasapi2.utils.KotlinUtilsKt")
    }

    @Test
    fun `Formats due date correctly`() {
        val expectedDueDate = "January 31, 2050 at 11:59 PM"
        val calendar = Calendar.getInstance().apply { set(2050, 0, 31, 23, 59, 0) }
        val assignment = baseAssignment.copy(dueAt = calendar.time.toApiString())
        val model = baseModel.copy(assignmentResult = DataResult.Success(assignment))
        val state = AssignmentDetailsPresenter.present(model, context) as AssignmentDetailsViewState.Loaded
        assertEquals(expectedDueDate, state.dueDate)
    }

    @Test
    fun `Shows no due date message when there is no due date`() {
        val expectedDueDate = "No Due Date"
        val assignment = baseAssignment.copy(dueAt = null)
        val model = baseModel.copy(assignmentResult = DataResult.Success(assignment))
        val state = AssignmentDetailsPresenter.present(model, context) as AssignmentDetailsViewState.Loaded
        assertEquals(expectedDueDate, state.dueDate)
    }

    @Test
    fun `Formats online submission types correctly`() {
        // You can't have an assignment that is both a standard online assignment and a quiz
        val allTypes = listOf(Assignment.SubmissionType.ONLINE_UPLOAD, Assignment.SubmissionType.ONLINE_TEXT_ENTRY, Assignment.SubmissionType.ONLINE_URL, Assignment.SubmissionType.DISCUSSION_TOPIC, Assignment.SubmissionType.BASIC_LTI_LAUNCH, Assignment.SubmissionType.ATTENDANCE, Assignment.SubmissionType.MEDIA_RECORDING)
        val expected = allTypes.map { Assignment.submissionTypeToPrettyPrintString(it, context) }.joinToString(", ")
        val assignment = baseAssignment.copy(
            submissionTypesRaw = allTypes.map { it.apiString }
        )
        val model = baseModel.copy(assignmentResult = DataResult.Success(assignment))
        val state = AssignmentDetailsPresenter.present(model, context) as AssignmentDetailsViewState.Loaded
        assertEquals(expected, state.submissionTypes)
    }

    @Test
    fun `Formats online quiz submission type correctly`() {
        val allTypes = listOf(Assignment.SubmissionType.ONLINE_QUIZ)
        val expected = allTypes.map { Assignment.submissionTypeToPrettyPrintString(it, context) }.joinToString(", ")
        val assignment = baseAssignment.copy(
                submissionTypesRaw = allTypes.map { it.apiString },
                quizId = baseQuiz.id
        )
        val model = baseModel.copy(assignmentResult = DataResult.Success(assignment), quizResult = DataResult.Success(baseQuiz))
        val state = AssignmentDetailsPresenter.present(model, context) as AssignmentDetailsViewState.Loaded
        assertEquals(expected, state.submissionTypes)
    }

    @Test
    fun `Formats offline submission type correctly`() {
        val allTypes = listOf(Assignment.SubmissionType.ON_PAPER)
        val expected = allTypes.map { Assignment.submissionTypeToPrettyPrintString(it, context) }.joinToString(", ")
        val assignment = baseAssignment.copy(
                submissionTypesRaw = allTypes.map { it.apiString }
        )
        val model = baseModel.copy(assignmentResult = DataResult.Success(assignment))
        val state = AssignmentDetailsPresenter.present(model, context) as AssignmentDetailsViewState.Loaded
        assertEquals(expected, state.submissionTypes)
    }

    @Test
    fun `Formats allowed file types correctly`() {
        val expected = "PNG, JPG, PDF, APK, DOC"
        val assignment = baseAssignment.copy(
            allowedExtensions = listOf("PNG", "JPG", "PDF", "APK", "DOC")
        )
        val model = baseModel.copy(assignmentResult = DataResult.Success(assignment))
        val state = AssignmentDetailsPresenter.present(model, context) as AssignmentDetailsViewState.Loaded
        assertEquals(expected, state.fileTypes)
    }

    @Test
    fun `Shows submit button when submissions are allowed`() {
        val assignment = baseAssignment.copy(
            submissionTypesRaw = listOf("online_text_entry")
        )
        val model = baseModel.copy(assignmentResult = DataResult.Success(assignment))
        val actual = AssignmentDetailsPresenter.present(model, context).visibilities
        assertTrue(actual.submitButton)
    }

    @Test
    fun `Never shows submit button when isObserver is true`() {
        val assignment = baseAssignment.copy(
                submissionTypesRaw = listOf("online_text_entry")
        )
        val model = baseModel.copy(assignmentResult = DataResult.Success(assignment), isObserver = true)
        val actual = AssignmentDetailsPresenter.present(model, context).visibilities
        assertFalse(actual.submitButton)
    }

    @Test
    fun `Submit button reads "Submit Assignment" if there are no existing submissions`() {
        val assignment = baseAssignment.copy(
            submissionTypesRaw = listOf("online_text_entry")
        )
        val model = baseModel.copy(assignmentResult = DataResult.Success(assignment))
        val state = AssignmentDetailsPresenter.present(model, context) as AssignmentDetailsViewState.Loaded
        assertEquals("Submit Assignment", state.submitButtonText)
    }

    @Test
    fun `Submit button shows re-submit text when there is an existing submission and further submissions are allowed`() {
        val assignment = baseAssignment.copy(
            submissionTypesRaw = listOf("online_text_entry"),
            submission = baseSubmission
        )
        val model = baseModel.copy(assignmentResult = DataResult.Success(assignment))
        val state = AssignmentDetailsPresenter.present(model, context) as AssignmentDetailsViewState.Loaded
        assertEquals("Resubmit Assignment", state.submitButtonText)
    }

    @Test
    fun `Submit button reads "View Quiz" if its a quiz with a submission`() {
        val assignment = baseAssignment.copy(
                submissionTypesRaw = listOf("online_quiz"),
                submission = baseSubmission,
                quizId = baseQuiz.id
        )
        val model = baseModel.copy(assignmentResult = DataResult.Success(assignment), quizResult = DataResult.Success(baseQuiz))
        val state = AssignmentDetailsPresenter.present(model, context) as AssignmentDetailsViewState.Loaded
        assertEquals("View Quiz", state.submitButtonText)
    }

    @Test
    fun `Submit button reads "View Quiz" if its a quiz with no submission`() {
        val assignment = baseAssignment.copy(
                submissionTypesRaw = listOf("online_quiz"),
                quizId = baseQuiz.id
        )
        val model = baseModel.copy(assignmentResult = DataResult.Success(assignment), quizResult = DataResult.Success(baseQuiz))
        val state = AssignmentDetailsPresenter.present(model, context) as AssignmentDetailsViewState.Loaded
        assertEquals("View Quiz", state.submitButtonText)
    }

    @Test
    fun `Submit button reads "View Discussion" if its a discussion with a submission`() {
        val assignment = baseAssignment.copy(
                submissionTypesRaw = listOf("discussion_topic"),
                submission = baseSubmission,
                discussionTopicHeader = baseDiscussion
        )
        val model = baseModel.copy(assignmentResult = DataResult.Success(assignment))
        val state = AssignmentDetailsPresenter.present(model, context) as AssignmentDetailsViewState.Loaded
        assertEquals("View Discussion", state.submitButtonText)
    }

    @Test
    fun `Submit button reads "View Discussion" if its a discussion with no submission`() {
        val assignment = baseAssignment.copy(
                submissionTypesRaw = listOf("discussion_topic"),
                discussionTopicHeader = baseDiscussion
        )
        val model = baseModel.copy(assignmentResult = DataResult.Success(assignment))
        val state = AssignmentDetailsPresenter.present(model, context) as AssignmentDetailsViewState.Loaded
        assertEquals("View Discussion", state.submitButtonText)
    }

    @Test
    fun `Submit button reads "Launch External Tool" if submission type is external tool`() {
        val assignment = baseAssignment.copy(
                submissionTypesRaw = listOf(Assignment.SubmissionType.EXTERNAL_TOOL.apiString)
        )
        val model = baseModel.copy(assignmentResult = DataResult.Success(assignment))
        val state = AssignmentDetailsPresenter.present(model, context) as AssignmentDetailsViewState.Loaded
        assertEquals("Launch External Tool", state.submitButtonText)
    }

    @Test
    fun `Displays 'Submission & Rubric' button`() {
        val model = baseModel.copy(assignmentResult = DataResult.Success(baseAssignment))
        val visibilities = AssignmentDetailsPresenter.present(model, context).visibilities
        assertTrue(visibilities.submissionAndRubricButton)
    }

    @Test
    fun `Shows no description message when there is no valid description`() {
        val assignment = baseAssignment.copy(
            description = null
        )
        val model = baseModel.copy(assignmentResult = DataResult.Success(assignment))
        val visibilities = AssignmentDetailsPresenter.present(model, context).visibilities
        assertFalse(visibilities.description)
        assertTrue(visibilities.noDescriptionLabel)
    }

    @Test
    fun `Displays grade cell when grade is not empty`() {
        val assignment = baseAssignment.copy(
            submission = baseSubmission.copy(
                workflowState = "graded",
                enteredScore = 85.0,
                enteredGrade = "85",
                score = 85.0,
                grade = "85"
            )
        )
        val model = baseModel.copy(assignmentResult = DataResult.Success(assignment))
        val actual = AssignmentDetailsPresenter.present(model, context).visibilities.grade
        assertTrue(actual)
    }

    @Test
    fun `Displays grade cell when grade is not empty and there is a failed submission`() {
        val submission = com.instructure.student.Submission.Impl(
            id = 123L,
            submissionEntry = null,
            lastActivityDate = OffsetDateTime.now(),
            assignmentName = null,
            assignmentId = baseAssignment.id,
            canvasContext = CanvasContext.emptyCourseContext(0),
            submissionType = "online_text_entry",
            errorFlag = false,
            assignmentGroupCategoryId = null,
            userId = 0,
            currentFile = 0,
            fileCount = 0,
            progress = null
        )
        val assignment = baseAssignment.copy(
            submission = baseSubmission.copy(
                workflowState = "graded",
                enteredScore = 85.0,
                enteredGrade = "85",
                score = 85.0,
                grade = "85"
            )
        )
        val model = baseModel.copy(assignmentResult = DataResult.Success(assignment), databaseSubmission = submission)
        val actual = AssignmentDetailsPresenter.present(model, context).visibilities.grade
        assertTrue(actual)
    }

    @Test
    fun `Does not display submitted cell when grade is null and there is a failed submission`() {
        val submission = com.instructure.student.Submission.Impl(
            id = 123L,
            submissionEntry = null,
            lastActivityDate = OffsetDateTime.now(),
            assignmentName = null,
            assignmentId = baseAssignment.id,
            canvasContext = CanvasContext.emptyCourseContext(0),
            submissionType = "online_text_entry",
            errorFlag = false,
            assignmentGroupCategoryId = null,
            userId = 0,
            currentFile = 0,
            fileCount = 0,
            progress = null
        )
        val assignment = baseAssignment.copy(
            submission = baseSubmission.copy(
                workflowState = "submitted",
                grade = null
            )
        )
        val model = baseModel.copy(assignmentResult = DataResult.Success(assignment), databaseSubmission = submission)
        val actual = AssignmentDetailsPresenter.present(model, context).visibilities.grade
        assertFalse(actual)
    }

    @Test
    fun `Does not display empty cell when grade is empty and there is a failed submission`() {
        val submission = com.instructure.student.Submission.Impl(
            id = 123L,
            submissionEntry = null,
            lastActivityDate = OffsetDateTime.now(),
            assignmentName = null,
            assignmentId = baseAssignment.id,
            canvasContext = CanvasContext.emptyCourseContext(0),
            submissionType = "online_text_entry",
            errorFlag = false,
            assignmentGroupCategoryId = null,
            userId = 0,
            currentFile = 0,
            fileCount = 0,
            progress = null
        )
        val assignment = baseAssignment.copy(
            submission = baseSubmission.copy(
                workflowState = "unsubmitted",
                submittedAt = null
            )
        )
        val model = baseModel.copy(assignmentResult = DataResult.Success(assignment), databaseSubmission = submission)
        val actual = AssignmentDetailsPresenter.present(model, context).visibilities.grade
        assertFalse(actual)
    }

    @Test
    fun `Displays upload in progress when database submission is not failed`() {
        val submission = com.instructure.student.Submission.Impl(
            id = 123L,
            submissionEntry = null,
            lastActivityDate = OffsetDateTime.now(),
            assignmentName = null,
            assignmentId = baseAssignment.id,
            canvasContext = CanvasContext.emptyCourseContext(0),
            submissionType = "online_text_entry",
            errorFlag = false,
            assignmentGroupCategoryId = null,
            userId = 0,
            currentFile = 0,
            fileCount = 0,
            progress = null
        )
        val model = baseModel.copy(
            assignmentResult = DataResult.Success(baseAssignment),
            databaseSubmission = submission
        )
        val actual = AssignmentDetailsPresenter.present(model, context).visibilities.submissionUploadStatusInProgress
        assertTrue(actual)
    }

    @Test
    fun `Displays failed submission when database submission is failed`() {
        val submission = com.instructure.student.Submission.Impl(
            id = 123L,
            submissionEntry = null,
            lastActivityDate = OffsetDateTime.now(),
            assignmentName = null,
            assignmentId = baseAssignment.id,
            canvasContext = CanvasContext.emptyCourseContext(0),
            submissionType = "online_text_entry",
            errorFlag = true,
            assignmentGroupCategoryId = null,
            userId = 0,
            currentFile = 0,
            fileCount = 0,
            progress = null
        )
        val model = baseModel.copy(
            assignmentResult = DataResult.Success(baseAssignment),
            databaseSubmission = submission
        )
        val actual = AssignmentDetailsPresenter.present(model, context).visibilities.submissionUploadStatusFailed
        assertTrue(actual)
    }

    @Test
    fun `isExternalToolSubmission is true if submission type is external tool`() {
        val assignment = baseAssignment.copy(
            submissionTypesRaw = listOf(Assignment.SubmissionType.EXTERNAL_TOOL.apiString)
        )
        val model = baseModel.copy(assignmentResult = DataResult.Success(assignment))
        val state = AssignmentDetailsPresenter.present(model, context) as AssignmentDetailsViewState.Loaded
        assertEquals(true, state.isExternalToolSubmission)
    }

    @Test
    fun `isExternalToolSubmission is false if submission type is not external tool`() {
        val assignment = baseAssignment.copy(
            submissionTypesRaw = listOf(Assignment.SubmissionType.ONLINE_UPLOAD.apiString)
        )
        val model = baseModel.copy(assignmentResult = DataResult.Success(assignment))
        val state = AssignmentDetailsPresenter.present(model, context) as AssignmentDetailsViewState.Loaded
        assertEquals(false, state.isExternalToolSubmission)
    }

    @Test
    fun `Hide Submit button if submission type is ON_PAPER`() {
        val assignment = baseAssignment.copy(
            submissionTypesRaw = listOf(Assignment.SubmissionType.ON_PAPER.apiString)
        )
        val model = baseModel.copy(assignmentResult = DataResult.Success(assignment))
        val state = AssignmentDetailsPresenter.present(model, context) as AssignmentDetailsViewState.Loaded
        assertEquals(false, state.visibilities.submitButton)
    }

    @Test
    fun `Hide Submit button if submission type is NONE`() {
        val assignment = baseAssignment.copy(
            submissionTypesRaw = listOf(Assignment.SubmissionType.NONE.apiString)
        )
        val model = baseModel.copy(assignmentResult = DataResult.Success(assignment))
        val state = AssignmentDetailsPresenter.present(model, context) as AssignmentDetailsViewState.Loaded
        assertEquals(false, state.visibilities.submitButton)
    }

    @Test
    fun `Description contains DiscussionHeaderState with no attachments when assignment is discussion`() {
        val authorAvatarUrl = "pretty-hodor.com"
        val authorName = "hodor"
        val authoredDate = "Jul 23 at 9:59 AM"
        val attachmentIconVisibility = false
        val discussionMessage = "yo yo yo"
        val calendar = GregorianCalendar.getInstance()
        calendar.set(2019, 6, 23, 9, 59)
        val discussionTopicHeader = baseDiscussion.copy(message = discussionMessage, author = DiscussionParticipant(displayName = authorName, avatarImageUrl = authorAvatarUrl), postedDate = calendar.time)
        val assignment = baseAssignment.copy(submissionTypesRaw = listOf("discussion_topic"), discussionTopicHeader = discussionTopicHeader)

        val model = baseModel.copy(assignmentResult = DataResult.Success(assignment))
        val state = AssignmentDetailsPresenter.present(model, context) as AssignmentDetailsViewState.Loaded
        val expectedState = DiscussionHeaderViewState.Loaded(authorAvatarUrl, authorName, null, authoredDate, attachmentIconVisibility)

        assertEquals(expectedState, state.discussionHeaderViewState)
    }

    @Test
    fun `Description contains DiscussionHeaderState with unknown author and date, when assignment is discussion with no author name or date`() {
        val authorAvatarUrl = "pretty-hodor.com"
        val authorName = "Unknown Author"
        val authoredDate = "Unknown Date"
        val discussionMessage = "yo yo yo"
        val attachmentIconVisibility = false
        val discussionTopicHeader = baseDiscussion.copy(message = discussionMessage, author = DiscussionParticipant(avatarImageUrl = authorAvatarUrl), postedDate = null)
        val assignment = baseAssignment.copy(submissionTypesRaw = listOf("discussion_topic"), discussionTopicHeader = discussionTopicHeader)

        val model = baseModel.copy(assignmentResult = DataResult.Success(assignment))
        val state = AssignmentDetailsPresenter.present(model, context) as AssignmentDetailsViewState.Loaded
        val expectedState = DiscussionHeaderViewState.Loaded(authorAvatarUrl, authorName, null, authoredDate, attachmentIconVisibility)

        assertEquals(expectedState, state.discussionHeaderViewState)
    }

    @Test
    fun `Description contains DiscussionHeaderState NoAuthor with when assignment is discussion with no author`() {
        val discussionMessage = "yo yo yo"
        val calendar = GregorianCalendar.getInstance()
        calendar.set(2019, 6, 23, 9, 59)
        val discussionTopicHeader = baseDiscussion.copy(message = discussionMessage, author = DiscussionParticipant(), postedDate = calendar.time)
        val assignment = baseAssignment.copy(submissionTypesRaw = listOf("discussion_topic"), discussionTopicHeader = discussionTopicHeader)

        val model = baseModel.copy(assignmentResult = DataResult.Success(assignment))
        val state = AssignmentDetailsPresenter.present(model, context) as AssignmentDetailsViewState.Loaded
        val expectedState = DiscussionHeaderViewState.NoAuthor

        assertEquals(expectedState, state.discussionHeaderViewState)
    }

    @Test
    fun `Description contains DiscussionHeaderState with attachments when assignment is discussion`() {
        val authorAvatarUrl = "pretty-hodor.com"
        val authorName = "hodor"
        val authoredDate = "Jul 23 at 9:59 AM"
        val attachmentIconVisibility = true
        val discussionMessage = "yo yo yo"
        val attachmentId = 12345L
        val remoteFiles = mutableListOf(RemoteFile(id = attachmentId))
        val calendar = GregorianCalendar.getInstance()
        calendar.set(2019, 6, 23, 9, 59)
        val discussionTopicHeader = baseDiscussion.copy(attachments = remoteFiles, message = discussionMessage, author = DiscussionParticipant(displayName = authorName, avatarImageUrl = authorAvatarUrl), postedDate = calendar.time)
        val assignment = baseAssignment.copy(submissionTypesRaw = listOf("discussion_topic"), discussionTopicHeader = discussionTopicHeader)

        val model = baseModel.copy(assignmentResult = DataResult.Success(assignment))
        val state = AssignmentDetailsPresenter.present(model, context) as AssignmentDetailsViewState.Loaded
        val expectedState = DiscussionHeaderViewState.Loaded(authorAvatarUrl, authorName, null, authoredDate, attachmentIconVisibility)

        assertEquals(expectedState, state.discussionHeaderViewState)
    }
    //val remoteFiles = mutableListOf(RemoteFile(id = attachmentId))

    @Test
    fun `Description contains author pronouns when assignment is discussion`() {
        val authorAvatarUrl = "pretty-hodor.com"
        val authorName = "hodor"
        val authorPronouns = "Pro/Noun"
        val authoredDate = "Jul 23 at 9:59 AM"
        val attachmentIconVisibility = false
        val discussionMessage = "yo yo yo"
        val calendar = GregorianCalendar.getInstance()
        calendar.set(2019, 6, 23, 9, 59)
        val discussionTopicHeader = baseDiscussion.copy(message = discussionMessage, author = DiscussionParticipant(displayName = authorName, pronouns = authorPronouns, avatarImageUrl = authorAvatarUrl), postedDate = calendar.time)
        val assignment = baseAssignment.copy(submissionTypesRaw = listOf("discussion_topic"), discussionTopicHeader = discussionTopicHeader)

        val model = baseModel.copy(assignmentResult = DataResult.Success(assignment))
        val state = AssignmentDetailsPresenter.present(model, context) as AssignmentDetailsViewState.Loaded
        val expectedState = DiscussionHeaderViewState.Loaded(authorAvatarUrl, authorName, authorPronouns, authoredDate, attachmentIconVisibility)

        assertEquals(expectedState, state.discussionHeaderViewState)
    }

    @Test
    fun `Description contains discussion topic header message when assignment is discussion`() {
        // This test no longer needs to find the baseDiscussion html text, as that formatting now happens when the
        // description is loaded into the webview.
        val assignment = baseAssignment.copy(submissionTypesRaw = listOf("discussion_topic"), discussionTopicHeader = baseDiscussion)

        val model = baseModel.copy(assignmentResult = DataResult.Success(assignment))
        val state = AssignmentDetailsPresenter.present(model, context) as AssignmentDetailsViewState.Loaded

        assertEquals(baseDiscussion.message, state.description)
    }

    @Test
    fun `SubmitButton is visible when assignment is discussion`() {
        val assignment = baseAssignment.copy(submissionTypesRaw = listOf("discussion_topic"), discussionTopicHeader = baseDiscussion)
        val model = baseModel.copy(assignmentResult = DataResult.Success(assignment))
        val state = AssignmentDetailsPresenter.present(model, context) as AssignmentDetailsViewState.Loaded
        assertEquals(true, state.visibilities.submitButton)
    }

    @Test
    fun `SubmitButton is visible when assignment is quiz`() {
        val assignment = baseAssignment.copy(
                submissionTypesRaw = listOf(Assignment.SubmissionType.ONLINE_QUIZ.apiString)
        )
        val model = baseModel.copy(assignmentResult = DataResult.Success(assignment), quizResult = DataResult.Success(baseQuiz))
        val state = AssignmentDetailsPresenter.present(model, context) as AssignmentDetailsViewState.Loaded
        assertEquals(true, state.visibilities.submitButton)
    }

    @Test
    fun `SubmitButton is visible when assignment is external tool`() {
        val assignment = baseAssignment.copy(submissionTypesRaw = listOf(Assignment.SubmissionType.EXTERNAL_TOOL.apiString))
        val model = baseModel.copy(assignmentResult = DataResult.Success(assignment))
        val state = AssignmentDetailsPresenter.present(model, context) as AssignmentDetailsViewState.Loaded
        assertEquals(true, state.visibilities.submitButton)
    }

    @Test
    fun `SubmitButton is visible when assignment is online`() {
        val assignment = baseAssignment.copy(submissionTypesRaw = listOf(Assignment.SubmissionType.ONLINE_UPLOAD.apiString, Assignment.SubmissionType.ONLINE_URL.apiString, Assignment.SubmissionType.ONLINE_TEXT_ENTRY.apiString))
        val model = baseModel.copy(assignmentResult = DataResult.Success(assignment))
        val state = AssignmentDetailsPresenter.present(model, context) as AssignmentDetailsViewState.Loaded
        assertEquals(true, state.visibilities.submitButton)
    }

    @Test
    fun `makeLockedState contains appropriate lock message when unlockDate is present`() {
        val unlockDate = Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(10))
        val assignment = baseAssignment.copy(
                unlockAt = unlockDate.toApiString(),
                lockInfo = LockInfo(unlockAt = unlockDate.toApiString())
        )
        val model = baseModel.copy(assignmentResult = DataResult.Success(assignment))
        val expected = AssignmentDetailsVisibilities(
                title = true,
                lockedMessage = true,
                lockedImage = true,
                submissionAndRubricButton = true
        )
        val actual = AssignmentDetailsPresenter.present(model, context).visibilities
        assertEquals(expected, actual)
    }

    @Test
    fun `Returns error state when assignment quiz fails`() {
        val assignment = baseAssignment.copy(
                submissionTypesRaw = listOf(Assignment.SubmissionType.ONLINE_QUIZ.apiString),
                quizId = baseQuiz.id
        )
        val model = baseModel.copy(assignmentResult = DataResult.Success(assignment), quizResult = DataResult.Fail())
        val expectedState = AssignmentDetailsViewState.Error
        val actualState = AssignmentDetailsPresenter.present(model, context) as AssignmentDetailsViewState.Error

        assertEquals(expectedState, actualState)
    }

    @Test
    fun `Returns error state when assignment quiz is null`() {
        val assignment = baseAssignment.copy(
                submissionTypesRaw = listOf(Assignment.SubmissionType.ONLINE_QUIZ.apiString),
                quizId = baseQuiz.id
        )
        val model = baseModel.copy(assignmentResult = DataResult.Success(assignment), quizResult = null)
        val expectedState = AssignmentDetailsViewState.Error
        val actualState = AssignmentDetailsPresenter.present(model, context) as AssignmentDetailsViewState.Error

        assertEquals(expectedState, actualState)
    }

    @Test
    fun `Clears URL cache when routing from a URL`() {
        mockkObject(CanvasRestAdapter.Companion)
        every { CanvasRestAdapter.clearCacheUrls(any()) } returns Unit

        val route = Route(AssignmentDetailsFragment::class.java, Course())
        route.paramsHash[RouterParams.ASSIGNMENT_ID] = "123"

        AssignmentDetailsFragment.newInstance(route)

        verify { CanvasRestAdapter.clearCacheUrls("assignments/123") }
        confirmVerified(CanvasRestAdapter)

        unmockkObject(CanvasRestAdapter.Companion)
    }

    @Test
    fun `Hides submission status when on paper submission type`() {
        val allTypes = listOf(Assignment.SubmissionType.ON_PAPER)
        val assignment = baseAssignment.copy(submissionTypesRaw = allTypes.map { it.apiString })
        val model = baseModel.copy(assignmentResult = DataResult.Success(assignment))
        val state = AssignmentDetailsPresenter.present(model, context) as AssignmentDetailsViewState.Loaded
        assertFalse(state.visibilities.submissionStatus)
    }

    @Test
    fun `Shows submission status when on paper submission type with Grade`() {
        val allTypes = listOf(Assignment.SubmissionType.ON_PAPER)
        val submission = Submission(id = 1, grade = "A", score = 35.0, late = false, attempt = 1, missing = false, postedAt = Date())
        val assignment = baseAssignment.copy(submissionTypesRaw = allTypes.map { it.apiString }, submission = submission)
        val model = baseModel.copy(assignmentResult = DataResult.Success(assignment))
        val state = AssignmentDetailsPresenter.present(model, context) as AssignmentDetailsViewState.Loaded
        assertTrue(state.visibilities.submissionStatus)
    }

    @Test
    fun `Hides submission status when no submission type`() {
        val allTypes = listOf(Assignment.SubmissionType.NONE)
        val assignment = baseAssignment.copy(submissionTypesRaw = allTypes.map { it.apiString })
        val model = baseModel.copy(assignmentResult = DataResult.Success(assignment))
        val state = AssignmentDetailsPresenter.present(model, context) as AssignmentDetailsViewState.Loaded
        assertFalse(state.visibilities.submissionStatus)
    }

    @Test
    fun `Shows submission status when online submission type`() {
        val allTypes = listOf(
            Assignment.SubmissionType.ONLINE_UPLOAD,
            Assignment.SubmissionType.ONLINE_TEXT_ENTRY,
            Assignment.SubmissionType.ONLINE_URL,
            Assignment.SubmissionType.BASIC_LTI_LAUNCH,
            Assignment.SubmissionType.EXTERNAL_TOOL,
            Assignment.SubmissionType.ATTENDANCE,
            Assignment.SubmissionType.MEDIA_RECORDING
        )

        val assignment = baseAssignment.copy(
                submissionTypesRaw = allTypes.map { it.apiString }
        )
        val model = baseModel.copy(assignmentResult = DataResult.Success(assignment))
        val state = AssignmentDetailsPresenter.present(model, context) as AssignmentDetailsViewState.Loaded
        assertTrue(state.visibilities.submissionStatus)
    }

    @Test
    fun `Shows submission status when online submission type with Grade`() {
        val allTypes = listOf(
            Assignment.SubmissionType.ONLINE_UPLOAD,
            Assignment.SubmissionType.ONLINE_TEXT_ENTRY,
            Assignment.SubmissionType.ONLINE_URL,
            Assignment.SubmissionType.BASIC_LTI_LAUNCH,
            Assignment.SubmissionType.EXTERNAL_TOOL,
            Assignment.SubmissionType.ATTENDANCE,
            Assignment.SubmissionType.MEDIA_RECORDING
        )

        val submission = Submission(id = 1, grade = "A", score = 35.0, late = false, attempt = 1, missing = false)
        val assignment = baseAssignment.copy(
            submissionTypesRaw = allTypes.map { it.apiString },
            submission = submission
        )
        val model = baseModel.copy(assignmentResult = DataResult.Success(assignment))
        val state = AssignmentDetailsPresenter.present(model, context) as AssignmentDetailsViewState.Loaded
        assertTrue(state.visibilities.submissionStatus)
    }

    @Test
    fun `Shows submission status when discussion submission type`() {
        val allTypes = listOf(Assignment.SubmissionType.DISCUSSION_TOPIC)
        val assignment = baseAssignment.copy(
            submissionTypesRaw = allTypes.map { it.apiString },
            discussionTopicHeader = baseDiscussion
        )
        val model = baseModel.copy(assignmentResult = DataResult.Success(assignment))
        val state = AssignmentDetailsPresenter.present(model, context) as AssignmentDetailsViewState.Loaded
        assertTrue(state.visibilities.submissionStatus)
    }

    @Test
    fun `Shows submission status when quiz submission type`() {
        val allTypes = listOf(Assignment.SubmissionType.ONLINE_QUIZ)
        val assignment = baseAssignment.copy(
            submissionTypesRaw = allTypes.map { it.apiString },
            quizId = baseQuiz.id
        )
        val model = baseModel.copy(assignmentResult = DataResult.Success(assignment), quizResult = DataResult.Success(baseQuiz))
        val state = AssignmentDetailsPresenter.present(model, context) as AssignmentDetailsViewState.Loaded
        assertTrue(state.visibilities.submissionStatus)
    }

    @Test
    fun `Shows enabled submit button when assignment has no submission`() {
        val assignment = baseAssignment.copy(
            submissionTypesRaw = listOf("online_text_entry"),
            allowedAttempts = -1,
            submission = null
        )
        val model = baseModel.copy(assignmentResult = DataResult.Success(assignment))
        val actual = AssignmentDetailsPresenter.present(model, context).visibilities
        assertTrue(actual.submitButton)
        assertTrue(actual.submitButtonEnabled)
    }

    @Test
    fun `Shows enabled submit button when submissions are allowed with unlimited attempts`() {
        val assignment = baseAssignment.copy(
            submissionTypesRaw = listOf("online_text_entry"),
            allowedAttempts = -1
        )
        val model = baseModel.copy(assignmentResult = DataResult.Success(assignment))
        val actual = AssignmentDetailsPresenter.present(model, context).visibilities
        assertTrue(actual.submitButton)
        assertTrue(actual.submitButtonEnabled)
    }

    @Test
    fun `Shows enabled submit button when submissions attempts are less than allowed attempts`() {
        val assignment = baseAssignment.copy(
            submissionTypesRaw = listOf("online_text_entry"),
            allowedAttempts = 2,
            submission = baseSubmission.copy(attempt = 1)
        )
        val model = baseModel.copy(assignmentResult = DataResult.Success(assignment))
        val state = AssignmentDetailsPresenter.present(model, context) as AssignmentDetailsViewState.Loaded
        val actual = state.visibilities

        assertEquals("Resubmit Assignment", state.submitButtonText)
        assertTrue(actual.submitButton)
        assertTrue(actual.submitButtonEnabled)
    }

    @Test
    fun `Shows disabled submit button when submissions attempts reach allowed attempts`() {
        val assignment = baseAssignment.copy(
            submissionTypesRaw = listOf("online_text_entry"),
            allowedAttempts = 1,
            submission = baseSubmission.copy(attempt = 1)
        )
        val model = baseModel.copy(assignmentResult = DataResult.Success(assignment))
        val state = AssignmentDetailsPresenter.present(model, context) as AssignmentDetailsViewState.Loaded
        val actual = state.visibilities

        assertEquals("No attempts left", state.submitButtonText)
        assertTrue(actual.submitButton)
        assertFalse(actual.submitButtonEnabled)
    }

    @Test
    fun `Shows attempt details when assignment limits allowed attempts`() {
        val assignment = baseAssignment.copy(
            submissionTypesRaw = listOf("online_text_entry"),
            allowedAttempts = 1
        )
        val model = baseModel.copy(assignmentResult = DataResult.Success(assignment))
        val actual = AssignmentDetailsPresenter.present(model, context).visibilities
        assertTrue(actual.allowedAttempts)
    }

    @Test
    fun `Hides attempt details when assignment has unlimited allowed attempts`() {
        val assignment = baseAssignment.copy(
            submissionTypesRaw = listOf("online_text_entry"),
            allowedAttempts = -1
        )
        val model = baseModel.copy(assignmentResult = DataResult.Success(assignment))
        val actual = AssignmentDetailsPresenter.present(model, context).visibilities
        assertFalse(actual.allowedAttempts)
    }



    private val discussionHtml = "<!DOCTYPE html>\n" +
            "<html>\n" +
            "\t<head>\n" +
            "\t\t<meta name=\"viewport\" charset=\"utf-8\" content=\"width=device-width, initial-scale=1.0, maximum-scale=1.0\" />\n" +
            "\t\t<style>\n" +
            "\n" +
            "        \t.lti_button {\n" +
            "\t\t\t\tmargin-bottom: 12px;\n" +
            "\t\t\t\theight: 38px;\n" +
            "\t\t\t\twidth: 100%;\n" +
            "\t\t\t\tborder: 0.5px solid #C7CDD1;\n" +
            "\t\t\t\tborder-radius: 4px;\n" +
            "\t\t\t\tbackground-color: #F5F5F5;\n" +
            "\t\t\t\ttext-align: center;\n" +
            "\t\t\t\tvertical-align: middle;\n" +
            "\t\t\t\tline-height: 38px;\n" +
            "\t\t\t\tcolor: #394B58;\n" +
            "\t\t\t\tfont-size: 13px;\n" +
            "\t\t\t\tmargin: auto;\n" +
            "\t    \t}\n" +
            "\n" +
            "        \t/* makes the videos in fullscreen black */\n" +
            "            :-webkit-full-screen-ancestor:not(iframe) { background-color: black }\n" +
            "\n" +
            "\t\t</style>\n" +
            "\t</head>\n" +
            "\t<body>\n" +
            "\t\t<div id=\"header_content\">discussion message</div>\n" +
            "\t\t<script type=\"text/javascript\">\n" +
            "\n" +
            "\t\t\tfunction onLtiToolButtonPressed(id) {\n" +
            "\t\t\t\taccessor.onLtiToolButtonPressed(id);\n" +
            "\t\t\t\twindow.event.cancelBubble = true;\n" +
            "\t\t\t\twindow.event.stopPropagation();\n" +
            "\t\t\t}\n" +
            "\n" +
            "\t\t</script>\n" +
            "\t</body>\n" +
            "</html>"
}
