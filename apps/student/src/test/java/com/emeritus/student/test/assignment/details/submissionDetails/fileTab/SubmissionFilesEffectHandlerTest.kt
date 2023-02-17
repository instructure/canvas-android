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

package com.emeritus.student.test.assignment.details.submissionDetails.fileTab

import com.instructure.canvasapi2.models.Attachment
import com.emeritus.student.mobius.assignmentDetails.submissionDetails.SubmissionDetailsSharedEvent
import com.emeritus.student.mobius.assignmentDetails.submissionDetails.drawer.files.SubmissionFilesEffect
import com.emeritus.student.mobius.assignmentDetails.submissionDetails.drawer.files.SubmissionFilesEffectHandler
import com.emeritus.student.mobius.assignmentDetails.submissionDetails.drawer.files.SubmissionFilesEvent
import com.emeritus.student.mobius.assignmentDetails.submissionDetails.drawer.files.ui.SubmissionFilesView
import com.emeritus.student.mobius.common.ChannelSource
import com.instructure.student.test.util.receiveOnce
import com.spotify.mobius.functions.Consumer
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.test.setMain
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.util.concurrent.Executors

class SubmissionFilesEffectHandlerTest : Assert() {
    private val mockView: SubmissionFilesView = mockk(relaxed = true)
    private val effectHandler = SubmissionFilesEffectHandler().apply { view = mockView }
    private val eventConsumer: Consumer<SubmissionFilesEvent> = mockk(relaxed = true)
    private val connection = effectHandler.connect(eventConsumer)

    @Before
    fun setup() {
        Dispatchers.setMain(Executors.newSingleThreadExecutor().asCoroutineDispatcher())
    }

    @Test
    fun `BroadcastFileSelected effect sends File selected shared event`() {
        val channel = ChannelSource.getChannel<SubmissionDetailsSharedEvent>()
        val attachment = Attachment(id = 123L, contentType = "test/data")
        val expectedEvent = SubmissionDetailsSharedEvent.FileSelected(attachment)
        val actualEvent = channel.receiveOnce {
            connection.accept(SubmissionFilesEffect.BroadcastFileSelected(attachment))
        }
        assertEquals(expectedEvent, actualEvent)
    }

}


