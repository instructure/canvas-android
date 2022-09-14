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
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.postmodels.FileSubmitObject
import com.instructure.pandautils.R
import com.instructure.pandautils.features.file.upload.itemviewmodels.FileItemViewModel
import com.instructure.pandautils.features.file.upload.worker.FileUploadBundleCreator
import com.instructure.pandautils.features.file.upload.worker.FileUploadWorker
import com.instructure.pandautils.mvvm.Event
import com.instructure.pandautils.utils.humanReadableByteCount
import com.instructure.pandautils.utils.orDefault
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.*
import javax.inject.Inject

@HiltViewModel
class FileUploadDialogViewModel @Inject constructor(
        private val fileUploadUtils: FileUploadUtilsHelper,
        private val resources: Resources,
        private val workManager: WorkManager,
        private val fileUploadBundleCreator: FileUploadBundleCreator
) : ViewModel() {

    val data: LiveData<FileUploadDialogViewData>
        get() = _data
    private val _data = MutableLiveData<FileUploadDialogViewData>()

    val events: LiveData<Event<FileUploadAction>>
        get() = _events
    private val _events = MutableLiveData<Event<FileUploadAction>>()

    private var assignment: Assignment? = null
    private var uploadType: FileUploadType = FileUploadType.ASSIGNMENT
    private var canvasContext: CanvasContext = CanvasContext.defaultCanvasContext()
    private var userId: Long? = null
    private var isOneFileOnly = false
    private var parentFolderId: Long? = null
    private var quizQuestionId: Long = -1L
    private var quizId: Long = -1L
    private var position: Int = -1

    var dialogCallback: ((Int) -> Unit)? = null
    var attachmentCallback: ((Int, FileSubmitObject?) -> Unit)? = null
    var selectedUriStringsCallback: ((List<String>) -> Unit)? = null
    var workerCallback: ((UUID, LiveData<WorkInfo>) -> Unit)? = null

    private var filesToUpload = mutableListOf<FileUploadData>()

    fun setData(
        assignment: Assignment?,
        files: ArrayList<Uri>?,
        uploadType: FileUploadType,
        canvasContext: CanvasContext,
        parentFolderId: Long,
        quizQuestionId: Long,
        position: Int,
        quizId: Long,
        userId: Long,
        dialogCallback: ((Int) -> Unit)? = null,
        attachmentCallback: ((Int, FileSubmitObject?) -> Unit)? = null,
        selectedFilePathsCallback: ((List<String>) -> Unit)? = null,
        workerCallback: ((UUID, LiveData<WorkInfo>) -> Unit)? = null
    ) {
        this.assignment = assignment
        files?.forEach { uri ->
            val submitObject = getUriContents(uri)
            submitObject?.let { fso ->
                this.filesToUpload.add(FileUploadData(uri, fso))
            }
        }
        this.uploadType = uploadType
        this.canvasContext = canvasContext
        this.isOneFileOnly = uploadType == FileUploadType.QUIZ || uploadType == FileUploadType.DISCUSSION
        this.parentFolderId = parentFolderId
        this.quizQuestionId = quizQuestionId
        this.quizId = quizId
        this.position = position
        this.userId = userId
        dialogCallback?.let {
            this.dialogCallback = it
        }
        attachmentCallback?.let {
            this.attachmentCallback = it
        }
        selectedFilePathsCallback?.let {
            this.selectedUriStringsCallback = it
        }
        workerCallback?.let {
            this.workerCallback = it
        }
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
        val submitObject = getUriContents(fileUri)
        submitObject?.let {
            if (it.errorMessage.isNullOrEmpty()) {
                val added = addIfExtensionAllowed(fileUri, it)
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
        val itemViewModels = filesToUpload.map {
            FileItemViewModel(FileItemViewData(
                    it.fileSubmitObject.name,
                    it.fileSubmitObject.size.humanReadableByteCount(),
                    it.fileSubmitObject.fullPath
            ), this::onRemoveFileClicked)
        }
        _data.postValue(FileUploadDialogViewData(
                setupAllowedExtensions(),
                itemViewModels))
    }

    private fun onRemoveFileClicked(fullPath: String) {
        filesToUpload.removeIf {
            it.fileSubmitObject.fullPath == fullPath
        }
        updateItems()
    }

    private fun getUriContents(fileUri: Uri): FileSubmitObject? {
        val mimeType = fileUploadUtils.getFileMimeType(fileUri)
        val fileName = fileUploadUtils.getFileNameWithDefault(fileUri)

        return fileUploadUtils.getFileSubmitObjectFromInputStream(fileUri, fileName, mimeType)
    }

    private fun addIfExtensionAllowed(uri: Uri, fileSubmitObject: FileSubmitObject): Boolean {
        if (assignment != null && (assignment?.allowedExtensions == null || assignment?.allowedExtensions?.size == 0)) {
            filesToUpload.add(FileUploadData(uri, fileSubmitObject))
            return true
        }

        //get the extension and compare it to the list of allowed extensions
        val index = fileSubmitObject.fullPath.lastIndexOf(".")
        if (assignment != null && index != -1) {
            val ext = fileSubmitObject.fullPath.substring(index + 1)
            for (i in 0 until (assignment?.allowedExtensions?.size ?: 0)) {
                if (assignment!!.allowedExtensions[i].trim { it <= ' ' }.equals(ext, ignoreCase = true)) {
                    filesToUpload.add(FileUploadData(uri, fileSubmitObject))
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
            filesToUpload.add(FileUploadData(uri, fileSubmitObject))
            return true
        }

        _events.postValue(Event(FileUploadAction.ShowToast(resources.getString(R.string.extensionNotAllowed))))
        return false
    }

    private fun checkIfFileSubmissionAllowed(): Boolean {
        return assignment?.submissionTypesRaw?.contains(Assignment.SubmissionType.ONLINE_UPLOAD.apiString) ?: false
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
        if (filesToUpload.size == 0) {
            _events.postValue(Event(FileUploadAction.ShowToast(resources.getString(R.string.noFilesUploaded))))
        } else {
            if (uploadType == FileUploadType.ASSIGNMENT) {

                if (!checkIfFileSubmissionAllowed()) { //see if we can actually submit files to this assignment
                    _events.postValue(Event(FileUploadAction.ShowToast(resources.getString(R.string.fileUploadNotSupported))))
                    return
                }

                filesToUpload.forEach {
                    if (!isExtensionAllowed(it.fileSubmitObject.fullPath)) {
                        _events.postValue(Event(FileUploadAction.ShowToast(resources.getString(R.string.oneOrMoreExtensionNotAllowed))))
                        return
                    }
                }
            }

            val uris = filesToUpload.map { it.uri }

            val data: Data = when (uploadType) {
                FileUploadType.USER -> {
                    fileUploadBundleCreator.getUserFilesBundle(uris, parentFolderId)
                            .putString(FileUploadWorker.FILE_SUBMIT_ACTION, FileUploadWorker.ACTION_USER_FILE)
                            .build()
                }
                FileUploadType.COURSE -> {
                    fileUploadBundleCreator.getCourseFilesBundle(uris, canvasContext.id, parentFolderId)
                            .putString(FileUploadWorker.FILE_SUBMIT_ACTION, FileUploadWorker.ACTION_COURSE_FILE)
                            .build()
                }
                FileUploadType.GROUP -> {
                    fileUploadBundleCreator.getCourseFilesBundle(uris, canvasContext.id, parentFolderId)
                            .putString(FileUploadWorker.FILE_SUBMIT_ACTION, FileUploadWorker.ACTION_GROUP_FILE)
                            .build()
                }
                FileUploadType.MESSAGE -> {
                    fileUploadBundleCreator.getUserFilesBundle(uris, null)
                            .putString(FileUploadWorker.FILE_SUBMIT_ACTION, FileUploadWorker.ACTION_MESSAGE_ATTACHMENTS)
                            .build()
                }
                FileUploadType.DISCUSSION -> {
                    fileUploadBundleCreator.getUserFilesBundle(uris, null)
                            .putString(FileUploadWorker.FILE_SUBMIT_ACTION, FileUploadWorker.ACTION_DISCUSSION_ATTACHMENT)
                            .build()
                }
                FileUploadType.QUIZ -> {
                    fileUploadBundleCreator.getQuizFileBundle(uris, parentFolderId, quizQuestionId, position, canvasContext.id, quizId)
                            .putString(FileUploadWorker.FILE_SUBMIT_ACTION, FileUploadWorker.ACTION_QUIZ_FILE)
                            .build()
                }
                FileUploadType.SUBMISSION_COMMENT -> {
                    fileUploadBundleCreator.getSubmissionCommentBundle(uris, canvasContext.id, assignment!!)
                            .putString(FileUploadWorker.FILE_SUBMIT_ACTION, FileUploadWorker.ACTION_SUBMISSION_COMMENT)
                            .build()
                }
                FileUploadType.TEACHER_SUBMISSION_COMMENT -> {
                    fileUploadBundleCreator.getTeacherSubmissionCommentBundle(
                        uris,
                        assignment?.courseId.orDefault(),
                        assignment?.id.orDefault(),
                        userId.orDefault()
                    ).build()
                }
                else -> {
                    fileUploadBundleCreator.getAssignmentSubmissionBundle(uris, canvasContext.id, assignment!!)
                            .putString(FileUploadWorker.FILE_SUBMIT_ACTION, FileUploadWorker.ACTION_ASSIGNMENT_SUBMISSION)
                            .build()

                }
            }

            startUpload(data)
        }
    }

    private fun getAttachmentUri(): FileSubmitObject? {
        return filesToUpload.firstOrNull()?.fileSubmitObject
    }

    private fun startUpload(data: Data) {
        if (uploadType == FileUploadType.DISCUSSION) {
            attachmentCallback?.invoke(FileUploadDialogFragment.EVENT_ON_FILE_SELECTED, getAttachmentUri())
        } else {
            val worker = OneTimeWorkRequestBuilder<FileUploadWorker>()
                    .setInputData(data)
                    .build()

            selectedUriStringsCallback?.invoke(filesToUpload.map { it.uri.toString() })
            workerCallback?.invoke(worker.id, workManager.getWorkInfoByIdLiveData(worker.id))
            workManager.enqueue(worker)
            dialogCallback?.invoke(FileUploadDialogFragment.EVENT_ON_UPLOAD_BEGIN)
        }
        _events.postValue(Event(FileUploadAction.UploadStarted))
    }

    fun onCancelClicked() {
        dialogCallback?.invoke(FileUploadDialogFragment.EVENT_DIALOG_CANCELED)
        attachmentCallback?.invoke(FileUploadDialogFragment.EVENT_DIALOG_CANCELED, null)
    }
}