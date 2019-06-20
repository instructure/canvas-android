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

import android.content.Context
import android.content.Intent
import com.instructure.canvasapi2.utils.ProgressEvent
import com.instructure.pandautils.models.FileSubmitObject
import com.instructure.pandautils.utils.Const
import com.instructure.student.FileSubmission
import com.instructure.student.db.Db
import com.instructure.student.db.StudentDb
import com.instructure.student.db.getInstance
import com.instructure.student.mobius.assignmentDetails.submission.file.UploadStatusSubmissionEffect
import com.instructure.student.mobius.assignmentDetails.submission.file.UploadStatusSubmissionEffectHandler
import com.instructure.student.mobius.assignmentDetails.submission.file.UploadStatusSubmissionEvent
import com.instructure.student.mobius.assignmentDetails.submission.file.ui.UploadStatusSubmissionView
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

class UploadStatusSubmissionEffectHandlerTest : Assert() {
    private val context: Context = mockk(relaxed = true)
    private val view: UploadStatusSubmissionView = mockk(relaxed = true)
    private val eventConsumer: Consumer<UploadStatusSubmissionEvent> = mockk(relaxed = true)
    private val effectHandler = UploadStatusSubmissionEffectHandler(context)
    private val connection = effectHandler.connect(eventConsumer)

    @ExperimentalCoroutinesApi
    @Before
    fun setup() {
        effectHandler.view = view
        Dispatchers.setMain(Executors.newSingleThreadExecutor().asCoroutineDispatcher())
    }

    @Test
    fun `LoadPersistedFiles results in OnPersistedSubmissionLoaded event`() {
        val submissionId = 123L

        val list = listOf(
            FileSubmission.Impl(
                0,
                submissionId,
                null,
                "File Name",
                1L,
                "contentType",
                "fullPath",
                null,
                false
            )
        )

        mockkStatic("com.instructure.student.db.ExtensionsKt")

        val db: StudentDb = mockk {
            every {
                submissionQueries.getSubmissionById(submissionId).executeAsOne().errorFlag
            } returns false
            every {
                fileSubmissionQueries.getFilesForSubmissionId(submissionId).executeAsList()
            } returns list
        }

        every { Db.getInstance(context) } returns db

        connection.accept(UploadStatusSubmissionEffect.LoadPersistedFiles(submissionId))

        verify(timeout = 100) {
            eventConsumer.accept(
                UploadStatusSubmissionEvent.OnPersistedSubmissionLoaded(
                    false,
                    listOf(FileSubmitObject("File Name", 1L, "contentType", "fullPath"))
                )
            )
        }

        confirmVerified(eventConsumer)
    }

    @Test
    fun `receiver getting called results in OnFilesRefreshed event`() {
        val submissionId = 123L

        val file = FileSubmitObject("File Name", 1L, "contentType", "fullPath")
        val list = listOf(
            FileSubmission.Impl(
                0,
                submissionId,
                null,
                file.name,
                file.size,
                file.contentType,
                file.fullPath,
                null,
                false
            )
        )

        mockkStatic("com.instructure.student.db.ExtensionsKt")

        val db: StudentDb = mockk {
            every {
                submissionQueries.getSubmissionById(submissionId).executeAsOne().errorFlag
            } returns false
            every {
                fileSubmissionQueries.getFilesForSubmissionId(submissionId).executeAsList()
            } returns list
        }

        every { Db.getInstance(context) } returns db

        val intent = mockk<Intent>()
        every { intent.hasExtra(Const.SUBMISSION) } returns true
        every { intent.extras.getLong(Const.SUBMISSION) } returns submissionId

        effectHandler.receiver.onReceive(context, intent)

        verify(timeout = 100) {
            eventConsumer.accept(
                UploadStatusSubmissionEvent.OnFilesRefreshed(
                    false,
                    submissionId,
                    listOf(file)
                )
            )
        }

        confirmVerified(eventConsumer)
    }

    @Test
    fun `receiver getting called with no submission ID results in no event`() {
        val intent = mockk<Intent>()
        every { intent.hasExtra(Const.SUBMISSION) } returns false

        effectHandler.receiver.onReceive(context, intent)

        verify(exactly = 0) {
            eventConsumer.accept(any())
        }

        confirmVerified(eventConsumer)
    }

    @Test
    fun `receiver getting called with no context results in no event`() {
        val intent = mockk<Intent>()
        every { intent.hasExtra(Const.SUBMISSION) } returns true

        effectHandler.receiver.onReceive(null, intent)

        verify(exactly = 0) {
            eventConsumer.accept(any())
        }

        confirmVerified(eventConsumer)
    }

    @Test
    fun `onUploadProgress results in OnUploadProgressChanged event`() {
        effectHandler.onUploadProgress(ProgressEvent(0, 1, 2, 3))


        verify(timeout = 100) {
            eventConsumer.accept(UploadStatusSubmissionEvent.OnUploadProgressChanged(0, 1, 2))
        }
    }


}
