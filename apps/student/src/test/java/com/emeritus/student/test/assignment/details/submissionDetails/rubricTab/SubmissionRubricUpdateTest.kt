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
 */
package com.emeritus.student.test.assignment.details.submissionDetails.rubricTab

import com.instructure.canvasapi2.models.*
import com.emeritus.student.mobius.assignmentDetails.submissionDetails.drawer.rubric.SubmissionRubricEffect
import com.emeritus.student.mobius.assignmentDetails.submissionDetails.drawer.rubric.SubmissionRubricEvent
import com.emeritus.student.mobius.assignmentDetails.submissionDetails.drawer.rubric.SubmissionRubricModel
import com.emeritus.student.mobius.assignmentDetails.submissionDetails.drawer.rubric.SubmissionRubricUpdate
import com.instructure.student.test.util.matchesEffects
import com.spotify.mobius.test.FirstMatchers
import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.InitSpec.assertThatFirst
import com.spotify.mobius.test.NextMatchers
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class SubmissionRubricUpdateTest : Assert() {

    private val initSpec = InitSpec(SubmissionRubricUpdate()::init)
    private val updateSpec = UpdateSpec(SubmissionRubricUpdate()::update)

    private lateinit var assignmentTemplate: Assignment
    private lateinit var submissionTemplate: Submission
    private lateinit var modelTemplate: SubmissionRubricModel

    @Before
    fun setup() {
        assignmentTemplate = Assignment()
        submissionTemplate = Submission()
        modelTemplate = SubmissionRubricModel(assignmentTemplate, submissionTemplate)
    }

    @Test
    fun `Initializes without change`() {
        initSpec
            .whenInit(modelTemplate)
            .then(
                assertThatFirst(
                    FirstMatchers.hasModel(modelTemplate),
                    FirstMatchers.hasNoEffects()
                )
            )
    }

    @Test
    fun `LongDescriptionClicked event produces ShowLongDescription effect`() {
        val assignment = assignmentTemplate.copy(
            rubric = listOf(
                RubricCriterion(id = "123", description = "Criterion 1", longDescription = "Long Description 123"),
                RubricCriterion(id = "456", description = "Criterion 2", longDescription = "Long Description 456")
            )
        )
        val model = modelTemplate.copy(assignment = assignment)
        val event = SubmissionRubricEvent.LongDescriptionClicked("123")
        val expectedEffect = SubmissionRubricEffect.ShowLongDescription("Criterion 1", "Long Description 123")
        updateSpec.given(model)
            .whenEvent(event)
            .then(
                assertThatNext(
                    NextMatchers.hasNoModel(),
                    matchesEffects<SubmissionRubricModel, SubmissionRubricEffect>(expectedEffect)
                )
            )
    }

    @Test
    fun `RatingClicked event updates selected rating`() {
        val assignment = Assignment(
            rubric = listOf(
                RubricCriterion(
                    id = "123",
                    ratings = mutableListOf(
                        RubricCriterionRating("_id1", "Rating 1 Title", "Rating 1 Description", 5.5),
                        RubricCriterionRating("_id2", "Rating 2 Title", "Rating 2 Description", 10.0)
                    )
                )
            )
        )
        val submission = Submission(
            rubricAssessment = hashMapOf(
                "123" to RubricCriterionAssessment("_id2", 10.0, "This is a comment")
            )
        )
        val model = SubmissionRubricModel(
            assignment = assignment,
            submission = submission,
            selectedRatingMap = mapOf("123" to "_id2")
        )
        val event = SubmissionRubricEvent.RatingClicked("123", "_id1")
        val expectedModel = model.copy(
            selectedRatingMap = mapOf("123" to "_id1")
        )
        updateSpec.given(model)
            .whenEvent(event)
            .then(
                assertThatNext(
                    NextMatchers.hasModel(expectedModel),
                    NextMatchers.hasNoEffects()
                )
            )
    }

    @Test
    fun `RatingClicked event un-selects the selected rating`() {
        val assignment = Assignment(
            rubric = listOf(
                RubricCriterion(
                    id = "123",
                    ratings = mutableListOf(
                        RubricCriterionRating("_id1", "Rating 1 Title", "Rating 1 Description", 5.5),
                        RubricCriterionRating("_id2", "Rating 2 Title", "Rating 2 Description", 10.0)
                    )
                )
            )
        )
        val submission = Submission(
            rubricAssessment = hashMapOf(
                "123" to RubricCriterionAssessment("_id2", 10.0, "This is a comment")
            )
        )
        val model = SubmissionRubricModel(
            assignment = assignment,
            submission = submission,
            selectedRatingMap = mapOf("123" to "_id2")
        )
        val event = SubmissionRubricEvent.RatingClicked("123", "_id2")
        val expectedModel = model.copy(
            selectedRatingMap = emptyMap()
        )
        updateSpec.given(model)
            .whenEvent(event)
            .then(
                assertThatNext(
                    NextMatchers.hasModel(expectedModel),
                    NextMatchers.hasNoEffects()
                )
            )
    }


}
