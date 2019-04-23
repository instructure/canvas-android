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
import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.student.R
import com.instructure.student.mobius.assignmentDetails.AssignmentDetailsModel
import com.instructure.student.mobius.assignmentDetails.AssignmentDetailsPresenter
import com.instructure.student.mobius.assignmentDetails.ui.AssignmentDetailsViewState
import com.instructure.student.mobius.assignmentDetails.ui.AssignmentDetailsVisibilities
import com.instructure.canvasapi2.utils.isRtl
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class AssignmentDetailsPresenterTest : Assert() {

    private lateinit var context: Context
    private lateinit var baseModel: AssignmentDetailsModel
    private lateinit var baseAssignment: Assignment
    private lateinit var baseVisibilities: AssignmentDetailsVisibilities
    private lateinit var baseSubmission: Submission

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
            description = true,
            submissionAndRubricButton = true
        )
        baseSubmission = Submission(
            attempt = 1,
            workflowState = "submitted"
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
            lockExplanation = lockMessage
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
        val expected = baseVisibilities.copy(fileTypes = false)
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
        val assignment = baseAssignment.copy(
            submission = baseSubmission.copy(workflowState = "graded")
        )
        val model = baseModel.copy(assignmentResult = DataResult.Success(assignment))
        val state = AssignmentDetailsPresenter.present(model, context) as AssignmentDetailsViewState.Loaded
        assertEquals("Submitted", state.submittedStateLabel)
    }

    @Test
    fun `Uses correct label text for submitted status when submission is graded`() {
        val assignment = baseAssignment.copy(submission = baseSubmission)
        val model = baseModel.copy(assignmentResult = DataResult.Success(assignment))
        val state = AssignmentDetailsPresenter.present(model, context) as AssignmentDetailsViewState.Loaded
        assertEquals("Submitted", state.submittedStateLabel)
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
        val expectedDueDate = "This assignment doesn't have a due date."
        val assignment = baseAssignment.copy(dueAt = null)
        val model = baseModel.copy(assignmentResult = DataResult.Success(assignment))
        val state = AssignmentDetailsPresenter.present(model, context) as AssignmentDetailsViewState.Loaded
        assertEquals(expectedDueDate, state.dueDate)
    }

    @Test
    fun `Formats submission types correctly`() {
        val allTypes = Assignment.SubmissionType.values()
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
}
