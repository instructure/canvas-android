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
package com.instructure.student.test.assignment.details.submissionDetails.rubricTab

import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.RubricCriterion
import com.instructure.canvasapi2.models.Submission
import com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.rubric.SubmissionRubricEffect
import com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.rubric.SubmissionRubricEvent
import com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.rubric.SubmissionRubricModel
import com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.rubric.SubmissionRubricUpdate
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
                RubricCriterion(id = "123", longDescription = "Long Description 123"),
                RubricCriterion(id = "456", longDescription = "Long Description 456")
            )
        )
        val model = modelTemplate.copy(assignment = assignment)
        val event = SubmissionRubricEvent.LongDescriptionClicked("123")
        val expectedEffect = SubmissionRubricEffect.ShowLongDescription("Long Description 123")
        updateSpec.given(model)
            .whenEvent(event)
            .then(
                assertThatNext(
                    NextMatchers.hasNoModel(),
                    matchesEffects<SubmissionRubricModel, SubmissionRubricEffect>(expectedEffect)
                )
            )
    }


}
