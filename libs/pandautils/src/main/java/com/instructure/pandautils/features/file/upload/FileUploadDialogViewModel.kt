/*
 * Copyright (C) 2022 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.instructure.pandautils.features.file.upload

import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.work.Data
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.postmodels.FileSubmitObject
import com.instructure.pandautils.R
import com.instructure.pandautils.features.file.upload.itemviewmodels.FileItemViewModel
import com.instructure.pandautils.mvvm.Event
import com.instructure.pandautils.services.FileUploadService
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.*
import javax.inject.Inject
import kotlin.math.ln
import kotlin.math.pow

@HiltViewModel
class FileUploadDialogViewModel @Inject constructor(
        private val fileUploadUtils: FileUploadUtilsHelper,
        private val resources: Resources
) : ViewModel() {

    val data: LiveData<FileUploadDialogViewData>
        get() = _data
    private val _data = MutableLiveData<FileUploadDialogViewData>()

    val events: LiveData<Event<FileUploadAction>>
        get() = _events
    private val _events = MutableLiveData<Event<FileUploadAction>>()

    private var assignment: Assignment? = null
    private var submitObjects: MutableList<FileSubmitObject> = mutableListOf()
    private var uploadType: FileUploadType = FileUploadType.ASSIGNMENT
    private var canvasContext: CanvasContext = CanvasContext.defaultCanvasContext()
    private var isOneFileOnly = false
    private var parentFolderId: Long? = null
    private var quizQuestionId: Long = -1L
    private var quizId: Long = -1L
    private var position: Int = -1

    private var uris = mutableListOf<Uri>()

    fun setData(assignment: Assignment?, file: Uri?, uploadType: FileUploadType, canvasContext: CanvasContext, parentFolderId: Long, quizQuestionId: Long, position: Int, quizId: Long) {
        this.assignment = assignment
        val submitObject = file?.let {
            uris.add(it)
            getUriContents(it)
        }
        this.submitObjects = submitObject?.let { mutableListOf(it) } ?: mutableListOf()
        this.uploadType = uploadType
        this.canvasContext = canvasContext
        this.isOneFileOnly = uploadType == FileUploadType.QUIZ || uploadType == FileUploadType.DISCUSSION
        this.parentFolderId = parentFolderId
        this.quizQuestionId = quizQuestionId
        this.quizId = quizId
        this.position = position
        updateItems()
    }

    fun onCameraClicked() {
        _events.postValue(Event(FileUploadAction.TakePhoto))
    }

    fun onGalleryClicked() {
        _events.postValue(Event(FileUploadAction.PickPhoto))
    }

    fun onFilesClicked() {
        _events.postValue(Event(FileUploadAction.PickFile))
    }

    fun addFile(fileUri: Uri) {
        uris.add(fileUri)
        val submitObject = getUriContents(fileUri)
        submitObject?.let {
            if (it.errorMessage.isNullOrEmpty()) {
                val added = addIfExtensionAllowed(it)
                if (added) {
                    updateItems()
                }
            } else {
                _events.postValue(Event(FileUploadAction.ShowToast(it.errorMessage
                        ?: resources.getString(R.string.errorOccurred))))
            }
        } ?: _events.postValue(Event(FileUploadAction.ShowToast(resources.getString(R.string.errorOccurred))))
    }

    private fun updateItems() {
        val itemViewModels = submitObjects.map {
            FileItemViewModel(FileItemViewData(
                    it.name,
                    humanReadableByteCount(it.size),
                    it.fullPath
            ), this::onRemoveFileClicked)
        }
        _data.postValue(FileUploadDialogViewData(
                setupAllowedExtensions(),
                itemViewModels))
    }

    private fun onRemoveFileClicked(fullPath: String) {
        val index = submitObjects.indexOfFirst { it.fullPath == fullPath }
        submitObjects.removeAt(index)
        uris.removeAt(index)
        updateItems()
    }

    private fun getUriContents(fileUri: Uri): FileSubmitObject? {
        val mimeType = fileUploadUtils.getFileMimeType(fileUri)
        val fileName = fileUploadUtils.getFileNameWithDefault(fileUri)

        return fileUploadUtils.getFileSubmitObjectFromInputStream(fileUri, fileName, mimeType)
    }

    private fun addIfExtensionAllowed(fileSubmitObject: FileSubmitObject): Boolean {
        if (assignment != null && (assignment?.allowedExtensions == null || assignment?.allowedExtensions?.size == 0)) {
            submitObjects.add(fileSubmitObject)
            return true
        }

        //get the extension and compare it to the list of allowed extensions
        val index = fileSubmitObject.fullPath.lastIndexOf(".")
        if (assignment != null && index != -1) {
            val ext = fileSubmitObject.fullPath.substring(index + 1)
            for (i in 0 until (assignment?.allowedExtensions?.size ?: 0)) {
                if (assignment!!.allowedExtensions[i].trim { it <= ' ' }.equals(ext, ignoreCase = true)) {
                    submitObjects.add(fileSubmitObject)
                    return true
                }
            }
            _events.postValue(Event(FileUploadAction.ShowToast(resources.getString(R.string.extensionNotAllowed))))
            return false
        }

        //if we're sharing it from an external source we won't know which assignment they're trying to
        //submit to, so we won't know if there are any extension limits
        //also, the assignment and/or course could be null due to memory pressures
        if (assignment == null || canvasContext.id != 0L) {
            submitObjects.add(fileSubmitObject)
            return true
        }

        _events.postValue(Event(FileUploadAction.ShowToast(resources.getString(R.string.extensionNotAllowed))))
        return false
    }

    private fun checkIfFileSubmissionAllowed(): Boolean {
        return assignment?.submissionTypesRaw?.contains(Assignment.SubmissionType.ONLINE_UPLOAD.apiString) ?: false
    }

    private fun humanReadableByteCount(bytes: Long): String {
        val unit = 1024
        if (bytes < unit) return "$bytes B"
        val exp = (ln(bytes.toDouble()) / ln(unit.toDouble())).toInt()
        val pre = "KMGTPE"[exp - 1].toString()
        return String.format(Locale.getDefault(), "%.1f %sB", bytes / unit.toDouble().pow(exp.toDouble()), pre)
    }

    private fun setupAllowedExtensions(): String? {
        return if (uploadType != FileUploadType.SUBMISSION_COMMENT && assignment != null && !assignment?.allowedExtensions.isNullOrEmpty()) {
            assignment?.let {
                var extensions = resources.getString(R.string.allowedExtensions)

                for (i in 0 until it.allowedExtensions.size) {
                    extensions += it.allowedExtensions[i]
                    if (it.allowedExtensions.size > 1 && i < it.allowedExtensions.size - 1) {
                        extensions += ","
                    }
                }
                extensions
            }
        } else {
            null
        }
    }

    private fun isExtensionAllowed(filePath: String): Boolean {
        if (assignment == null) {
            _events.postValue(Event(FileUploadAction.ShowToast(resources.getString(R.string.noAssignmentSelected))))
            return false
        }
        if (assignment!!.allowedExtensions.isEmpty()) return true

        val extension = filePath.substringAfterLast(".")

        return assignment!!.allowedExtensions.contains(extension)
    }

    fun uploadFiles() {
        if (submitObjects.size == 0) {
            _events.postValue(Event(FileUploadAction.ShowToast(resources.getString(R.string.noFilesUploaded))))
        } else {
            if (uploadType == FileUploadType.ASSIGNMENT) {

                if (!checkIfFileSubmissionAllowed()) { //see if we can actually submit files to this assignment
                    _events.postValue(Event(FileUploadAction.ShowToast(resources.getString(R.string.fileUploadNotSupported))))
                    return
                }

                submitObjects.forEach {
                    if (!isExtensionAllowed(it.fullPath)) {
                        _events.postValue(Event(FileUploadAction.ShowToast(resources.getString(R.string.oneOrMoreExtensionNotAllowed))))
                        return
                    }
                }
            }

            val fileList = ArrayList(submitObjects)

            val data: Data = when (uploadType) {
                FileUploadType.USER -> {
                    FileUploadWorker.getUserFilesBundle(uris, parentFolderId)
                            .putString(FileUploadWorker.FILE_SUBMIT_ACTION, FileUploadWorker.ACTION_USER_FILE)
                            .build()
                }
                FileUploadType.COURSE -> {
                    FileUploadWorker.getCourseFilesBundle(uris, canvasContext.id, parentFolderId)
                            .putString(FileUploadWorker.FILE_SUBMIT_ACTION, FileUploadWorker.ACTION_COURSE_FILE)
                            .build()
                }
                FileUploadType.GROUP -> {
                    FileUploadWorker.getCourseFilesBundle(uris, canvasContext.id, parentFolderId)
                            .putString(FileUploadWorker.FILE_SUBMIT_ACTION, FileUploadWorker.ACTION_GROUP_FILE)
                            .build()
                }
                FileUploadType.MESSAGE -> {
                    FileUploadWorker.getUserFilesBundle(uris, null)
                            .putString(FileUploadWorker.FILE_SUBMIT_ACTION, FileUploadWorker.ACTION_MESSAGE_ATTACHMENTS)
                            .build()
                }
                FileUploadType.DISCUSSION -> {
                    FileUploadWorker.getUserFilesBundle(uris, null)
                            .putString(FileUploadWorker.FILE_SUBMIT_ACTION, FileUploadWorker.ACTION_DISCUSSION_ATTACHMENT)
                            .build()
                }
                FileUploadType.QUIZ -> {
                    FileUploadWorker.getQuizFileBundle(uris, parentFolderId, quizQuestionId, position, canvasContext.id, quizId)
                            .putString(FileUploadWorker.FILE_SUBMIT_ACTION, FileUploadWorker.ACTION_QUIZ_FILE)
                            .build()
                }
                FileUploadType.SUBMISSION_COMMENT -> {
                    FileUploadWorker.getSubmissionCommentBundle(uris, canvasContext.id, assignment!!)
                            .putString(FileUploadWorker.FILE_SUBMIT_ACTION, FileUploadWorker.ACTION_SUBMISSION_COMMENT)
                            .build()
                }
                else -> {
                    FileUploadWorker.getAssignmentSubmissionBundle(uris, canvasContext.id, assignment!!)
                            .putString(FileUploadWorker.FILE_SUBMIT_ACTION, FileUploadWorker.ACTION_ASSIGNMENT_SUBMISSION)
                            .build()

                }
            }

            _events.postValue(Event(FileUploadAction.StartUpload(data)))
        }
    }

    fun getAttachmentUri(): FileSubmitObject? {
        return submitObjects.firstOrNull()
    }
}