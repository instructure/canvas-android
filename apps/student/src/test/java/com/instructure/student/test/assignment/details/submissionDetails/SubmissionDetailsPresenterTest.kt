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
package com.instructure.student.test.assignment.details.submissionDetails

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Attachment
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.RubricCriterion
import com.instructure.canvasapi2.models.RubricCriterionAssessment
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.student.mobius.assignmentDetails.submissionDetails.SubmissionDetailsModel
import com.instructure.student.mobius.assignmentDetails.submissionDetails.SubmissionDetailsPresenter
import com.instructure.student.mobius.assignmentDetails.submissionDetails.ui.SubmissionDetailsTabData
import com.instructure.student.mobius.assignmentDetails.submissionDetails.ui.SubmissionDetailsViewState
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SubmissionDetailsPresenterTest : Assert() {

    private lateinit var context: Context
    private lateinit var baseModel: SubmissionDetailsModel
    private lateinit var baseAssignment: Assignment
    private lateinit var baseSubmission: Submission

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        baseModel = SubmissionDetailsModel(
            canvasContext = Course(),
            assignmentId = 123,
            selectedSubmissionAttempt = 1
        )
        baseAssignment = Assignment(
            id = 123,
            name = "Assignment Name",
            description = "This is a description",
            pointsPossible = 35.0
        )
        baseSubmission = Submission(
            attempt = 1,
            workflowState = "submitted"
        )
    }

    @Test
    fun `Returns Loading state when model is loading`() {
        val expectedState = SubmissionDetailsViewState.Loading
        val model = baseModel.copy(isLoading = true)
        val actualState = SubmissionDetailsPresenter.present(model, context)
        assertEquals(expectedState, actualState)
    }

    @Test
    fun `Returns failed state when assignment result is failed`() {
        val expectedState = SubmissionDetailsViewState.Error
        val model = baseModel.copy(assignmentResult = DataResult.Fail())
        val actualState = SubmissionDetailsPresenter.present(model, context)
        assertEquals(expectedState, actualState)
    }

    @Test
    fun `Returns failed state when submission result is failed`() {
        val expectedState = SubmissionDetailsViewState.Error
        val model = baseModel.copy(rootSubmissionResult = DataResult.Fail())
        val actualState = SubmissionDetailsPresenter.present(model, context)
        assertEquals(expectedState, actualState)
    }

    @Test
    fun `Returns loaded state when loading is false and results are valid`() {
        val submission = baseSubmission.copy(
            submissionHistory = listOf(baseSubmission)
        )
        val model = baseModel.copy(
            assignmentResult = DataResult.Success(baseAssignment),
            rootSubmissionResult = DataResult.Success(submission)
        )
        val actualState = SubmissionDetailsPresenter.present(model, context)
        assertTrue(actualState is SubmissionDetailsViewState.Loaded)
    }

    @Test
    fun `showVersionSpinner is true when there are multiple submission versions`() {
        val submission = baseSubmission.copy(
            submissionHistory = listOf(
                baseSubmission,
                baseSubmission.copy(
                    attempt = 1
                )
            )
        )
        val model = baseModel.copy(
            assignmentResult = DataResult.Success(baseAssignment),
            rootSubmissionResult = DataResult.Success(submission)
        )
        val actualState = SubmissionDetailsPresenter.present(model, context) as SubmissionDetailsViewState.Loaded
        assertTrue(actualState.showVersionsSpinner)
    }

    @Test
    fun `showVersionSpinner is false when there are not multiple submissions`() {
        val submission = baseSubmission.copy(
            submissionHistory = listOf(baseSubmission)
        )
        val model = baseModel.copy(
            assignmentResult = DataResult.Success(baseAssignment),
            rootSubmissionResult = DataResult.Success(submission)
        )
        val actualState = SubmissionDetailsPresenter.present(model, context) as SubmissionDetailsViewState.Loaded
        assertFalse(actualState.showVersionsSpinner)
    }

    @Test
    fun `Sorts submission versions by descending date`() {
        val firstSubmission = Submission(
            attempt = 1,
            submittedAt = DateHelper.makeDate(2050, 0, 30, 23, 59, 0)
        )
        val secondSubmission = Submission(
            attempt = 2,
            submittedAt = DateHelper.makeDate(2050, 0, 31, 23, 59, 0)
        )
        val model = baseModel.copy(
            assignmentResult = DataResult.Success(baseAssignment),
            rootSubmissionResult = DataResult.Success(
                Submission(submissionHistory = listOf(firstSubmission, secondSubmission))
            )
        )
        val viewState = SubmissionDetailsPresenter.present(model, context) as SubmissionDetailsViewState.Loaded
        val expectedVersions = arrayListOf(
            2L to "Jan 31, 2050, 11:59 PM",
            1L to "Jan 30, 2050, 11:59 PM"
        )
        val actualVersions = viewState.submissionVersions
        assertEquals(expectedVersions, actualVersions)
    }

    @Test
    fun `Has correct selected version index`() {
        val firstSubmission = Submission(
            attempt = 1,
            submittedAt = DateHelper.makeDate(2050, 0, 29, 23, 59, 0)
        )
        val secondSubmission = Submission(
            attempt = 2,
            submittedAt = DateHelper.makeDate(2050, 0, 30, 23, 59, 0)
        )
        val thirdSubmission = Submission(
            attempt = 3,
            submittedAt = DateHelper.makeDate(2050, 0, 31, 23, 59, 0)
        )
        val model = baseModel.copy(
            assignmentResult = DataResult.Success(baseAssignment),
            rootSubmissionResult = DataResult.Success(
                Submission(
                    submissionHistory = listOf(firstSubmission, secondSubmission, thirdSubmission)
                )
            ),
            selectedSubmissionAttempt = 2
        )
        val viewState = SubmissionDetailsPresenter.present(model, context) as SubmissionDetailsViewState.Loaded
        assertEquals(1, viewState.selectedVersionSpinnerIndex)
    }

    @Test
    fun `Shows correct Files tab name when there are no files`() {
        val model = baseModel.copy(
            assignmentResult = DataResult.Success(baseAssignment),
            rootSubmissionResult = DataResult.Success(
                Submission(submissionHistory = listOf(baseSubmission))
            )
        )
        val viewState = SubmissionDetailsPresenter.present(model, context) as SubmissionDetailsViewState.Loaded
        val actual = viewState.tabData.first { it is SubmissionDetailsTabData.FileData }.tabName
        assertEquals("Files", actual)
    }

    @Test
    fun `Shows correct Files tab name when there are files`() {
        val fileCount = 3
        val model = baseModel.copy(
            assignmentResult = DataResult.Success(baseAssignment),
            rootSubmissionResult = DataResult.Success(
                Submission(
                    submissionHistory = listOf(
                        baseSubmission.copy(attachments = ArrayList(List(fileCount) { Attachment() }))
                    )
                )
            )
        )
        val viewState = SubmissionDetailsPresenter.present(model, context) as SubmissionDetailsViewState.Loaded
        val actual = viewState.tabData.first { it is SubmissionDetailsTabData.FileData }.tabName
        assertEquals("Files ($fileCount)", actual)
    }

    @Test
    fun `Rubric tab name reads "Rubric" when assignment uses a rubric`() {
        val submission = baseSubmission.copy(
            rubricAssessment = hashMapOf("1" to RubricCriterionAssessment())
        )
        val model = baseModel.copy(
            assignmentResult = DataResult.Success(
                baseAssignment.copy(
                    rubric = listOf(RubricCriterion("1"))
                )
            ),
            rootSubmissionResult = DataResult.Success(
                submission.copy(submissionHistory = listOf(submission))
            )
        )
        val viewState = SubmissionDetailsPresenter.present(model, context) as SubmissionDetailsViewState.Loaded
        val expectedTab = SubmissionDetailsTabData.RubricData(
            "Rubric",
            model.assignmentResult!!.dataOrThrow,
            model.rootSubmissionResult!!.dataOrThrow
        )
        val actualTab = viewState.tabData.single { it is SubmissionDetailsTabData.RubricData }
        assertEquals(expectedTab, actualTab)
    }
}
