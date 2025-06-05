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
import com.instructure.student.mobius.assignmentDetails.submission.file.UploadStatusSubmissionEffect
import com.instructure.student.mobius.assignmentDetails.submission.file.UploadStatusSubmissionEffectHandler
import com.instructure.student.mobius.assignmentDetails.submission.file.UploadStatusSubmissionEvent
import com.instructure.student.mobius.assignmentDetails.submission.file.ui.UploadStatusSubmissionView
import com.instructure.student.mobius.common.ui.SubmissionHelper
import com.instructure.pandautils.room.studentdb.StudentDb
import com.instructure.pandautils.room.studentdb.entities.CreateFileSubmissionEntity
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
    private val view: UploadStatusSubmissionView = mockk(relaxed = true)
    private val eventConsumer: Consumer<UploadStatusSubmissionEvent> = mockk(relaxed = true)
    private val submissionHelper: SubmissionHelper = mockk(relaxed = true)
    private val studentDb: StudentDb = mockk(relaxed = true)
    private val effectHandler =
        UploadStatusSubmissionEffectHandler(submissionId, submissionHelper, studentDb)
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
            CreateFileSubmissionEntity(
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

        coEvery {
            studentDb.submissionDao().findSubmissionById(submissionId)
        } returns mockk {
            every { errorFlag } returns failed
            every { assignmentName } returns name
        }
        coEvery {
            studentDb.fileSubmissionDao().findFilesForSubmissionId(submissionId)
        } returns list

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
        coEvery {
            studentDb.submissionDao().findSubmissionById(submissionId)
        } returns null

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
        coEvery {
            studentDb.submissionDao().deleteSubmissionById(submissionId)
        } returns Unit

        coEvery {
            studentDb.fileSubmissionDao().deleteFilesForSubmissionId(submissionId)
        } returns Unit


        effectHandler.accept(UploadStatusSubmissionEffect.OnDeleteSubmission(submissionId))

        coVerify(timeout = 100) {
            studentDb.submissionDao().deleteSubmissionById(submissionId)
            studentDb.fileSubmissionDao().deleteFilesForSubmissionId(submissionId)
            view.submissionDeleted()
        }

        confirmVerified(studentDb, view)
    }

    @Test
    fun `RetrySubmission results in view call for submissionDeleted`() {
        val course = Course()

        coEvery {
            studentDb.submissionDao().findSubmissionById(submissionId)?.canvasContext
        } returns course

        effectHandler.accept(UploadStatusSubmissionEffect.RetrySubmission(submissionId))

        verify(timeout = 100) {
            submissionHelper.retryFileSubmission(submissionId)
            view.submissionRetrying()
        }

        confirmVerified(view, submissionHelper)
    }

    @Test
    fun `OnDeleteFileFromSubmission results in db deletion`() {
        val fileId = 101L

        coEvery {
            studentDb.fileSubmissionDao().deleteFileById(fileId)
        } returns Unit

        effectHandler.accept(UploadStatusSubmissionEffect.OnDeleteFileFromSubmission(fileId))

        coVerify(timeout = 100) {
            studentDb.fileSubmissionDao().deleteFileById(fileId)
        }

        confirmVerified(studentDb)
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
