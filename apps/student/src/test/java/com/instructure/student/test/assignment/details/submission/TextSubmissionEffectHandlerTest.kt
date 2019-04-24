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
package com.instructure.student.test.assignment.details.submission

import com.instructure.canvasapi2.models.Course
import com.instructure.student.mobius.assignmentDetails.submission.text.TextSubmissionEffect
import com.instructure.student.mobius.assignmentDetails.submission.text.TextSubmissionEffectHandler
import com.instructure.student.mobius.assignmentDetails.submission.text.TextSubmissionEvent
import com.instructure.student.mobius.assignmentDetails.submission.text.ui.TextSubmissionView
import com.spotify.mobius.functions.Consumer
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.test.setMain
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.util.concurrent.Executors

class TextSubmissionEffectHandlerTest : Assert() {
    private val view: TextSubmissionView = mockk(relaxed = true)
    private val eventConsumer: Consumer<TextSubmissionEvent> = mockk(relaxed = true)
    private val effectHandler = TextSubmissionEffectHandler()
    private val connection = effectHandler.connect(eventConsumer)

    @ExperimentalCoroutinesApi
    @Before
    fun setup() {
        effectHandler.view = view
        Dispatchers.setMain(Executors.newSingleThreadExecutor().asCoroutineDispatcher())
    }

    @Test
    fun `SubmitText results in submission`() {
        val text = "Some text here"
        val assignmentId = 1234L
        val assignmentName = "Name"
        val course = Course()

        connection.accept(TextSubmissionEffect.SubmitText(text, course, assignmentId, assignmentName))

        verify(timeout = 100) {
            view.onTextSubmitted(text, course, assignmentId, assignmentName)
        }

        confirmVerified(view)
    }

    @Test
    fun `SubmitText with no view does not crash`() {
        val text = "Some text here"
        val assignmentId = 1234L
        val assignmentName = "Name"
        val course = Course()
        effectHandler.view = null

        connection.accept(TextSubmissionEffect.SubmitText(text, course, assignmentId, assignmentName))

        verify(exactly = 0) {
            view.onTextSubmitted(any(), any(), any(), any())
        }

        confirmVerified(view)
    }

    @Test
    fun `InitializeText results in view calling setInitialSubmissionText`() {
        val text = "Some text"
        connection.accept(TextSubmissionEffect.InitializeText(text))

        verify(timeout = 100) {
            view.setInitialSubmissionText(text)
        }

        confirmVerified(view)
    }

    @Test
    fun `InitializeText with no view does not crash`() {
        val text = "Some text"
        effectHandler.view = null

        connection.accept(TextSubmissionEffect.InitializeText(text))

        verify(exactly = 0) {
            view.setInitialSubmissionText(any())
        }

        confirmVerified(view)
    }
}
