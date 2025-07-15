/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.pandautils.features.speedgrader.grading.rubric

import androidx.lifecycle.SavedStateHandle
import com.instructure.canvasapi2.SubmissionRubricQuery
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.RubricCriterionAssessment
import com.instructure.canvasapi2.models.RubricSettings
import com.instructure.pandautils.features.speedgrader.grade.SpeedGraderGradingEventHandler
import com.instructure.pandautils.features.speedgrader.grade.rubric.RubricCriterion
import com.instructure.pandautils.features.speedgrader.grade.rubric.RubricRating
import com.instructure.pandautils.features.speedgrader.grade.rubric.SpeedGraderRubricRepository
import com.instructure.pandautils.features.speedgrader.grade.rubric.SpeedGraderRubricUiState
import com.instructure.pandautils.features.speedgrader.grade.rubric.SpeedGraderRubricViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SpeedGraderRubricViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()

    private val savedStateHandle: SavedStateHandle = mockk(relaxed = true)
    private val repository: SpeedGraderRubricRepository = mockk(relaxed = true)
    private val gradingEventHandler: SpeedGraderGradingEventHandler = mockk(relaxed = true)

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        every { savedStateHandle.get<Long>("assignmentId") } returns 1L
        every { savedStateHandle.get<Long>("submissionId") } returns 1L
        every { savedStateHandle.get<Long>("courseId") } returns 1L
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test(expected = IllegalArgumentException::class)
    fun `createViewModel should throw exception when assignmentId is missing`() {
        every { savedStateHandle.get<Long>("assignmentId") } returns null
        createViewModel()
    }

    @Test(expected = IllegalArgumentException::class)
    fun `createViewModel should throw exception when submissionId is missing`() {
        every { savedStateHandle.get<Long>("submissionId") } returns null
        createViewModel()
    }

    @Test(expected = IllegalArgumentException::class)
    fun `createViewModel should throw exception when courseId is missing`() {
        every { savedStateHandle.get<Long>("courseId") } returns null
        createViewModel()
    }

    @Test
    fun `data maps correctly`() = runTest {
        coEvery { repository.getAssignmentRubric(any(), any()) } returns createMockAssignment()
        coEvery { repository.getRubrics(any(), any()) } returns createMockRubrics()

        val expected = SpeedGraderRubricUiState(
            loading = false,
            onPointChanged = { _, _ -> },
            onRubricSelected = { _, _, _ -> },
            criterions = listOf(
                RubricCriterion(
                    id = "criterionId",
                    description = "Criterion Description",
                    longDescription = "Long Criterion Description",
                    points = 10.0,
                    ratings = listOf(
                        RubricRating(
                            id = "ratingId",
                            description = "Rating Description",
                            longDescription = "Long Rating Description",
                            points = 5.0
                        )
                    )
                ),
                RubricCriterion(
                    id = "criterionId2",
                    description = "Criterion Description 2",
                    longDescription = "Long Criterion Description 2",
                    points = 20.0,
                    ratings = listOf(
                        RubricRating(
                            id = "ratingId2",
                            description = "Rating Description 2",
                            longDescription = "Long Rating Description 2",
                            points = 10.0
                        )
                    )
                )
            ),
            assessments = mapOf(
                "criterionId" to RubricCriterionAssessment(
                    ratingId = "ratingId",
                    points = 5.0,
                    comments = "Good job"
                ),
                "criterionId2" to RubricCriterionAssessment(
                    ratingId = "ratingId2",
                    points = 3.0,
                    comments = "Needs improvement"
                )
            ),
            hidePoints = false,
        )

        val viewModel = createViewModel()

        val uiState = viewModel.uiState.first()

        assertEquals(expected.assessments, uiState.assessments)
        assertEquals(expected.criterions, uiState.criterions)
        assertEquals(expected.hidePoints, uiState.hidePoints)
    }

    @Test
    fun `error state on assignment error`() = runTest {
        coEvery {
            repository.getAssignmentRubric(
                any(),
                any()
            )
        } throws Exception("Assignment error")
        coEvery { repository.getRubrics(any(), any()) } returns createMockRubrics()

        val viewModel = createViewModel()

        val uiState = viewModel.uiState.first()

        assertEquals(true, uiState.error)
    }

    @Test
    fun `error state on rubric error`() = runTest {
        coEvery { repository.getAssignmentRubric(any(), any()) } returns createMockAssignment()
        coEvery { repository.getRubrics(any(), any()) } throws Exception("Rubric error")

        val viewModel = createViewModel()

        val uiState = viewModel.uiState.first()

        assertEquals(true, uiState.error)
    }

    @Test
    fun `save assessment`() = runTest {
        coEvery { repository.getAssignmentRubric(any(), any()) } returns createMockAssignment()
        coEvery { repository.getRubrics(any(), any()) } returns createMockRubrics()

        val viewModel = createViewModel()
        val uiState = viewModel.uiState.first()

        uiState.onRubricSelected(10.0, "criterionId", "ratingId")

        val updatedUiState = viewModel.uiState.first()

        val expected = mapOf(
            "criterionId" to RubricCriterionAssessment(
                ratingId = "ratingId",
                points = 10.0,
                comments = null
            ),
            "criterionId2" to RubricCriterionAssessment(
                ratingId = "ratingId2",
                points = 3.0,
                comments = "Needs improvement"
            )
        )

        assertEquals(
            expected,
            updatedUiState.assessments
        )
    }

    @Test
    fun `save point assessment`() = runTest {
        coEvery { repository.getAssignmentRubric(any(), any()) } returns createMockAssignment()
        coEvery { repository.getRubrics(any(), any()) } returns createMockRubrics()
        coEvery { repository.postSubmissionRubricAssessment(any(), any(), any(), any()) } returns mockk()

        val viewModel = createViewModel()
        val uiState = viewModel.uiState.first()

        uiState.onPointChanged(15.0, "criterionId")

        dispatcher.scheduler.advanceTimeBy(600)

        val updatedUiState = viewModel.uiState.first()

        val expected = mapOf(
            "criterionId" to RubricCriterionAssessment(
                ratingId = null,
                points = 15.0,
                comments = null
            ),
            "criterionId2" to RubricCriterionAssessment(
                ratingId = "ratingId2",
                points = 3.0,
                comments = "Needs improvement"
            )
        )

        assertEquals(
            expected,
            updatedUiState.assessments
        )

        coVerify {
            repository.postSubmissionRubricAssessment(
                1L,
                1L,
                1L,
                any()
            )
        }
    }

    @Test
    fun `update point on range rubric`() = runTest {
        coEvery { repository.getAssignmentRubric(any(), any()) } returns Assignment(
            rubric = listOf(
                com.instructure.canvasapi2.models.RubricCriterion(
                    id = "criterionId",
                    description = "Criterion Description",
                    longDescription = "Long Criterion Description",
                    points = 10.0,
                    ratings = mutableListOf(
                        com.instructure.canvasapi2.models.RubricCriterionRating(
                            id = "ratingId2",
                            description = "Rating Description 2",
                            longDescription = "Long Rating Description 2",
                            points = 10.0
                        ),
                        com.instructure.canvasapi2.models.RubricCriterionRating(
                            id = "ratingId",
                            description = "Rating Description",
                            longDescription = "Long Rating Description",
                            points = 5.0
                        )
                    ),
                    criterionUseRange = true
                ),
                com.instructure.canvasapi2.models.RubricCriterion(
                    id = "criterionId2",
                    description = "Criterion Description 2",
                    longDescription = "Long Criterion Description 2",
                    points = 20.0,
                    ratings = mutableListOf(
                        com.instructure.canvasapi2.models.RubricCriterionRating(
                            id = "ratingId2",
                            description = "Rating Description 2",
                            longDescription = "Long Rating Description 2",
                            points = 10.0
                        )
                    )
                )
            ),
            isUseRubricForGrading = true,
            pointsPossible = 100.0,
            rubricSettings = RubricSettings(
                hidePoints = false
            )
        )
        coEvery { repository.getRubrics(any(), any()) } returns createMockRubrics()
        coEvery { repository.postSubmissionRubricAssessment(any(), any(), any(), any()) } returns mockk()

        val viewModel = createViewModel()
        val uiState = viewModel.uiState.first()

        uiState.onPointChanged(12.0, "criterionId")

        dispatcher.scheduler.advanceTimeBy(600)

        val updatedUiState = viewModel.uiState.first()

        val expected = mapOf(
            "criterionId" to RubricCriterionAssessment(
                ratingId = "ratingId2",
                points = 12.0,
                comments = null
            ),
            "criterionId2" to RubricCriterionAssessment(
                ratingId = "ratingId2",
                points = 3.0,
                comments = "Needs improvement"
            )
        )

        assertEquals(
            expected,
            updatedUiState.assessments
        )

        coVerify {
            repository.postSubmissionRubricAssessment(
                1L,
                1L,
                1L,
                any()
            )
        }
    }

    @Test
    fun `update point over max on range rubric`() = runTest {
        coEvery { repository.getAssignmentRubric(any(), any()) } returns Assignment(
            rubric = listOf(
                com.instructure.canvasapi2.models.RubricCriterion(
                    id = "criterionId",
                    description = "Criterion Description",
                    longDescription = "Long Criterion Description",
                    points = 10.0,
                    ratings = mutableListOf(
                        com.instructure.canvasapi2.models.RubricCriterionRating(
                            id = "ratingId2",
                            description = "Rating Description 2",
                            longDescription = "Long Rating Description 2",
                            points = 10.0
                        ),
                        com.instructure.canvasapi2.models.RubricCriterionRating(
                            id = "ratingId",
                            description = "Rating Description",
                            longDescription = "Long Rating Description",
                            points = 5.0
                        )
                    ),
                    criterionUseRange = true
                ),
                com.instructure.canvasapi2.models.RubricCriterion(
                    id = "criterionId2",
                    description = "Criterion Description 2",
                    longDescription = "Long Criterion Description 2",
                    points = 20.0,
                    ratings = mutableListOf(
                        com.instructure.canvasapi2.models.RubricCriterionRating(
                            id = "ratingId2",
                            description = "Rating Description 2",
                            longDescription = "Long Rating Description 2",
                            points = 10.0
                        )
                    )
                )
            ),
            isUseRubricForGrading = true,
            pointsPossible = 100.0,
            rubricSettings = RubricSettings(
                hidePoints = false
            )
        )
        coEvery { repository.getRubrics(any(), any()) } returns createMockRubrics()
        coEvery { repository.postSubmissionRubricAssessment(any(), any(), any(), any()) } returns mockk()

        val viewModel = createViewModel()
        val uiState = viewModel.uiState.first()

        uiState.onPointChanged(22.0, "criterionId")

        dispatcher.scheduler.advanceTimeBy(600)

        val updatedUiState = viewModel.uiState.first()

        val expected = mapOf(
            "criterionId" to RubricCriterionAssessment(
                ratingId = "ratingId2",
                points = 22.0,
                comments = null
            ),
            "criterionId2" to RubricCriterionAssessment(
                ratingId = "ratingId2",
                points = 3.0,
                comments = "Needs improvement"
            )
        )

        assertEquals(
            expected,
            updatedUiState.assessments
        )

        coVerify {
            repository.postSubmissionRubricAssessment(
                1L,
                1L,
                1L,
                any()
            )
        }
    }

    @Test
    fun `reset rubric on error`() = runTest {
        coEvery { repository.getAssignmentRubric(any(), any()) } returns createMockAssignment()
        coEvery { repository.getRubrics(any(), any()) } returns createMockRubrics()
        coEvery { repository.postSubmissionRubricAssessment(any(), any(), any(), any()) } throws Exception("Network error")

        val viewModel = createViewModel()

        val expected = mapOf(
            "criterionId" to RubricCriterionAssessment(
                ratingId = "ratingId",
                points = 5.0,
                comments = "Good job"
            ),
            "criterionId2" to RubricCriterionAssessment(
                ratingId = "ratingId2",
                points = 3.0,
                comments = "Needs improvement"
            )
        )

        val uiState = viewModel.uiState.first()

        uiState.onPointChanged(15.0, "criterionId")

        dispatcher.scheduler.advanceTimeBy(600)

        val updatedUiState = viewModel.uiState.first()

        assertEquals(
            expected,
            updatedUiState.assessments
        )
    }

    private fun createMockAssignment(): Assignment {
        return Assignment(
            rubric = listOf(
                com.instructure.canvasapi2.models.RubricCriterion(
                    id = "criterionId",
                    description = "Criterion Description",
                    longDescription = "Long Criterion Description",
                    points = 10.0,
                    ratings = mutableListOf(
                        com.instructure.canvasapi2.models.RubricCriterionRating(
                            id = "ratingId",
                            description = "Rating Description",
                            longDescription = "Long Rating Description",
                            points = 5.0
                        )
                    )
                ),
                com.instructure.canvasapi2.models.RubricCriterion(
                    id = "criterionId2",
                    description = "Criterion Description 2",
                    longDescription = "Long Criterion Description 2",
                    points = 20.0,
                    ratings = mutableListOf(
                        com.instructure.canvasapi2.models.RubricCriterionRating(
                            id = "ratingId2",
                            description = "Rating Description 2",
                            longDescription = "Long Rating Description 2",
                            points = 10.0
                        )
                    )
                )
            ),
            isUseRubricForGrading = true,
            pointsPossible = 100.0,
            rubricSettings = RubricSettings(
                hidePoints = false
            )
        )
    }

    private fun createMockRubrics(): SubmissionRubricQuery.Data {
        return SubmissionRubricQuery.Data(
            submission = SubmissionRubricQuery.Submission(
                assignment = SubmissionRubricQuery.Assignment(
                    rubric = SubmissionRubricQuery.Rubric(
                        _id = "rubricId",
                        buttonDisplay = "Button Display",
                        criteriaCount = 2,
                        criteria = listOf(
                            SubmissionRubricQuery.Criterium(
                                _id = "criterionId",
                                description = "Criterion Description",
                                longDescription = "Long Criterion Description",
                                points = 10.0,
                                ratings = listOf(
                                    SubmissionRubricQuery.Rating(
                                        _id = "ratingId",
                                        description = "Rating Description",
                                        longDescription = "Long Rating Description",
                                        points = 5.0,
                                        rubricId = "rubricId"
                                    )
                                )
                            ),
                            SubmissionRubricQuery.Criterium(
                                _id = "criterionId2",
                                description = "Criterion Description 2",
                                longDescription = "Long Criterion Description 2",
                                points = 20.0,
                                ratings = listOf(
                                    SubmissionRubricQuery.Rating(
                                        _id = "ratingId2",
                                        description = "Rating Description 2",
                                        longDescription = "Long Rating Description 2",
                                        points = 10.0,
                                        rubricId = "rubricId"
                                    )
                                )
                            )
                        )
                    )
                ),
                rubricAssessmentsConnection = SubmissionRubricQuery.RubricAssessmentsConnection(
                    pageInfo = SubmissionRubricQuery.PageInfo(
                        null,
                        null,
                        hasNextPage = false,
                        hasPreviousPage = false
                    ),
                    edges = listOf(
                        SubmissionRubricQuery.Edge(
                            node = SubmissionRubricQuery.Node(
                                _id = "assessmentId",
                                score = 8.0,
                                assessmentRatings = listOf(
                                    SubmissionRubricQuery.AssessmentRating(
                                        _id = "ratingId",
                                        points = 5.0,
                                        comments = "Good job",
                                        commentsHtml = null,
                                        commentsEnabled = true,
                                        description = "Rating Description 1",
                                        criterion = SubmissionRubricQuery.Criterion(
                                            _id = "criterionId",
                                            description = "Criterion Description",
                                            longDescription = "Long Criterion Description",
                                            points = 10.0,
                                            learningOutcomeId = null,
                                            ratings = listOf(
                                                SubmissionRubricQuery.Rating1(
                                                    _id = "ratingId",
                                                    description = "Rating Description",
                                                    longDescription = "Long Rating Description",
                                                    points = 5.0
                                                )
                                            )
                                        )
                                    ),
                                    SubmissionRubricQuery.AssessmentRating(
                                        _id = "ratingId2",
                                        points = 3.0,
                                        comments = "Needs improvement",
                                        commentsHtml = null,
                                        commentsEnabled = true,
                                        description = "Rating Description 2",
                                        criterion = SubmissionRubricQuery.Criterion(
                                            _id = "criterionId2",
                                            description = "Criterion Description 2",
                                            longDescription = "Long Criterion Description 2",
                                            points = 5.0,
                                            learningOutcomeId = null,
                                            ratings = listOf(
                                                SubmissionRubricQuery.Rating1(
                                                    _id = "ratingId2",
                                                    description = "Rating Description 2",
                                                    longDescription = "Long Rating Description 2",
                                                    points = 10.0,
                                                )
                                            )
                                        )
                                    )
                                ),
                                artifactAttempt = 1
                            )
                        )
                    )
                )
            )
        )
    }

    private fun createViewModel(): SpeedGraderRubricViewModel {
        return SpeedGraderRubricViewModel(
            savedStateHandle,
            repository,
            gradingEventHandler
        )
    }
}