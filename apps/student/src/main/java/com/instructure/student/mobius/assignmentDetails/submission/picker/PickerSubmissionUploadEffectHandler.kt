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
import com.instructure.canvasapi2.utils.exhaustive
import com.instructure.pandautils.models.FileSubmitObject
import com.instructure.pandautils.utils.*
import com.instructure.student.R
import com.instructure.student.mobius.assignmentDetails.submission.picker.ui.PickerSubmissionUploadView
import com.instructure.student.mobius.common.ui.EffectHandler
import com.instructure.student.mobius.common.ui.SubmissionService
import com.spotify.mobius.Connection
import com.spotify.mobius.functions.Consumer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

// We need a context in this class to register receivers and to access the database
class PickerSubmissionUploadEffectHandler(private val context: Context) :
    EffectHandler<PickerSubmissionUploadView, PickerSubmissionUploadEvent, PickerSubmissionUploadEffect>() {

    override fun connect(output: Consumer<PickerSubmissionUploadEvent>): Connection<PickerSubmissionUploadEffect> {
        EventBus.getDefault().register(this)
        return super.connect(output)
    }

    override fun dispose() {
        EventBus.getDefault().unregister(this)
        super.dispose()
    }

    @Suppress("unused", "UNUSED_PARAMETER")
    @Subscribe(sticky = true)
    fun onActivityResults(event: OnActivityResults) {
        event.get {
            event.remove()//Remove the event so it doesn't show up again somewhere else.
            if(it.resultCode == Activity.RESULT_OK) {
                if (it.requestCode == PickerSubmissionUploadView.REQUEST_CAMERA_PIC) {
                    // Attempt to restore URI in case were were booted from memory
                    val cameraImageUri = Uri.parse(FilePrefs.tempCaptureUri)

                    //if it's still null, tell the user there is an error and return.
                    if (cameraImageUri == null) {
                        view?.showFileResultErrorMessage(R.string.utils_errorGettingPhoto)
                        return@get
                    }

                    consumer.accept(PickerSubmissionUploadEvent.OnFileSelected(cameraImageUri))
                } else if(it.data != null && it.data?.data != null) {
                    consumer.accept(PickerSubmissionUploadEvent.OnFileSelected(it.data!!.data!!))
                } else {
                    view?.showFileResultErrorMessage(R.string.unexpectedErrorOpeningFile)
                }
            }
        }
    }

    override fun accept(effect: PickerSubmissionUploadEffect) {
        when (effect) {
            PickerSubmissionUploadEffect.LaunchCamera -> {
                view?.launchCamera()
            }
            PickerSubmissionUploadEffect.LaunchGallery -> {
                view?.launchGallery()
            }
            PickerSubmissionUploadEffect.LaunchSelectFile -> {
                view?.launchSelectFile()
            }
            is PickerSubmissionUploadEffect.LoadFileContents -> {
                loadFile(effect.allowedExtensions, effect.uri, context)
            }
            is PickerSubmissionUploadEffect.HandleSubmit -> {
                handleSubmit(effect.model)
            }
        }.exhaustive
    }

    private fun loadFile(allowedExtensions: List<String>, uri: Uri, context: Context) {
        launch(Dispatchers.Main) {
            val contentResolver = context.contentResolver
            val mimeType = FileUploadUtils.getFileMimeType(contentResolver, uri)
            val fileName = FileUploadUtils.getFileNameWithDefault(contentResolver, uri, mimeType)
            val submitObject =
                FileUploadUtils.getFileSubmitObjectFromInputStream(context, uri, fileName, mimeType)

            submitObject?.let {
                if (it.errorMessage.isNullOrBlank()) {
                    if (isExtensionAllowed(it, allowedExtensions)) {
                        consumer.accept(PickerSubmissionUploadEvent.OnFileAdded(it))
                    } else {
                        view?.showBadExtensionDialog(allowedExtensions)
                    }
                } else {
                    view?.showFileErrorMessage(it.errorMessage)
                }
            }
        }
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

    private fun handleSubmit(model: PickerSubmissionUploadModel) {
        if (model.isMediaSubmission) {
            SubmissionService.startMediaSubmission(
                context,
                model.canvasContext,
                model.assignmentId,
                model.assignmentName
            )
        } else {
            SubmissionService.startFileSubmission(
                context,
                model.canvasContext,
                model.assignmentId,
                model.assignmentName,
                model.assignmentGroupCategoryId,
                ArrayList(model.files)
            )
        }
        view?.closeSubmissionView()
    }
}
