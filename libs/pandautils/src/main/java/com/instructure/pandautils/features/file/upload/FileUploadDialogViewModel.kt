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
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.postmodels.FileSubmitObject
import com.instructure.pandautils.R
import com.instructure.pandautils.features.file.upload.itemviewmodels.FileItemViewModel
import com.instructure.pandautils.features.file.upload.worker.FileUploadWorker
import com.instructure.pandautils.mvvm.Event
import com.instructure.pandautils.room.appdatabase.daos.FileUploadInputDao
import com.instructure.pandautils.room.appdatabase.entities.FileUploadInputEntity
import com.instructure.pandautils.utils.humanReadableByteCount
import com.instructure.pandautils.utils.orDefault
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class FileUploadDialogViewModel @Inject constructor(
        private val fileUploadUtils: FileUploadUtilsHelper,
        private val resources: Resources,
        private val workManager: WorkManager,
        private val fileUploadInputDao: FileUploadInputDao
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
    private var attemptId: Long? = null

    var dialogCallback: ((Int) -> Unit)? = null

    private var filesToUpload = mutableListOf<FileSubmitObject>()

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
        attemptId: Long?,
        dialogCallback: ((Int) -> Unit)? = null
    ) {
        this.assignment = assignment
        files?.forEach { uri ->
            val submitObject = getUriContents(uri)
            submitObject?.let { fso ->
                this.filesToUpload.add(fso)
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
        this.attemptId = attemptId
        dialogCallback?.let {
            this.dialogCallback = it
        }
        updateItems()
    }

    fun onCameraClicked() {
        if (isOneFileOnly && filesToUpload.isNotEmpty()) {
            _events.value = Event(FileUploadAction.ShowToast(resources.getString(R.string.oneFileOnly)))
            return
        }
        _events.value = Event(FileUploadAction.TakePhoto)
    }

    fun onGalleryClicked() {
        if (isOneFileOnly && filesToUpload.isNotEmpty()) {
            _events.value = Event(FileUploadAction.ShowToast(resources.getString(R.string.oneFileOnly)))
            return
        }

        if (isOneFileOnly) {
            _events.value = Event(FileUploadAction.PickImage)
        } else {
            _events.value = Event(FileUploadAction.PickMultipleImage)
        }
    }

    fun onFilesClicked() {
        if (isOneFileOnly && filesToUpload.isNotEmpty()) {
            _events.value = Event(FileUploadAction.ShowToast(resources.getString(R.string.oneFileOnly)))
            return
        }

        if (isOneFileOnly) {
            _events.value = Event(FileUploadAction.PickFile)
        } else {
            _events.value = Event(FileUploadAction.PickMultipleFile)
        }
    }

    fun addFile(fileUri: Uri) {
        val submitObject = getUriContents(fileUri)
        if (submitObject != null) {
            if (submitObject.errorMessage.isNullOrEmpty()) {
                val added = addIfExtensionAllowed(submitObject)
                if (added) {
                    updateItems()
                }
            } else {
                _events.value = Event(FileUploadAction.ShowToast(submitObject.errorMessage
                        ?: resources.getString(R.string.errorOccurred)))
            }
        } else {
            _events.value = Event(FileUploadAction.ShowToast(resources.getString(R.string.errorOccurred)))
        }
    }

    fun addFiles(fileUris: List<Uri>) {
        fileUris.forEach {
            addFile(it)
        }
    }

    private fun updateItems() {
        val itemViewModels = filesToUpload.map {
            FileItemViewModel(FileItemViewData(
                    it.name,
                    it.size.humanReadableByteCount(),
                    it.fullPath
            ), this::onRemoveFileClicked)
        }
        _data.postValue(FileUploadDialogViewData(
                setupAllowedExtensions(),
                itemViewModels))
    }

    private fun onRemoveFileClicked(fullPath: String) {
        val removed = filesToUpload.removeIf {
            it.fullPath == fullPath
        }
        if (removed) {
            fileUploadUtils.deleteTempFile(fullPath)
        }
        updateItems()
    }

    private fun getUriContents(fileUri: Uri): FileSubmitObject? {
        val mimeType = fileUploadUtils.getFileMimeType(fileUri)
        val fileName = fileUploadUtils.getFileNameWithDefault(fileUri)

        return fileUploadUtils.getFileSubmitObjectFromInputStream(fileUri, fileName, mimeType)
    }

    private fun addIfExtensionAllowed(fileSubmitObject: FileSubmitObject): Boolean {
        if (assignment != null && assignment?.allowedExtensions.isNullOrEmpty()) {
            filesToUpload.add(fileSubmitObject)
            return true
        }

        //get the extension and compare it to the list of allowed extensions
        val index = fileSubmitObject.fullPath.lastIndexOf(".")
        if (assignment != null && index != -1) {
            val ext = fileSubmitObject.fullPath.substring(index + 1)
            for (i in 0 until (assignment?.allowedExtensions?.size ?: 0)) {
                if (assignment!!.allowedExtensions[i].trim { it <= ' ' }.equals(ext, ignoreCase = true)) {
                    filesToUpload.add(fileSubmitObject)
                    return true
                }
            }
            _events.value = Event(FileUploadAction.ShowToast(resources.getString(R.string.extensionNotAllowed)))
            return false
        }

        //if we're sharing it from an external source we won't know which assignment they're trying to
        //submit to, so we won't know if there are any extension limits
        //also, the assignment and/or course could be null due to memory pressures
        if (assignment == null || canvasContext.id != 0L) {
            filesToUpload.add(fileSubmitObject)
            return true
        }

        _events.value = Event(FileUploadAction.ShowToast(resources.getString(R.string.extensionNotAllowed)))
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
            _events.value = Event(FileUploadAction.ShowToast(resources.getString(R.string.noAssignmentSelected)))
            return false
        }
        if (assignment!!.allowedExtensions.isEmpty()) return true

        val extension = filePath.substringAfterLast(".")

        return assignment!!.allowedExtensions.contains(extension)
    }

    fun uploadFiles() {
        if (filesToUpload.size == 0) {
            _events.value = Event(FileUploadAction.ShowToast(resources.getString(R.string.noFilesUploaded)))
        } else {
            if (uploadType == FileUploadType.ASSIGNMENT) {

                if (!checkIfFileSubmissionAllowed()) { //see if we can actually submit files to this assignment
                    _events.value = Event(FileUploadAction.ShowToast(resources.getString(R.string.fileUploadNotSupported)))
                    return
                }

                filesToUpload.forEach {
                    if (!isExtensionAllowed(it.fullPath)) {
                        _events.value = Event(FileUploadAction.ShowToast(resources.getString(R.string.oneOrMoreExtensionNotAllowed)))
                        return
                    }
                }
            }

            val uris = filesToUpload.map { Uri.fromFile(File(it.fullPath)) }

            startUpload(uris)
        }
    }

    private fun getAttachmentUri() = filesToUpload.firstOrNull()

    private fun startUpload(uris: List<Uri>) {
        viewModelScope.launch {
            if (uploadType == FileUploadType.DISCUSSION) {
                _events.value = Event(FileUploadAction.AttachmentSelectedAction(FileUploadDialogFragment.EVENT_ON_FILE_SELECTED, getAttachmentUri()))
            } else {
                val worker = OneTimeWorkRequestBuilder<FileUploadWorker>()
                    .addTag("FileUploadWorker")
                    .build()

                val input = getInputData(worker.id, uris)
                fileUploadInputDao.insert(input)

                _events.value = Event(
                    FileUploadAction.UploadStartedAction(
                        worker.id,
                        workManager.getWorkInfoByIdLiveData(worker.id),
                        uris.map { it.toString() }
                    )
                )

                workManager.enqueue(worker)
                dialogCallback?.invoke(FileUploadDialogFragment.EVENT_ON_UPLOAD_BEGIN)
            }
            _events.value = Event(FileUploadAction.UploadStarted)
        }
    }

    private fun getInputData(workerId: UUID, uris: List<Uri>): FileUploadInputEntity {
        return when (uploadType) {
            FileUploadType.USER -> {
                FileUploadInputEntity(
                    workerId = workerId.toString(),
                    filePaths = uris.map { it.toString() },
                    action = FileUploadWorker.ACTION_USER_FILE,
                    parentFolderId = parentFolderId ?: FileUploadWorker.INVALID_ID
                )
            }
            FileUploadType.COURSE -> {
                FileUploadInputEntity(
                    workerId = workerId.toString(),
                    filePaths = uris.map { it.toString() },
                    action = FileUploadWorker.ACTION_COURSE_FILE,
                    courseId = canvasContext.id,
                    parentFolderId = parentFolderId ?: FileUploadWorker.INVALID_ID
                )
            }
            FileUploadType.GROUP -> {
                FileUploadInputEntity(
                    workerId = workerId.toString(),
                    filePaths = uris.map { it.toString() },
                    action = FileUploadWorker.ACTION_GROUP_FILE,
                    courseId = canvasContext.id,
                    parentFolderId = parentFolderId ?: FileUploadWorker.INVALID_ID
                )
            }
            FileUploadType.MESSAGE -> {
                FileUploadInputEntity(
                    workerId = workerId.toString(),
                    filePaths = uris.map { it.toString() },
                    action = FileUploadWorker.ACTION_MESSAGE_ATTACHMENTS
                )
            }
            FileUploadType.DISCUSSION -> {
                FileUploadInputEntity(
                    workerId = workerId.toString(),
                    filePaths = uris.map { it.toString() },
                    action = FileUploadWorker.ACTION_DISCUSSION_ATTACHMENT
                )
            }
            FileUploadType.QUIZ -> {
                FileUploadInputEntity(
                    workerId = workerId.toString(),
                    filePaths = uris.map { it.toString() },
                    action = FileUploadWorker.ACTION_QUIZ_FILE,
                    quizId = quizId,
                    quizQuestionId = quizQuestionId,
                    position = position,
                    courseId = canvasContext.id,
                    parentFolderId = parentFolderId
                )
            }
            FileUploadType.SUBMISSION_COMMENT -> {
                FileUploadInputEntity(
                    workerId = workerId.toString(),
                    filePaths = uris.map { it.toString() },
                    action = FileUploadWorker.ACTION_SUBMISSION_COMMENT,
                    courseId = canvasContext.id,
                    assignmentId = assignment!!.id
                )
            }
            FileUploadType.TEACHER_SUBMISSION_COMMENT -> {
                FileUploadInputEntity(
                    workerId = workerId.toString(),
                    filePaths = uris.map { it.toString() },
                    action = FileUploadWorker.ACTION_TEACHER_SUBMISSION_COMMENT,
                    courseId = assignment?.courseId.orDefault(),
                    assignmentId = assignment?.id.orDefault(),
                    userId = userId.orDefault(),
                    attemptId = attemptId
                )
            }
            else -> {
                FileUploadInputEntity(
                    workerId = workerId.toString(),
                    filePaths = uris.map { it.toString() },
                    action = FileUploadWorker.ACTION_ASSIGNMENT_SUBMISSION,
                    courseId = assignment?.courseId.orDefault(),
                    assignmentId = assignment?.id.orDefault(),
                    submissionId = null
                )
            }
        }
    }

    fun onCancelClicked() {
        dialogCallback?.invoke(FileUploadDialogFragment.EVENT_DIALOG_CANCELED)
        _events.value = Event(FileUploadAction.AttachmentSelectedAction(FileUploadDialogFragment.EVENT_DIALOG_CANCELED, null))
    }
}