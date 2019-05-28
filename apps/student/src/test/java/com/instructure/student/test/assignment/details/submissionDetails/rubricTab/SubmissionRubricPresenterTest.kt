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
package com.instructure.student.test.assignment.details.submissionDetails.rubricTab

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.rubric.*
import com.instructure.student.mobius.assignmentDetails.ui.gradeCell.GradeCellViewState
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SubmissionRubricPresenterTest : Assert() {

    private lateinit var context: Context
    private lateinit var modelTemplate: SubmissionRubricModel
    private lateinit var assignmentTemplate: Assignment
    private lateinit var submissionTemplate: Submission
    private lateinit var gradeTemplate: RubricListData.Grade
    private lateinit var criterionTemplate: RubricListData.Criterion

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        assignmentTemplate = Assignment(
            freeFormCriterionComments = false,
            isUseRubricForGrading = true,
            pointsPossible = 15.0,
            gradingType = Assignment.POINTS_TYPE,
            rubric = listOf(
                RubricCriterion(
                    id = "123",
                    description = "Criterion description 1",
                    longDescription = "This is a long description for criterion 1",
                    points = 15.0,
                    ratings = mutableListOf(
                        RubricCriterionRating("rating1", "Rating 1 Description", 5.5),
                        RubricCriterionRating("rating2", "Rating 2 Description", 10.0),
                        RubricCriterionRating("rating3", "Rating 3 Description", 15.0)
                    )
                )
            )
        )
        submissionTemplate = Submission(
            attempt = 1L,
            submittedAt = DateHelper.makeDate(2017, 6, 27, 18, 47, 0),
            workflowState = "graded",
            enteredGrade = "10",
            enteredScore = 10.0,
            grade = "10",
            score = 10.0,
            rubricAssessment = hashMapOf(
                "123" to RubricCriterionAssessment("rating2", 10.0, "This is a comment")
            )
        )
        modelTemplate = SubmissionRubricModel(assignmentTemplate, submissionTemplate)
        gradeTemplate = RubricListData.Grade(
            GradeCellViewState.fromSubmission(context, assignmentTemplate, submissionTemplate)
        )
        criterionTemplate = RubricListData.Criterion(
            description = "Criterion description 1",
            ratingDescription = "Rating 2 Description",
            ratings = listOf(
                RatingData("5.5", "Rating 1 Description", false),
                RatingData("10", "Rating 2 Description", true),
                RatingData("15", "Rating 3 Description", false)
            ),
            criterionId = "123",
            showLongDescriptionButton = true,
            comment = "This is a comment"
        )
    }

    @Test
    fun `Returns empty state when there is no rubric`() {
        val model = modelTemplate.copy(
            assignment = assignmentTemplate.copy(
                rubric = emptyList()
            )
        )
        val expectedState = SubmissionRubricViewState(
            listOf(RubricListData.Empty)
        )
        val actualState = SubmissionRubricPresenter.present(model, context)
        assertEquals(expectedState, actualState)
    }

    @Test
    fun `Returns correct state for graded submission graded by rubric`() {
        val model = modelTemplate
        val expectedState = SubmissionRubricViewState(
            listOf(
                gradeTemplate,
                criterionTemplate
            )
        )
        val actualState = SubmissionRubricPresenter.present(model, context)
        assertEquals(expectedState, actualState)
    }

    @Test
    fun `Returns correct state for graded submission not graded by rubric`() {
        val model = modelTemplate.copy(
            assignment = assignmentTemplate.copy(
                isUseRubricForGrading = false
            )
        )
        val expectedState = SubmissionRubricViewState(
            listOf(criterionTemplate)
        )
        val actualState = SubmissionRubricPresenter.present(model, context)
        assertEquals(expectedState, actualState)
    }

    @Test
    fun `Returns correct state for ungraded submission`() {
        val model = modelTemplate.copy(
            submission = Submission(
                workflowState = "unsubmitted"
            )
        )
        val expectedState = SubmissionRubricViewState(
            listOf(
                RubricListData.Criterion(
                    description = "Criterion description 1",
                    ratingDescription = null,
                    ratings = listOf(
                        RatingData("5.5", "Rating 1 Description", false),
                        RatingData("10", "Rating 2 Description", false),
                        RatingData("15", "Rating 3 Description", false)
                    ),
                    criterionId = "123",
                    showLongDescriptionButton = true,
                    comment = null
                )
            )
        )
        val actualState = SubmissionRubricPresenter.present(model, context)
        assertEquals(expectedState, actualState)
    }

    @Test
    fun `Returns correct state for free-form rubric`() {
        val model = modelTemplate.copy(
            assignment = assignmentTemplate.copy(
                freeFormCriterionComments = true
            )
        )
        val expectedState = SubmissionRubricViewState(
            listOf(
                gradeTemplate,
                criterionTemplate.copy(
                    ratingDescription = null,
                    ratings = listOf(
                        criterionTemplate.ratings[1].copy(
                            description = null,
                            points = "10 / 15 pts"
                        )
                    )
                )
            )
        )
        val actualState = SubmissionRubricPresenter.present(model, context)
        assertEquals(expectedState, actualState)
    }


    @Test
    fun `Returns correct state for custom assessment`() {
        val model = modelTemplate.copy(
            submission = submissionTemplate.copy(
                rubricAssessment = hashMapOf(
                    "123" to RubricCriterionAssessment(null, 7.0, "Custom comment")
                )
            )
        )
        val expectedState = SubmissionRubricViewState(
            listOf(
                gradeTemplate,
                criterionTemplate.copy(
                    ratingDescription = "Custom score",
                    comment = "Custom comment",
                    ratings = listOf(
                        RatingData("5.5", "Rating 1 Description", false),
                        RatingData("7", "Custom score", true),
                        RatingData("10", "Rating 2 Description", false),
                        RatingData("15", "Rating 3 Description", false)
                    )
                )
            )
        )
        val actualState = SubmissionRubricPresenter.present(model, context)
        assertEquals(expectedState, actualState)
    }

    @Test
    fun `Returns correct state for rubric assessment without comment`() {
        val model = modelTemplate.copy(
            submission = submissionTemplate.copy(
                rubricAssessment = hashMapOf(
                    "123" to RubricCriterionAssessment("rating2", 10.0, "")
                )
            )
        )
        val expectedState = SubmissionRubricViewState(
            listOf(
                gradeTemplate,
                criterionTemplate.copy(comment = null)
            )
        )
        val actualState = SubmissionRubricPresenter.present(model, context)
        assertEquals(expectedState, actualState)
    }

    @Test
    fun `Returns correct state for empty rating description`() {
        val model = modelTemplate.copy(
            assignment = assignmentTemplate.copy(
                rubric = listOf(
                    assignmentTemplate.rubric!![0].copy(
                        ratings = mutableListOf(
                            RubricCriterionRating("rating1", "Rating 1 Description", 5.5),
                            RubricCriterionRating("rating2", null, 10.0),
                            RubricCriterionRating("rating3", "Rating 3 Description", 15.0)
                        )
                    )
                )
            )
        )
        val expectedState = SubmissionRubricViewState(
            listOf(
                gradeTemplate,
                criterionTemplate.copy(
                    ratingDescription = null,
                    ratings = listOf(
                        RatingData("5.5", "Rating 1 Description", false),
                        RatingData("10", null, true),
                        RatingData("15", "Rating 3 Description", false)
                    )
                )
            )
        )
        val actualState = SubmissionRubricPresenter.present(model, context)
        assertEquals(expectedState, actualState)
    }

    @Test
    fun `Returns correct state when rubric does not include points`() {
        val model = modelTemplate.copy(
            assignment = assignmentTemplate.copy(
                rubricSettings = RubricSettings(hidePoints = true)
            )
        )
        val expectedState = SubmissionRubricViewState(
            listOf(
                gradeTemplate,
                criterionTemplate.copy(
                    ratingDescription = null,
                    ratings = listOf(
                        RatingData("Rating 1 Description", null, isSelected = false, useSmallText = true),
                        RatingData("Rating 2 Description", null, isSelected = true, useSmallText = true),
                        RatingData("Rating 3 Description", null, isSelected = false, useSmallText = true)
                    )
                )
            )
        )
        val actualState = SubmissionRubricPresenter.present(model, context)
        assertEquals(expectedState, actualState)
    }

    @Test
    fun `Returns correct state for free-form rubric that does not include points`() {
        val model = modelTemplate.copy(
            assignment = assignmentTemplate.copy(
                freeFormCriterionComments = true,
                rubricSettings = RubricSettings(hidePoints = true)
            )
        )
        val expectedState = SubmissionRubricViewState(
            listOf(
                gradeTemplate,
                criterionTemplate.copy(
                    ratingDescription = null,
                    ratings = emptyList()
                )
            )
        )
        val actualState = SubmissionRubricPresenter.present(model, context)
        assertEquals(expectedState, actualState)
    }
}
