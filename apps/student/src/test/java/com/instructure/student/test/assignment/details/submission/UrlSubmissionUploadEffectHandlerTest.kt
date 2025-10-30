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

import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Course
import com.instructure.student.mobius.assignmentDetails.submission.url.UrlSubmissionUploadEffect
import com.instructure.student.mobius.assignmentDetails.submission.url.UrlSubmissionUploadEffectHandler
import com.instructure.student.mobius.assignmentDetails.submission.url.UrlSubmissionUploadEvent
import com.instructure.student.mobius.assignmentDetails.submission.url.ui.UrlSubmissionUploadView
import com.instructure.student.mobius.common.ui.SubmissionHelper
import com.spotify.mobius.functions.Consumer
import io.mockk.confirmVerified
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.test.setMain
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.util.concurrent.Executors

class UrlSubmissionUploadEffectHandlerTest : Assert() {

    private val view: UrlSubmissionUploadView = mockk(relaxed = true)
    private val eventConsumer: Consumer<UrlSubmissionUploadEvent> = mockk(relaxed = true)
    private val submissionHelper: SubmissionHelper = mockk(relaxed = true)
    private val effectHandler = UrlSubmissionUploadEffectHandler(submissionHelper)
    private val connection = effectHandler.connect(eventConsumer)

    @Before
    fun setup() {
        effectHandler.view = view
        Dispatchers.setMain(Executors.newSingleThreadExecutor().asCoroutineDispatcher())
    }

    @Test
    fun `ShowUrlPreview effect calls showPreviewUrl on view`() {
        val url = "www.instructure.com"
        connection.accept(UrlSubmissionUploadEffect.ShowUrlPreview(url))

        verify(timeout = 100) {
            view.showPreviewUrl(url)
        }

        confirmVerified(view)
    }

    @Test
    fun `SubmitUrl effect calls onSubmitUrl on view`() {
        val assignment = Assignment()
        val course = Course()
        val url = "www.instructure.com"

        connection.accept(UrlSubmissionUploadEffect.SubmitUrl(url, course, assignment.id, assignment.name, 1L))

        verify(timeout = 100) {
            submissionHelper.startUrlSubmission(course, assignment.id, assignment.name, url, 1L)
        }

        confirmVerified(submissionHelper)
    }

    @Test
    fun `InitializeUrl effect calls setInitialUrl on view`() {
        val url = "www.instructure.com"
        connection.accept(UrlSubmissionUploadEffect.InitializeUrl(url))

        verify(timeout = 100) {
            view.setInitialUrl(url)
        }

        confirmVerified(view)
    }
}