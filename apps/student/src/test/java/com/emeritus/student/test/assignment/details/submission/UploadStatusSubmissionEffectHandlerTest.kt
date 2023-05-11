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
package com.emeritus.student.test.assignment.details.submission

import android.content.Context
import com.instructure.canvasapi2.models.Course
import com.emeritus.student.FileSubmission
import com.emeritus.student.db.Db
import com.emeritus.student.db.StudentDb
import com.emeritus.student.db.getInstance
import com.emeritus.student.mobius.assignmentDetails.submission.file.UploadStatusSubmissionEffect
import com.emeritus.student.mobius.assignmentDetails.submission.file.UploadStatusSubmissionEffectHandler
import com.emeritus.student.mobius.assignmentDetails.submission.file.UploadStatusSubmissionEvent
import com.emeritus.student.mobius.assignmentDetails.submission.file.ui.UploadStatusSubmissionView
import com.emeritus.student.mobius.common.ui.SubmissionService
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
    private val submissionId = 123L
    private val context: Context = mockk(relaxed = true)
    private val view: UploadStatusSubmissionView = mockk(relaxed = true)
    private val eventConsumer: Consumer<UploadStatusSubmissionEvent> = mockk(relaxed = true)
    private val effectHandler = UploadStatusSubmissionEffectHandler(context, submissionId)
    private val connection = effectHandler.connect(eventConsumer)

    @ExperimentalCoroutinesApi
    @Before
    fun setup() {
        effectHandler.view = view
        Dispatchers.setMain(Executors.newSingleThreadExecutor().asCoroutineDispatcher())
    }

    @Test
    fun `LoadPersistedFiles results in OnPersistedSubmissionLoaded event`() {
        val name = "assignment"
        val failed = false
        val list = listOf(
            FileSubmission(
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

        mockkStatic("com.emeritus.student.db.ExtensionsKt")

        val db: StudentDb = mockk {
            every {
                submissionQueries.getSubmissionById(submissionId).executeAsOneOrNull()
            } returns mockk {
                every { errorFlag } returns failed
                every { assignmentName } returns name
            }
            every {
                fileSubmissionQueries.getFilesForSubmissionId(submissionId).executeAsList()
            } returns list
        }

        every { Db.getInstance(context) } returns db

        connection.accept(UploadStatusSubmissionEffect.LoadPersistedFiles(submissionId))

        verify(timeout = 100) {
            eventConsumer.accept(
                UploadStatusSubmissionEvent.OnPersistedSubmissionLoaded(name, failed, list)
            )
        }

        confirmVerified(eventConsumer)
    }

    @Test
    fun `LoadPersistedFiles results in OnPersistedSubmissionLoaded event with success submission`() {
        mockkStatic("com.emeritus.student.db.ExtensionsKt")

        val db: StudentDb = mockk {
            every {
                submissionQueries.getSubmissionById(submissionId).executeAsOneOrNull()
            } returns null
        }

        every { Db.getInstance(context) } returns db

        connection.accept(UploadStatusSubmissionEffect.LoadPersistedFiles(submissionId))

        verify(timeout = 100) {
            eventConsumer.accept(
                UploadStatusSubmissionEvent.OnPersistedSubmissionLoaded(null, false, emptyList())
            )
        }

        confirmVerified(eventConsumer)
    }

    @Test
    fun `OnDeleteSubmission results in view call for submissionDeleted`() {

        mockkStatic("com.emeritus.student.db.ExtensionsKt")

        val db: StudentDb = mockk {
            every {
                submissionQueries.deleteSubmissionById(submissionId)
            } returns Unit

            every {
                fileSubmissionQueries.deleteFilesForSubmissionId(submissionId)
            } returns Unit
        }

        every { Db.getInstance(context) } returns db

        effectHandler.accept(UploadStatusSubmissionEffect.OnDeleteSubmission(submissionId))

        verify (timeout = 100) {
            db.submissionQueries.deleteSubmissionById(submissionId)
            db.fileSubmissionQueries.deleteFilesForSubmissionId(submissionId)
            view.submissionDeleted()
        }

        confirmVerified(db, view)
    }

    @Test
    fun `RetrySubmission results in view call for submissionDeleted`() {
        val course = Course()

        mockkObject(SubmissionService.Companion)
        every {
            SubmissionService.retryFileSubmission(any(), any())
        } returns Unit

        mockkStatic("com.emeritus.student.db.ExtensionsKt")

        val db: StudentDb = mockk {
            every {
                submissionQueries.getSubmissionById(submissionId).executeAsOne().canvasContext
            } returns course
        }

        every { Db.getInstance(context) } returns db

        effectHandler.accept(UploadStatusSubmissionEffect.RetrySubmission(submissionId))

        verify (timeout = 100) {
            SubmissionService.retryFileSubmission(context, submissionId)
            view.submissionRetrying()
        }

        confirmVerified(view, SubmissionService)
    }

    @Test
    fun `OnDeleteFileFromSubmission results in db deletion`() {
        val fileId = 101L
        mockkStatic("com.emeritus.student.db.ExtensionsKt")

        val db: StudentDb = mockk {
            every {
                fileSubmissionQueries.deleteFileById(fileId)
            } returns Unit
        }

        every { Db.getInstance(context) } returns db

        effectHandler.accept(UploadStatusSubmissionEffect.OnDeleteFileFromSubmission(fileId))

        verify (timeout = 100) {
            db.fileSubmissionQueries.deleteFileById(fileId)
        }

        confirmVerified(db)
    }

    @Test
    fun `ShowCancelDialog calls showCancelSubmissionDialog`() {
        effectHandler.accept(UploadStatusSubmissionEffect.ShowCancelDialog)
        verify(timeout = 100) {
            view.showCancelSubmissionDialog()
        }
        confirmVerified(view)
    }
}
