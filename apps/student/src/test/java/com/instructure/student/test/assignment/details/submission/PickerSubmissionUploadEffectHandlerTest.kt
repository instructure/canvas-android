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
import android.net.Uri
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.utils.ProgressEvent
import com.instructure.pandautils.models.FileSubmitObject
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.FileUploadUtils
import com.instructure.student.FileSubmission
import com.instructure.student.db.Db
import com.instructure.student.db.StudentDb
import com.instructure.student.db.getInstance
import com.instructure.student.mobius.assignmentDetails.submission.picker.PickerSubmissionUploadEffect
import com.instructure.student.mobius.assignmentDetails.submission.picker.PickerSubmissionUploadEffectHandler
import com.instructure.student.mobius.assignmentDetails.submission.picker.PickerSubmissionUploadEvent
import com.instructure.student.mobius.assignmentDetails.submission.picker.PickerSubmissionUploadModel
import com.instructure.student.mobius.assignmentDetails.submission.picker.ui.PickerSubmissionUploadView
import com.instructure.student.mobius.common.ui.SubmissionService
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

class PickerSubmissionUploadEffectHandlerTest : Assert() {
    private val context: Context = mockk(relaxed = true)
    private val view: PickerSubmissionUploadView = mockk(relaxed = true)
    private val eventConsumer: Consumer<PickerSubmissionUploadEvent> = mockk(relaxed = true)
    private val effectHandler = PickerSubmissionUploadEffectHandler(context)
    private val connection = effectHandler.connect(eventConsumer)

    @ExperimentalCoroutinesApi
    @Before
    fun setup() {
        effectHandler.view = view
        Dispatchers.setMain(Executors.newSingleThreadExecutor().asCoroutineDispatcher())
    }

    @Test
    fun `LaunchCamera results in view calling launchCamera`() {
        connection.accept(PickerSubmissionUploadEffect.LaunchCamera)

        verify(timeout = 100) {
            view.launchCamera()
        }

        confirmVerified(view)
    }

    @Test
    fun `LaunchGallery results in view calling launchGallery`() {
        connection.accept(PickerSubmissionUploadEffect.LaunchGallery)

        verify(timeout = 100) {
            view.launchGallery()
        }

        confirmVerified(view)
    }

    @Test
    fun `LaunchVideoRecorder results in view calling launchVideoRecorder`() {
        connection.accept(PickerSubmissionUploadEffect.LaunchVideoRecorder)

        verify(timeout = 100) {
            view.launchVideoRecorder()
        }

        confirmVerified(view)
    }

    @Test
    fun `LaunchAudioRecorder results in view calling launchAudioRecorder`() {
        connection.accept(PickerSubmissionUploadEffect.LaunchAudioRecorder)

        verify(timeout = 100) {
            view.launchAudioRecorder()
        }

        confirmVerified(view)
    }

    @Test
    fun `LaunchSelectFile results in view calling launchSelectFile`() {
        connection.accept(PickerSubmissionUploadEffect.LaunchSelectFile)

        verify(timeout = 100) {
            view.launchSelectFile()
        }

        confirmVerified(view)
    }

    @Test
    fun `LoadFileContents with no restricted extensions results in OnFileAdded event`() {
        val uri = mockk<Uri>()
        val mimeType = "mime"
        val fileName = "file"
        val file = FileSubmitObject(fileName, 1L, mimeType, "fullPath.ext")

        mockkStatic(FileUploadUtils::class)
        every { FileUploadUtils.getFileMimeType(any(), uri) } returns mimeType
        every { FileUploadUtils.getFileNameWithDefault(any(), uri, mimeType) } returns fileName
        every {
            FileUploadUtils.getFileSubmitObjectFromInputStream(
                context,
                uri,
                fileName,
                mimeType
            )
        } returns file

        connection.accept(PickerSubmissionUploadEffect.LoadFileContents(uri, emptyList()))

        verify(timeout = 100) {
            eventConsumer.accept(
                PickerSubmissionUploadEvent.OnFileAdded(file)
            )
        }

        confirmVerified(eventConsumer)
    }

    @Test
    fun `LoadFileContents with allowed extension results in OnFileAdded event`() {
        val uri = mockk<Uri>()
        val mimeType = "mime"
        val fileName = "file"
        val file = FileSubmitObject(fileName, 1L, mimeType, "fullPath.ext")

        mockkStatic(FileUploadUtils::class)
        every { FileUploadUtils.getFileMimeType(any(), uri) } returns mimeType
        every { FileUploadUtils.getFileNameWithDefault(any(), uri, mimeType) } returns fileName
        every {
            FileUploadUtils.getFileSubmitObjectFromInputStream(
                context,
                uri,
                fileName,
                mimeType
            )
        } returns file

        connection.accept(
            PickerSubmissionUploadEffect.LoadFileContents(
                uri,
                listOf("bad", "other", "ext")
            )
        )

        verify(timeout = 10000) {
            eventConsumer.accept(
                PickerSubmissionUploadEvent.OnFileAdded(file)
            )
        }

        confirmVerified(eventConsumer)
    }

    @Test
    fun `LoadFileContents with not allowed extension results in view calling showBadExtensionDialog`() {
        val uri = mockk<Uri>()
        val mimeType = "mime"
        val fileName = "file"
        val file = FileSubmitObject(fileName, 1L, mimeType, "fullPath.ext")
        val allowedExtensions = listOf("bad", "other")

        mockkStatic(FileUploadUtils::class)
        every { FileUploadUtils.getFileMimeType(any(), uri) } returns mimeType
        every { FileUploadUtils.getFileNameWithDefault(any(), uri, mimeType) } returns fileName
        every {
            FileUploadUtils.getFileSubmitObjectFromInputStream(
                context,
                uri,
                fileName,
                mimeType
            )
        } returns file

        connection.accept(PickerSubmissionUploadEffect.LoadFileContents(uri, allowedExtensions))

        verify(timeout = 1000) {
            view.showBadExtensionDialog(allowedExtensions)
        }

        confirmVerified(view)
    }

    @Test
    fun `LoadFileContents with file error results in view calling showFileErrorMessage`() {
        val uri = mockk<Uri>()
        val mimeType = "mime"
        val fileName = "file"
        val errorMessage = "error"
        val file = FileSubmitObject(fileName, 1L, mimeType, "fullPath.ext", errorMessage)

        mockkStatic(FileUploadUtils::class)
        every { FileUploadUtils.getFileMimeType(any(), uri) } returns mimeType
        every { FileUploadUtils.getFileNameWithDefault(any(), uri, mimeType) } returns fileName
        every {
            FileUploadUtils.getFileSubmitObjectFromInputStream(
                context,
                uri,
                fileName,
                mimeType
            )
        } returns file

        connection.accept(PickerSubmissionUploadEffect.LoadFileContents(uri, emptyList()))

        verify(timeout = 1000) {
            view.showFileErrorMessage(errorMessage)
        }

        confirmVerified(view)
    }

    @Test
    fun `HandleSubmit and isMediaSubmission results in starting media submission`() {
        val model = PickerSubmissionUploadModel(
            canvasContext = CanvasContext.emptyCourseContext(1L),
            assignmentId = 2L,
            assignmentGroupCategoryId = 3L,
            assignmentName = "AssignmentName",
            allowedExtensions = emptyList(),
            isMediaSubmission = true
        )

        mockkObject(SubmissionService.Companion)
        every { SubmissionService.startMediaSubmission(any(), any(), any(), any()) } returns Unit

        connection.accept(PickerSubmissionUploadEffect.HandleSubmit(model))

        verify(timeout = 100) {
            SubmissionService.startMediaSubmission(
                context,
                model.canvasContext,
                model.assignmentId,
                model.assignmentName
            )
            view.closeSubmissionView()
        }

        confirmVerified(view)
        confirmVerified(SubmissionService)
    }

    @Test
    fun `HandleSubmit and not isMediaSubmission results in starting file submission`() {
        val model = PickerSubmissionUploadModel(
            canvasContext = CanvasContext.emptyCourseContext(1L),
            assignmentId = 2L,
            assignmentGroupCategoryId = 3L,
            assignmentName = "AssignmentName",
            allowedExtensions = emptyList(),
            isMediaSubmission = false
        )

        mockkObject(SubmissionService.Companion)
        every {
            SubmissionService.startFileSubmission(any(), any(), any(), any(), any(), any())
        } returns Unit

        connection.accept(PickerSubmissionUploadEffect.HandleSubmit(model))

        verify(timeout = 100) {
            SubmissionService.startFileSubmission(
                context,
                model.canvasContext,
                model.assignmentId,
                model.assignmentName,
                model.assignmentGroupCategoryId,
                ArrayList(model.files)
            )
            view.closeSubmissionView()
        }

        confirmVerified(view)
        confirmVerified(SubmissionService)
    }
}
