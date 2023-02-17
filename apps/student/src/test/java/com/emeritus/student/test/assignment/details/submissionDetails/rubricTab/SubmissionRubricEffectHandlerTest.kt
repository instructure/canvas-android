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

import com.emeritus.student.mobius.assignmentDetails.submissionDetails.drawer.rubric.SubmissionRubricEffect
import com.emeritus.student.mobius.assignmentDetails.submissionDetails.drawer.rubric.SubmissionRubricEffectHandler
import com.emeritus.student.mobius.assignmentDetails.submissionDetails.drawer.rubric.SubmissionRubricEvent
import com.emeritus.student.mobius.assignmentDetails.submissionDetails.drawer.rubric.ui.SubmissionRubricView
import com.spotify.mobius.functions.Consumer
import io.mockk.confirmVerified
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert
import org.junit.Test

class SubmissionRubricEffectHandlerTest : Assert() {
    private val view: SubmissionRubricView = mockk(relaxed = true)
    private val effectHandler =
        SubmissionRubricEffectHandler().apply { view = this@SubmissionRubricEffectHandlerTest.view }
    private val eventConsumer: Consumer<SubmissionRubricEvent> = mockk(relaxed = true)
    private val connection = effectHandler.connect(eventConsumer)

    @Test
    fun `ShowLongDescription results in view calling displayLongDescription`() {
        val description = "Description 123"
        val longDescription = "Long Description 123"

        connection.accept(SubmissionRubricEffect.ShowLongDescription(description, longDescription))

        verify(timeout = 100) {
            view.displayCriterionDescription(description, longDescription)
        }

        confirmVerified(view)
    }

}
