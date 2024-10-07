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
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.RubricCriterion
import com.instructure.canvasapi2.models.RubricCriterionAssessment
import com.instructure.canvasapi2.models.RubricCriterionRating
import com.instructure.canvasapi2.models.RubricSettings
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.pandautils.features.assignments.details.mobius.gradeCell.GradeCellViewState
import com.instructure.pandautils.utils.color
import com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.rubric.RatingData
import com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.rubric.RubricListData
import com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.rubric.SubmissionRubricModel
import com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.rubric.SubmissionRubricPresenter
import com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.rubric.SubmissionRubricViewState
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SubmissionRubricPresenterTest : Assert() {

    private val course = Course(id = 123L, name = "Test Course")
    private lateinit var context: Context
    private lateinit var modelTemplate: SubmissionRubricModel
    private lateinit var assignmentTemplate: Assignment
    private lateinit var submissionTemplate: Submission
    private lateinit var gradeTemplate: RubricListData.Grade
    private lateinit var criterionTemplate: RubricListData.Criterion

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        val courseColor = course.color // Define here so the color gets cached properly for all tests

        assignmentTemplate = Assignment(
            courseId = course.id,
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
                        RubricCriterionRating("_id1", "Rating 1 Title", "Rating 1 Description", 5.5),
                        RubricCriterionRating("_id2", "Rating 2 Title", "Rating 2 Description", 10.0),
                        RubricCriterionRating("_id3", "Rating 3 Title", "Rating 3 Description", 15.0)
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
                "123" to RubricCriterionAssessment("_id2", 10.0, "This is a comment")
            )
        )
        modelTemplate = SubmissionRubricModel(assignmentTemplate, submissionTemplate)
        gradeTemplate = RubricListData.Grade(
            GradeCellViewState.fromSubmission(context, assignmentTemplate, submissionTemplate)
        )
        criterionTemplate = RubricListData.Criterion(
            title = "Criterion description 1",
            ratingTitle = "Rating 2 Title",
            ratingDescription = "Rating 2 Description",
            ratings = listOf(
                RatingData("_id1", "5.5", isSelected = false, isAssessed = false),
                RatingData("_id2", "10", isSelected = true, isAssessed = true),
                RatingData("_id3", "15", isSelected = false, isAssessed = false)
            ),
            criterionId = "123",
            showDescriptionButton = true,
            comment = "This is a comment",
            tint = courseColor
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
                    title = "Criterion description 1",
                    ratingTitle = null,
                    ratingDescription = null,
                    ratings = listOf(
                        RatingData("_id1", "5.5", isSelected = false, isAssessed = false),
                        RatingData("_id2", "10", isSelected = false, isAssessed = false),
                        RatingData("_id3", "15", isSelected = false, isAssessed = false)
                    ),
                    criterionId = "123",
                    showDescriptionButton = true,
                    comment = null,
                    tint = course.color
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
                    ratingTitle = null,
                    ratingDescription = null,
                    ratings = listOf(
                        criterionTemplate.ratings[1].copy(
                            text = "10 / 15 pts"
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
                    ratingTitle = "Custom score",
                    ratingDescription = null,
                    comment = "Custom comment",
                    ratings = listOf(
                        RatingData("_id1", "5.5", isSelected = false, isAssessed = false),
                        RatingData(SubmissionRubricPresenter.customRatingId, "7", isSelected = true, isAssessed = true),
                        RatingData("_id2", "10", isSelected = false, isAssessed = false),
                        RatingData("_id3", "15", isSelected = false, isAssessed = false)
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
                    "123" to RubricCriterionAssessment("_id2", 10.0, "")
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
    fun `Returns correct state for empty rating title`() {
        val model = modelTemplate.copy(
            assignment = assignmentTemplate.copy(
                rubric = listOf(
                    assignmentTemplate.rubric!![0].copy(
                        ratings = mutableListOf(
                            RubricCriterionRating("_id1", "Rating 1 Title", "Rating 1 Description", 5.5),
                            RubricCriterionRating("_id2", null, "Rating 2 Description", 10.0),
                            RubricCriterionRating("_id3", "Rating 3 Title", "Rating 3 Description", 15.0)
                        )
                    )
                )
            )
        )
        val expectedState = SubmissionRubricViewState(
            listOf(
                gradeTemplate,
                criterionTemplate.copy(ratingTitle = null)
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
                            RubricCriterionRating("_id1", "Rating 1 Title", "Rating 1 Description", 5.5),
                            RubricCriterionRating("_id2", "Rating 2 Title", null, 10.0),
                            RubricCriterionRating("_id3", "Rating 3 Title", "Rating 3 Description", 15.0)
                        )
                    )
                )
            )
        )
        val expectedState = SubmissionRubricViewState(
            listOf(
                gradeTemplate,
                criterionTemplate.copy(ratingDescription = null)
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
                    ratings = listOf(
                        RatingData("_id1", "Rating 1 Title", isSelected = false, isAssessed = false, useSmallText = true),
                        RatingData("_id2", "Rating 2 Title", isSelected = true, isAssessed = true, useSmallText = true),
                        RatingData("_id3", "Rating 3 Title", isSelected = false, isAssessed = false, useSmallText = true)
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
                    ratingTitle = null,
                    ratingDescription = null,
                    ratings = emptyList()
                )
            )
        )
        val actualState = SubmissionRubricPresenter.present(model, context)
        assertEquals(expectedState, actualState)
    }

    @Test
    fun `Returns correct state for selected rating`() {
        val model = modelTemplate.copy(
            selectedRatingMap = mapOf("123" to "_id1")
        )
        val expectedState = SubmissionRubricViewState(
            listOf(
                gradeTemplate,
                criterionTemplate.copy(
                    ratingTitle = "Rating 1 Title",
                    ratingDescription = "Rating 1 Description",
                    ratings = listOf(
                        RatingData("_id1", "5.5", isSelected = true, isAssessed = false),
                        RatingData("_id2", "10", isSelected = false, isAssessed = true),
                        RatingData("_id3", "15", isSelected = false, isAssessed = false)
                    )
                )
            )
        )
        val actualState = SubmissionRubricPresenter.present(model, context)
        assertEquals(expectedState, actualState)
    }

    @Test
    fun `Returns correct state for partially graded rubric`() {
        val comment = "This is not yet graded"
        val model = modelTemplate.copy(
            submission = submissionTemplate.copy(
                rubricAssessment = hashMapOf(
                    "123" to RubricCriterionAssessment("_id2", null, comment)
                )
            )
        )
        val expectedState = SubmissionRubricViewState(
            listOf(
                gradeTemplate,
                criterionTemplate.copy(
                    ratingTitle = null,
                    ratingDescription = null,
                    comment = comment,
                    ratings = listOf(
                        RatingData("_id1", "5.5", isSelected = false, isAssessed = false),
                        RatingData("_id2", "10", isSelected = false, isAssessed = false),
                        RatingData("_id3", "15", isSelected = false, isAssessed = false)
                    )
                )
            )
        )
        val actualState = SubmissionRubricPresenter.present(model, context)
        assertEquals(expectedState, actualState)
    }

    @Test
    fun `Returns custom score for ranged rubric`() {
        val model = modelTemplate.copy(
            assignment = assignmentTemplate.copy(
                rubric = listOf(assignmentTemplate.rubric!![0].copy(criterionUseRange = true))
            ),
            submission = submissionTemplate.copy(
                rubricAssessment = hashMapOf(
                    "123" to RubricCriterionAssessment("_id3", 11.5, "Comment")
                )
            )
        )
        val expectedState = SubmissionRubricViewState(
            listOf(
                gradeTemplate,
                criterionTemplate.copy(
                    ratingTitle = "Rating 3 Title",
                    ratingDescription = "Rating 3 Description",
                    comment = "Comment",
                    ratings = listOf(
                        RatingData("_id1", "5.5", isSelected = false, isAssessed = false),
                        RatingData("_id2", "10", isSelected = false, isAssessed = false),
                        RatingData(
                            SubmissionRubricPresenter.customRatingId,
                            "11.5",
                            isSelected = true,
                            isAssessed = true
                        ),
                        RatingData("_id3", "15", isSelected = false, isAssessed = false)
                    )
                )
            )
        )
        val actualState = SubmissionRubricPresenter.present(model, context)
        assertEquals(expectedState, actualState)
    }

    @Test
    fun `Does not returns custom score for ranged rubric if score matches range value`() {
        val model = modelTemplate.copy(
            assignment = assignmentTemplate.copy(
                rubric = listOf(assignmentTemplate.rubric!![0].copy(criterionUseRange = true))
            ),
            submission = submissionTemplate.copy(
                rubricAssessment = hashMapOf(
                    "123" to RubricCriterionAssessment("_id3", 15.0, "Comment")
                )
            )
        )
        val expectedState = SubmissionRubricViewState(
            listOf(
                gradeTemplate,
                criterionTemplate.copy(
                    ratingTitle = "Rating 3 Title",
                    ratingDescription = "Rating 3 Description",
                    comment = "Comment",
                    ratings = listOf(
                        RatingData("_id1", "5.5", isSelected = false, isAssessed = false),
                        RatingData("_id2", "10", isSelected = false, isAssessed = false),
                        RatingData("_id3", "15", isSelected = true, isAssessed = true)
                    )
                )
            )
        )
        val actualState = SubmissionRubricPresenter.present(model, context)
        assertEquals(expectedState, actualState)
    }

    @Test
    fun `Returns correct state when quantitative data is restricted`() {
        val model = modelTemplate.copy(
            restrictQuantitativeData = true
        )
        val expectedState = SubmissionRubricViewState(
            listOf(
                RubricListData.Grade(GradeCellViewState.fromSubmission(context, assignmentTemplate, submissionTemplate, true)),
                criterionTemplate.copy(
                    ratings = listOf(
                        RatingData("_id1", "Rating 1 Title", isSelected = false, isAssessed = false, useSmallText = true),
                        RatingData("_id2", "Rating 2 Title", isSelected = true, isAssessed = true, useSmallText = true),
                        RatingData("_id3", "Rating 3 Title", isSelected = false, isAssessed = false, useSmallText = true)
                    )
                )
            )
        )
        val actualState = SubmissionRubricPresenter.present(model, context)
        assertEquals(expectedState, actualState)
    }
}
