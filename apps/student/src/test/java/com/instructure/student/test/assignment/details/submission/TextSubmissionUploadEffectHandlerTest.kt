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

import android.net.Uri
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.student.mobius.assignmentDetails.submission.text.TextSubmissionUploadEffect
import com.instructure.student.mobius.assignmentDetails.submission.text.TextSubmissionUploadEffectHandler
import com.instructure.student.mobius.assignmentDetails.submission.text.TextSubmissionUploadEvent
import com.instructure.student.mobius.assignmentDetails.submission.text.ui.TextSubmissionUploadView
import com.instructure.student.mobius.common.ui.SubmissionHelper
import com.spotify.mobius.functions.Consumer
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.test.setMain
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.util.concurrent.Executors

class TextSubmissionUploadEffectHandlerTest : Assert() {
    private val view: TextSubmissionUploadView = mockk(relaxed = true)
    private val eventConsumer: Consumer<TextSubmissionUploadEvent> = mockk(relaxed = true)
    private val submissionHelper: SubmissionHelper = mockk(relaxed = true)
    private val effectHandler = TextSubmissionUploadEffectHandler(submissionHelper)
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

        connection.accept(TextSubmissionUploadEffect.SubmitText(text, course, assignmentId, assignmentName, 1L))

        verify(timeout = 100) {
            submissionHelper.startTextSubmission(course, assignmentId, assignmentName, text, 1L)
            view.goBack()
        }

        confirmVerified(view)
    }

    @Test
    fun `InitializeText results in view calling setInitialSubmissionText`() {
        val text = "Some text"
        connection.accept(TextSubmissionUploadEffect.InitializeText(text))

        verify(timeout = 100) {
            view.setInitialSubmissionText(text)
        }

        confirmVerified(view)
    }

    @Test
    fun `InitializeText with no view does not crash`() {
        val text = "Some text"
        effectHandler.view = null

        connection.accept(TextSubmissionUploadEffect.InitializeText(text))

        verify(exactly = 0) {
            view.setInitialSubmissionText(any())
        }

        confirmVerified(view)
    }

    @Test
    fun `AddImage results in view calling addImageToSubmission`() {
        val uri = mockk<Uri>()
        val canvasContext = CanvasContext.emptyCourseContext(0)

        connection.accept(TextSubmissionUploadEffect.AddImage(uri, canvasContext))

        verify(timeout = 100) {
            view.addImageToSubmission(uri, canvasContext)
        }

        confirmVerified(view)
    }

    @Test
    fun `ShowFailedImageMessage results in view calling showFailedImageMessage`() {
        connection.accept(TextSubmissionUploadEffect.ShowFailedImageMessage)

        verify(timeout = 100) {
            view.showFailedImageMessage()
        }

        confirmVerified(view)
    }

    @Test
    fun `ProcessCameraImage results in ImageAdded event when the view returns a uri`() {
        val uri = mockk<Uri>()

        every { view.retrieveCameraImage() } returns uri

        connection.accept(TextSubmissionUploadEffect.ProcessCameraImage)

        verify(timeout = 100) {
            eventConsumer.accept(TextSubmissionUploadEvent.ImageAdded(uri))
        }

        confirmVerified(eventConsumer)
    }

    @Test
    fun `ProcessCameraImage results in ImageFailed event when the view returns a null uri`() {
        every { view.retrieveCameraImage() } returns null

        connection.accept(TextSubmissionUploadEffect.ProcessCameraImage)

        verify(timeout = 100) {
            eventConsumer.accept(TextSubmissionUploadEvent.ImageFailed)
        }

        confirmVerified(eventConsumer)
    }
}
