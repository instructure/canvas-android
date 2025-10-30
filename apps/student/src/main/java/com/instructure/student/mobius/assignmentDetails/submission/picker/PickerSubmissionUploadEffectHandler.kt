/*
 * Copyright (C) 2019 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.student.mobius.assignmentDetails.submission.picker

import android.app.Activity
import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.instructure.canvasapi2.models.postmodels.FileSubmitObject
import com.instructure.canvasapi2.utils.exhaustive
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.FilePrefs
import com.instructure.pandautils.utils.FileUploadUtils
import com.instructure.pandautils.utils.OnActivityResults
import com.instructure.pandautils.utils.PermissionUtils
import com.instructure.pandautils.utils.getFragmentActivity
import com.instructure.pandautils.utils.remove
import com.instructure.pandautils.utils.requestPermissions
import com.instructure.student.R
import com.instructure.student.mobius.assignmentDetails.isIntentAvailable
import com.instructure.student.mobius.assignmentDetails.submission.picker.PickerSubmissionMode.CommentAttachment
import com.instructure.student.mobius.assignmentDetails.submission.picker.PickerSubmissionMode.FileSubmission
import com.instructure.student.mobius.assignmentDetails.submission.picker.PickerSubmissionMode.MediaSubmission
import com.instructure.student.mobius.assignmentDetails.submission.picker.ui.PickerSubmissionUploadView
import com.instructure.student.mobius.common.ui.EffectHandler
import com.instructure.student.mobius.common.ui.SubmissionHelper
import com.spotify.mobius.Connection
import com.spotify.mobius.functions.Consumer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.io.File

// We need a context in this class to register receivers and to access the database
class PickerSubmissionUploadEffectHandler(
    private val context: Context,
    private val submissionHelper: SubmissionHelper
) : EffectHandler<PickerSubmissionUploadView, PickerSubmissionUploadEvent, PickerSubmissionUploadEffect>() {

    override fun connect(output: Consumer<PickerSubmissionUploadEvent>): Connection<PickerSubmissionUploadEffect> {
        EventBus.getDefault().register(this)
        return super.connect(output)
    }

    override fun dispose() {
        EventBus.getDefault().unregister(this)
        super.dispose()
    }

    val subId = PickerSubmissionUploadEffectHandler::class.java.name

    @Suppress("unused", "UNUSED_PARAMETER")
    @Subscribe(sticky = true)
    fun onActivityResults(event: OnActivityResults) {
        event.once(subId) {
            if (it.resultCode == Activity.RESULT_OK) {
                if (it.requestCode == REQUEST_CAMERA_PIC) {
                    event.remove() //Remove the event so it doesn't show up again somewhere else

                    // Attempt to restore URI in case were were booted from memory
                    val cameraImageUri = Uri.parse(FilePrefs.tempCaptureUri)

                    // If it's still null, tell the user there is an error and return.
                    if (cameraImageUri == null) {
                        view?.showErrorMessage(R.string.utils_errorGettingPhoto)
                        return@once
                    }

                    consumer.accept(PickerSubmissionUploadEvent.OnFileSelected(cameraImageUri))
                } else if (it.requestCode in listOf(REQUEST_PICK_IMAGE_GALLERY, REQUEST_PICK_FILE_FROM_DEVICE)) {
                    event.remove() //Remove the event so it doesn't show up again somewhere else

                    if (it.data != null && it.data?.data != null) {
                        consumer.accept(PickerSubmissionUploadEvent.OnFileSelected(it.data!!.data!!))
                    } else {
                        view?.showErrorMessage(R.string.unexpectedErrorOpeningFile)
                    }
                } else if (it.requestCode == REQUEST_DOCUMENT_SCANNING) {
                    event.remove()

                    if (it.data != null && it.data?.data != null) {
                        consumer.accept(PickerSubmissionUploadEvent.OnFileSelected(it.data!!.data!!))
                    } else {
                        view?.showErrorMessage(R.string.unexpectedErrorOpeningFile)
                    }
                }
            }
        }
    }

    override fun accept(effect: PickerSubmissionUploadEffect) {
        when (effect) {
            PickerSubmissionUploadEffect.LaunchCamera -> {
                launchCamera()
            }
            PickerSubmissionUploadEffect.LaunchGallery -> {
                launchGallery()
            }
            PickerSubmissionUploadEffect.LaunchSelectFile -> {
                launchSelectFile()
            }
            is PickerSubmissionUploadEffect.LoadFileContents -> {
                loadFile(effect.allowedExtensions, effect.uri, context)
            }
            is PickerSubmissionUploadEffect.HandleSubmit -> {
                handleSubmit(effect.model)
            }
            is PickerSubmissionUploadEffect.RemoveTempFile -> {
                removeTempFile(effect.path)
            }
        }.exhaustive
    }

    private fun handleSubmit(model: PickerSubmissionUploadModel) {
        when (model.mode) {
            MediaSubmission -> {
                val file = model.files.first()
                val mediaType = when {
                    file.contentType?.startsWith("video/") == true -> "video"
                    file.contentType?.startsWith("audio/") == true -> "audio"
                    else -> null
                }
                submissionHelper.startMediaSubmission(
                    canvasContext = model.canvasContext,
                    assignmentId = model.assignmentId,
                    assignmentName = model.assignmentName,
                    assignmentGroupCategoryId = model.assignmentGroupCategoryId,
                    mediaFilePath = file.fullPath,
                    attempt = model.attemptId ?: 1L,
                    mediaType = mediaType,
                    mediaSource = model.mediaSource
                )
            }
            FileSubmission -> {
                submissionHelper.startFileSubmission(
                    canvasContext = model.canvasContext,
                    assignmentId = model.assignmentId,
                    assignmentName = model.assignmentName,
                    assignmentGroupCategoryId = model.assignmentGroupCategoryId,
                    files = ArrayList(model.files),
                    attempt = model.attemptId ?: 1L
                )
            }
            CommentAttachment -> {
                submissionHelper.startCommentUpload(
                    canvasContext = model.canvasContext,
                    assignmentId = model.assignmentId,
                    assignmentName = model.assignmentName,
                    message = null,
                    attachments = model.files,
                    isGroupMessage = model.assignmentGroupCategoryId > 0,
                    attemptId = model.attemptId
                )
            }
        }
        view?.closeSubmissionView()
    }

    //region Media Fetching

    private fun loadFile(allowedExtensions: List<String>, uri: Uri, context: Context) {
        launch(Dispatchers.Main) {
            val submitObject = withContext(Dispatchers.IO) {
                FileUploadUtils.getFile(context, uri)
            }

            submitObject?.let {
                var fileToSubmit: FileSubmitObject? = null
                if (it.errorMessage.isNullOrBlank()) {
                    if (isExtensionAllowed(it, allowedExtensions)) {
                        fileToSubmit = it
                    } else {
                        view?.showBadExtensionDialog(allowedExtensions)
                    }
                } else {
                    view?.showFileErrorMessage(it.errorMessage.orEmpty())
                }
                consumer.accept(PickerSubmissionUploadEvent.OnFileAdded(fileToSubmit))
            }
        }
    }

    private fun launchGallery() {
        val file = File(context.filesDir, "/submission/*")
        val uri = FileProvider.getUriForFile(
            context,
            context.packageName + Const.FILE_PROVIDER_AUTHORITY,
            file
        )

        view?.getGalleryIntent(uri)?.let {
            (context.getFragmentActivity()).startActivityForResult(it, REQUEST_PICK_IMAGE_GALLERY)
        }
    }

    private fun launchSelectFile() {
        view?.getSelectFileIntent()?.let {
            (context.getFragmentActivity()).startActivityForResult(it, REQUEST_PICK_FILE_FROM_DEVICE)
        }
    }

    private fun launchCamera() {
        // Get camera permission if we need it
        if (needsPermissions(
                PickerSubmissionUploadEvent.CameraClicked,
                PermissionUtils.CAMERA
            )
        ) return

        // Store the uri that we're saving the file to
        val fileName = "pic_${System.currentTimeMillis()}.jpg"
        val file = File(FileUploadUtils.getExternalCacheDir(context), fileName)
        val uri = FileProvider.getUriForFile(
            context,
            context.packageName + Const.FILE_PROVIDER_AUTHORITY,
            file
        )
        if (uri != null) {
            FilePrefs.tempCaptureUri = uri.toString()
        }

        // Create new Intent and launch
        val intent = view?.getCameraIntent(uri)

        if (intent != null && context.isIntentAvailable(intent.action)) {
            (context.getFragmentActivity()).startActivityForResult(intent, REQUEST_CAMERA_PIC)
        }
    }

    // Helper functions for handling media

    private fun needsPermissions(
        successEvent: PickerSubmissionUploadEvent,
        vararg permissions: String
    ): Boolean {
        if (PermissionUtils.hasPermissions(context.getFragmentActivity(), *permissions)) {
            return false
        }

        context.getFragmentActivity().requestPermissions(setOf(*permissions)) { results ->
            if (results.isNotEmpty() && results.all { it.value }) {
                // If permissions list is not empty and all are granted, send the success event
                consumer.accept(successEvent)
            } else {
                view?.showErrorMessage(R.string.permissionDenied)
            }
        }
        return true
    }

    private fun isExtensionAllowed(
        file: FileSubmitObject,
        allowedExtensions: List<String>
    ): Boolean {
        if (allowedExtensions.isEmpty()) {
            return true // No restrictions if empty
        }

        // Get the extension and compare it to the list of allowed extensions
        val index = file.fullPath.lastIndexOf(".")
        if (index != -1) {
            val ext = file.fullPath.substring(index + 1)
            for (i in 0 until (allowedExtensions.size)) {
                if (allowedExtensions[i].trim { it <= ' ' }.equals(ext, ignoreCase = true)) {
                    return true
                }
            }
        }

        return false
    }

    private fun removeTempFile(path: String) {
        FileUploadUtils.deleteTempFile(path)
    }

    companion object {
        const val REQUEST_CAMERA_PIC = 5100
        const val REQUEST_PICK_IMAGE_GALLERY = 5101
        const val REQUEST_PICK_FILE_FROM_DEVICE = 5102
        const val REQUEST_DOCUMENT_SCANNING = 5103

        fun isPickerRequest(code: Int): Boolean {
            return code in listOf(
                REQUEST_CAMERA_PIC,
                REQUEST_PICK_IMAGE_GALLERY,
                REQUEST_PICK_FILE_FROM_DEVICE,
                REQUEST_DOCUMENT_SCANNING
            )

        }
    }
}
