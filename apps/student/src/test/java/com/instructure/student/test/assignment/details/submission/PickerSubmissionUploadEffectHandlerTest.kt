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

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentActivity
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.postmodels.FileSubmitObject
import com.instructure.pandautils.utils.ActivityResult
import com.instructure.pandautils.utils.FilePrefs
import com.instructure.pandautils.utils.FileUploadUtils
import com.instructure.pandautils.utils.OnActivityResults
import com.instructure.pandautils.utils.PermissionUtils
import com.instructure.pandautils.utils.requestPermissions
import com.instructure.student.R
import com.instructure.student.mobius.assignmentDetails.isIntentAvailable
import com.instructure.student.mobius.assignmentDetails.submission.picker.PickerSubmissionMode
import com.instructure.student.mobius.assignmentDetails.submission.picker.PickerSubmissionUploadEffect
import com.instructure.student.mobius.assignmentDetails.submission.picker.PickerSubmissionUploadEffectHandler
import com.instructure.student.mobius.assignmentDetails.submission.picker.PickerSubmissionUploadEvent
import com.instructure.student.mobius.assignmentDetails.submission.picker.PickerSubmissionUploadModel
import com.instructure.student.mobius.assignmentDetails.submission.picker.ui.PickerSubmissionUploadView
import com.instructure.student.mobius.common.ui.SubmissionHelper
import com.spotify.mobius.functions.Consumer
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.invoke
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkObject
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.test.setMain
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.io.File
import java.util.concurrent.Executors

class PickerSubmissionUploadEffectHandlerTest : Assert() {
    private val context: FragmentActivity = mockk(relaxed = true)
    private val view: PickerSubmissionUploadView = mockk(relaxed = true)
    private val eventConsumer: Consumer<PickerSubmissionUploadEvent> = mockk(relaxed = true)
    private val submissionHelper: SubmissionHelper = mockk(relaxed = true)
    private val effectHandler = PickerSubmissionUploadEffectHandler(context, submissionHelper)
    private val connection = effectHandler.connect(eventConsumer)

    @ExperimentalCoroutinesApi
    @Before
    fun setup() {
        effectHandler.view = view
        Dispatchers.setMain(Executors.newSingleThreadExecutor().asCoroutineDispatcher())
    }

    @Test
    fun `LaunchCamera without permission will request permission and show an error message when denied`() {
        // Mock both so we can mockk the class and the extensions in the same file
        mockkObject(PermissionUtils)
        mockkStatic("${PermissionUtils::class.java.canonicalName}Kt")
        every { PermissionUtils.hasPermissions(context, *anyVararg()) } returns false

        val block = slot<(Map<String, Boolean>) -> Unit>()

        every { context.requestPermissions(any(), capture(block)) } answers {
            block.invoke(mapOf(Pair("any", false)))
        }

        connection.accept(PickerSubmissionUploadEffect.LaunchCamera)

        verify(timeout = 100) {
            view.showErrorMessage(R.string.permissionDenied)
        }

        confirmVerified(view)

        unmockkObject(PermissionUtils)
        unmockkStatic("${PermissionUtils::class.java.canonicalName}Kt")
    }

    @Test
    fun `LaunchCamera without permission will request permission and send CameraClicked event when successful`() {
        // Mock both so we can mockk the class and the extensions in the same file
        mockkObject(PermissionUtils)
        mockkStatic("${PermissionUtils::class.java.canonicalName}Kt")
        every { PermissionUtils.hasPermissions(context, *anyVararg()) } returns false

        val block = slot<(Map<String, Boolean>) -> Unit>()

        every { context.requestPermissions(any(), capture(block)) } answers {
            block.invoke(mapOf(Pair("any", true)))
        }

        connection.accept(PickerSubmissionUploadEffect.LaunchCamera)

        verify(timeout = 100) {
            eventConsumer.accept(PickerSubmissionUploadEvent.CameraClicked)
        }

        confirmVerified(eventConsumer)

        unmockkObject(PermissionUtils)
        unmockkStatic("${PermissionUtils::class.java.canonicalName}Kt")
    }

    @Test
    fun `LaunchCamera results in launching intent`() {
        val uri = mockk<Uri>()
        val intent = mockk<Intent>()
        every { intent.action } returns ""

        mockkStatic("com.instructure.student.mobius.assignmentDetails.SubmissionUtilsKt")
        every { any<Context>().isIntentAvailable(any()) } returns true

        mockkObject(PermissionUtils)
        every { PermissionUtils.hasPermissions(context, *anyVararg()) } returns true

        mockkObject(FileUploadUtils)
        every { FileUploadUtils.getExternalCacheDir(context) } returns File("")

        mockkStatic(FileProvider::class)
        every { FileProvider.getUriForFile(any(), any(), any()) } returns uri

        mockkObject(FilePrefs)
        every { FilePrefs.tempCaptureUri = any() } answers { "" }

        every { view.getCameraIntent(uri) } returns intent

        connection.accept(PickerSubmissionUploadEffect.LaunchCamera)

         verify(timeout = 100) {
            context.startActivityForResult(intent, PickerSubmissionUploadEffectHandler.REQUEST_CAMERA_PIC)
        }

        confirmVerified(eventConsumer)

        unmockkObject(PermissionUtils)
    }

    @Test
    fun `LaunchGallery results in launching intent`() {
        val uri = mockk<Uri>()
        val intent = mockk<Intent>()

        every { context.packageName } returns "package"
        every { context.filesDir } returns File("")

        mockkStatic(FileProvider::class)
        every { FileProvider.getUriForFile(any(), any(), any()) } returns uri

        every { view.getGalleryIntent(uri) } returns intent

        connection.accept(PickerSubmissionUploadEffect.LaunchGallery)

        verify(timeout = 100) {
            context.packageName
            context.filesDir
            context.startActivityForResult(
                intent,
                PickerSubmissionUploadEffectHandler.REQUEST_PICK_IMAGE_GALLERY
            )
        }

        confirmVerified(context)
    }

    @Test
    fun `LaunchSelectFile results in launching intent`() {
        val intent = mockk<Intent>()

        every { view.getSelectFileIntent() } returns intent

        connection.accept(PickerSubmissionUploadEffect.LaunchSelectFile)

        verify(timeout = 100) {
            context.startActivityForResult(
                intent,
                PickerSubmissionUploadEffectHandler.REQUEST_PICK_FILE_FROM_DEVICE
            )
        }

        confirmVerified(context)
    }

    @Test
    fun `LoadFileContents with no restricted extensions results in OnFileAdded event`() {
        val uri = mockk<Uri>()
        val mimeType = "mime"
        val fileName = "file"
        val file = FileSubmitObject(fileName, 1L, mimeType, "fullPath.ext")

        mockkObject(FileUploadUtils)
        every { FileUploadUtils.getFileMimeType(any(), uri) } returns mimeType
        every { FileUploadUtils.getFileNameWithDefault(any(), uri) } returns fileName
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

        mockkObject(FileUploadUtils)
        every { FileUploadUtils.getFileMimeType(any(), uri) } returns mimeType
        every { FileUploadUtils.getFileNameWithDefault(any(), uri) } returns fileName
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

        mockkObject(FileUploadUtils)
        every { FileUploadUtils.getFileMimeType(any(), uri) } returns mimeType
        every { FileUploadUtils.getFileNameWithDefault(any(), uri) } returns fileName
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
            eventConsumer.accept(PickerSubmissionUploadEvent.OnFileAdded(null))
        }

        confirmVerified(view, eventConsumer)
    }

    @Test
    fun `LoadFileContents with file error results in view calling showFileErrorMessage`() {
        val uri = mockk<Uri>()
        val mimeType = "mime"
        val fileName = "file"
        val errorMessage = "error"
        val file = FileSubmitObject(fileName, 1L, mimeType, "fullPath.ext", errorMessage)

        mockkObject(FileUploadUtils)
        every { FileUploadUtils.getFileMimeType(any(), uri) } returns mimeType
        every { FileUploadUtils.getFileNameWithDefault(any(), uri) } returns fileName
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
            eventConsumer.accept(PickerSubmissionUploadEvent.OnFileAdded(null))
        }

        confirmVerified(view, eventConsumer)
    }

    @Test
    fun `HandleSubmit and MediaSubmission mode results in starting media submission`() {
        val file = FileSubmitObject("file.mp4", 1L, "mimeType", "/path/to/the/file.mp4")
        val model = PickerSubmissionUploadModel(
            canvasContext = CanvasContext.emptyCourseContext(1L),
            assignmentId = 2L,
            assignmentGroupCategoryId = 3L,
            assignmentName = "AssignmentName",
            allowedExtensions = emptyList(),
            files = listOf(file),
            mode = PickerSubmissionMode.MediaSubmission
        )

        connection.accept(PickerSubmissionUploadEffect.HandleSubmit(model))

        verify(timeout = 100) {
            submissionHelper.startMediaSubmission(
                model.canvasContext,
                model.assignmentId,
                model.assignmentName,
                model.assignmentGroupCategoryId,
                model.files.first().fullPath
            )
            view.closeSubmissionView()
        }

        confirmVerified(view)
        confirmVerified(submissionHelper)
    }

    @Test
    fun `HandleSubmit and FileSubmission mode results in starting file submission`() {
        val model = PickerSubmissionUploadModel(
            canvasContext = CanvasContext.emptyCourseContext(1L),
            assignmentId = 2L,
            assignmentGroupCategoryId = 3L,
            assignmentName = "AssignmentName",
            allowedExtensions = emptyList(),
            mode = PickerSubmissionMode.FileSubmission
        )

        connection.accept(PickerSubmissionUploadEffect.HandleSubmit(model))

        verify(timeout = 100) {
            submissionHelper.startFileSubmission(
                model.canvasContext,
                model.assignmentId,
                model.assignmentName,
                model.assignmentGroupCategoryId,
                ArrayList(model.files)
            )
            view.closeSubmissionView()
        }

        confirmVerified(view)
        confirmVerified(submissionHelper)
    }

    @Test
    fun `HandleSubmit and CommentAttachment mode results in starting file comment upload`() {
        val model = PickerSubmissionUploadModel(
            canvasContext = CanvasContext.emptyCourseContext(1L),
            assignmentId = 2L,
            assignmentGroupCategoryId = 3L,
            assignmentName = "AssignmentName",
            allowedExtensions = emptyList(),
            mode = PickerSubmissionMode.CommentAttachment,
            attemptId = 1
        )

        connection.accept(PickerSubmissionUploadEffect.HandleSubmit(model))

        verify(timeout = 100) {
            submissionHelper.startCommentUpload(
                model.canvasContext,
                model.assignmentId,
                model.assignmentName,
                null,
                ArrayList(model.files),
                true,
                1
            )
            view.closeSubmissionView()
        }

        confirmVerified(view)
        confirmVerified(submissionHelper)
    }

    @Test
    fun `eventBus onActivityResults results in no event if resultCode is not RESULT_OK`() {
        val result = ActivityResult(
            PickerSubmissionUploadEffectHandler.REQUEST_PICK_FILE_FROM_DEVICE,
            Activity.RESULT_CANCELED,
            null
        )

        effectHandler.onActivityResults(OnActivityResults(result))

        verify(exactly = 0) {
            eventConsumer.accept(any())
            view.showErrorMessage(any())
        }

        confirmVerified(view)
        confirmVerified(eventConsumer)
    }

    @Test
    fun `eventBus onActivityResults results in no event when there is no uri data`() {
        val result = ActivityResult(
            PickerSubmissionUploadEffectHandler.REQUEST_PICK_FILE_FROM_DEVICE,
            Activity.RESULT_OK,
            null
        )

        effectHandler.onActivityResults(OnActivityResults(result))

        verify(timeout = 100) {
            view.showErrorMessage(R.string.unexpectedErrorOpeningFile)
        }
        verify(exactly = 0) {
            eventConsumer.accept(any())
        }

        confirmVerified(view)
        confirmVerified(eventConsumer)
    }

    @Test
    fun `eventBus onActivityResults results in OnFileSelected event`() {
        val uri = mockk<Uri>()
        val intent = mockk<Intent>()
        every { intent.data } returns uri

        val result = ActivityResult(
            PickerSubmissionUploadEffectHandler.REQUEST_PICK_FILE_FROM_DEVICE,
            Activity.RESULT_OK,
            intent
        )

        effectHandler.onActivityResults(OnActivityResults(result))

        verify(timeout = 100) {
            eventConsumer.accept(PickerSubmissionUploadEvent.OnFileSelected(uri))
        }

        confirmVerified(eventConsumer)
    }

    @Test
    fun `eventBus onActivityResults results in OnFileSelected event when requestCode is CAMERA_PIC_REQUEST`() {
        val uri = mockk<Uri>()

        mockkStatic(Uri::class)
        mockkObject(FilePrefs)
        every { FilePrefs.tempCaptureUri } returns ""
        every { Uri.parse("") } returns uri

        val result = ActivityResult(
            PickerSubmissionUploadEffectHandler.REQUEST_CAMERA_PIC,
            Activity.RESULT_OK,
            null
        )

        effectHandler.onActivityResults(OnActivityResults(result))

        verify(timeout = 100) {
            eventConsumer.accept(PickerSubmissionUploadEvent.OnFileSelected(uri))
        }

        confirmVerified(eventConsumer)
    }

    @Test
    fun `eventBus onActivityResults results in view calling showFileErrorMessage when requestCode is CAMERA_PIC_REQUEST with no uri data`() {
        mockkStatic(Uri::class)
        mockkObject(FilePrefs)
        every { FilePrefs.tempCaptureUri } returns ""
        every { Uri.parse("") } returns null

        val result = ActivityResult(
            PickerSubmissionUploadEffectHandler.REQUEST_CAMERA_PIC,
            Activity.RESULT_OK,
            null
        )

        effectHandler.onActivityResults(OnActivityResults(result))

        verify(timeout = 100) {
            view.showErrorMessage(R.string.utils_errorGettingPhoto)
        }
        verify(exactly = 0) {
            eventConsumer.accept(any())
        }

        confirmVerified(eventConsumer)
    }

    @Test
    fun `isPickerRequest with REQUEST_CAMERA_PIC code returns true`() {
        assertTrue(
            PickerSubmissionUploadEffectHandler.isPickerRequest(
                PickerSubmissionUploadEffectHandler.REQUEST_CAMERA_PIC
            )
        )
    }

    @Test
    fun `isPickerRequest with REQUEST_PICK_IMAGE_GALLERY code returns true`() {
        assertTrue(
            PickerSubmissionUploadEffectHandler.isPickerRequest(
                PickerSubmissionUploadEffectHandler.REQUEST_PICK_IMAGE_GALLERY
            )
        )
    }

    @Test
    fun `isPickerRequest with REQUEST_PICK_FILE_FROM_DEVICE code returns true`() {
        assertTrue(
            PickerSubmissionUploadEffectHandler.isPickerRequest(
                PickerSubmissionUploadEffectHandler.REQUEST_PICK_FILE_FROM_DEVICE
            )
        )
    }

    @Test
    fun `isPickerRequest with invalid code return false`() {
        assertFalse(PickerSubmissionUploadEffectHandler.isPickerRequest(1))
    }

    @Test
    fun `RemoveTempFile removes temp file`() {
        mockkObject(FileUploadUtils)
        connection.accept(PickerSubmissionUploadEffect.RemoveTempFile("path"))

        verify {
            FileUploadUtils.deleteTempFile("path")
        }

        unmockkObject(FileUploadUtils)
    }
}
