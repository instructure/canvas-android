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
@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.instructure.student.test.assignment.details.submissionDetails.fileTab

import com.instructure.canvasapi2.models.Attachment
import com.instructure.student.mobius.assignmentDetails.submissionDetails.SubmissionDetailsSharedEvent
import com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.files.SubmissionFilesEffect
import com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.files.SubmissionFilesEffectHandler
import com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.files.SubmissionFilesEvent
import com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.files.ui.SubmissionFilesView
import com.instructure.student.mobius.common.FlowSource
import com.spotify.mobius.functions.Consumer
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SubmissionFilesEffectHandlerTest : Assert() {
    private val mockView: SubmissionFilesView = mockk(relaxed = true)
    private val effectHandler = SubmissionFilesEffectHandler().apply { view = mockView }
    private val eventConsumer: Consumer<SubmissionFilesEvent> = mockk(relaxed = true)
    private val connection = effectHandler.connect(eventConsumer)
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `BroadcastFileSelected effect sends File selected shared event`() = runTest(testDispatcher) {
        val flow = FlowSource.getFlow<SubmissionDetailsSharedEvent>()
        val attachment = Attachment(id = 123L, contentType = "test/data")
        val expectedEvent = SubmissionDetailsSharedEvent.FileSelected(attachment)

        val deferred = async {
            flow.first()
        }

        connection.accept(SubmissionFilesEffect.BroadcastFileSelected(attachment))
        assertEquals(expectedEvent, deferred.await())
    }

}


