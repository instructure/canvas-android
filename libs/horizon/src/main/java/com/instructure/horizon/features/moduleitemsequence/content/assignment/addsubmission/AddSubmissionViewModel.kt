/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.horizon.features.moduleitemsequence.content.assignment.addsubmission

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.postmodels.FileSubmitObject
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.horizon.R
import com.instructure.horizon.features.moduleitemsequence.content.assignment.submission.HorizonSubmissionHelper
import com.instructure.pandautils.features.file.upload.FileUploadUtilsHelper
import com.instructure.pandautils.room.studentdb.entities.CreateSubmissionEntity
import com.instructure.pandautils.room.studentdb.entities.daos.CreateFileSubmissionDao
import com.instructure.pandautils.room.studentdb.entities.daos.CreateSubmissionDao
import com.instructure.pandautils.utils.format
import com.instructure.pandautils.utils.orDefault
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class AddSubmissionViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val submissionHelper: HorizonSubmissionHelper,
    private val apiPrefs: ApiPrefs,
    private val createSubmissionDao: CreateSubmissionDao,
    private val createFileSubmissionDao: CreateFileSubmissionDao,
    private val fileUploadUtilsHelper: FileUploadUtilsHelper
) : ViewModel() {

    private var files: MutableList<FileSubmitObject> = mutableListOf()

    private var assignmentName: String = ""
    private var assignmentId: Long = -1L
    private var courseId: Long = -1L
    private var assignment: Assignment? = null

    private val _uiState =
        MutableStateFlow(
            AddSubmissionUiState(
                onSubmissionTypeSelected = ::submissionTypeSelected,
                onSubmissionButtonClicked = ::showSubmissionConfirmation,
                onDismissSubmissionConfirmation = ::submissionConfirmationDismissed,
                onSubmitAssignment = ::sendSubmission,
                onAssignmentUpdated = ::updateAssignment
            )
        )

    val uiState = _uiState.asStateFlow()

    private var onSubmissionSuccess: (suspend () -> Unit)? = null
    fun setOnSubmissionSuccessListener(listener: (suspend () -> Unit)?) {
        onSubmissionSuccess = listener
    }

    private fun updateAssignment(assignment: Assignment) {
        viewModelScope.tryLaunch {
            assignmentName = assignment.name.orEmpty()
            assignmentId = assignment.id
            courseId = assignment.courseId
            this@AddSubmissionViewModel.assignment = assignment

            val submissionTypes = assignment.getSubmissionTypes().mapNotNull {
                if (it != Assignment.SubmissionType.ONLINE_UPLOAD && it != Assignment.SubmissionType.ONLINE_TEXT_ENTRY) return@mapNotNull null

                val draft = withContext(Dispatchers.IO) {
                    createSubmissionDao.findDraftSubmissionByAssignmentIdAndType(
                        assignmentId,
                        apiPrefs.user?.id.orDefault(),
                        it.apiString
                    )
                }

                val draftDate = getDraftDateString(draft?.lastActivityDate)
                val draftUiState = DraftUiState(
                    draftDate,
                    onDeleteDraftClicked = { deleteDraftClicked(it) },
                    onDismissDeleteDraftConfirmation = { deleteDraftDismissed(it) },
                    onDraftDeleted = { deleteDraftSubmission(it) },
                )

                if (it == Assignment.SubmissionType.ONLINE_TEXT_ENTRY) {
                    val text = draft?.submissionEntry.orEmpty()
                    AddSubmissionTypeUiState.Text(text, ::onTextSubmissionChanged, draftUiState, text.isNotEmpty())
                } else {
                    val files = createFileSubmissionDao.findFilesForSubmissionId(draft?.id ?: -1L)
                    this@AddSubmissionViewModel.files = files.map { file ->
                        FileSubmitObject(
                            name = file.name.orEmpty(),
                            fullPath = file.fullPath.orEmpty(),
                            contentType = file.contentType.orEmpty(),
                            size = file.size.orDefault()
                        )
                    }.toMutableList()

                    val fileUiStates = files.map { file ->
                        AddSubmissionFileUiState(
                            name = file.name.orEmpty(),
                            path = file.fullPath,
                            onDeleteClicked = { deleteFile(file.fullPath) }
                        )
                    }
                    AddSubmissionTypeUiState.File(
                        allowedTypes = assignment.allowedExtensions,
                        cameraAllowed = assignment.allowedExtensions.isEmpty() || assignment.allowedExtensions.contains("jpg"),
                        galleryPickerAllowed = galleryPickerAllowed(assignment.allowedExtensions),
                        onFileAdded = ::onFileAdded,
                        files = fileUiStates,
                        draftUiState = draftUiState,
                        submitEnabled = fileUiStates.isNotEmpty()
                    )
                }
            }

            _uiState.update {
                it.copy(
                    submissionTypes = submissionTypes
                )
            }
        } catch {

        }
    }

    private fun deleteDraftClicked(submissionType: Assignment.SubmissionType) {
        updateShowDeleteDraftConfirmation(submissionType, true)
    }

    private fun deleteDraftDismissed(submissionType: Assignment.SubmissionType) {
        updateShowDeleteDraftConfirmation(submissionType, false)
    }

    private fun updateShowDeleteDraftConfirmation(submissionType: Assignment.SubmissionType, show: Boolean) {
        _uiState.update {
            it.copy(
                submissionTypes = it.submissionTypes.map { submissionTypeUiState ->
                    if (submissionTypeUiState.submissionType == submissionType) {
                        submissionTypeUiState.copyWith(submissionTypeUiState.draftUiState.copy(showDeleteDraftConfirmation = show))
                    } else {
                        submissionTypeUiState
                    }
                }
            )
        }
    }

    private fun deleteDraftSubmission(submissionType: Assignment.SubmissionType) {
        viewModelScope.launch {
            createSubmissionDao.deleteDraftByAssignmentIdAndType(assignmentId, apiPrefs.user?.id.orDefault(), submissionType.apiString)
            updateShowDeleteDraftConfirmation(submissionType, false)
            if (submissionType == Assignment.SubmissionType.ONLINE_TEXT_ENTRY) {
                onTextSubmissionChanged("")
            } else if (submissionType == Assignment.SubmissionType.ONLINE_UPLOAD) {
                deleteAllFiles()
            }
            _uiState.update {
                it.copy(
                    snackbarMessage = context.getString(R.string.assignmentDetails_draftDeleted),
                    onSnackbarDismiss = {
                        _uiState.update { uiState ->
                            uiState.copy(snackbarMessage = null)
                        }
                    })
            }
            updateDraftTextForSubmissionType(submissionType)
        }
    }

    private fun galleryPickerAllowed(allowedExtensions: List<String>): Boolean {
        val mediaExtensions = setOf(
            "jpg", "jpeg", "png", "gif", "bmp", "webp", "heic", "heif", "tif", "tiff", "raw", "svg", "ico", "mp4", "mkv", "avi",
            "mov", "webm", "flv", "3gp", "3g2", "wmv", "mpeg", "mpg", "m4v", "ts", "mts"
        )
        return allowedExtensions.isEmpty() || allowedExtensions.any { mediaExtensions.contains(it) }
    }

    private fun submissionTypeSelected(index: Int) {
        _uiState.update {
            it.copy(
                selectedSubmissionTypeIndex = index,
            )
        }
    }

    private fun showSubmissionConfirmation() {
        _uiState.update {
            it.copy(
                showSubmissionConfirmation = true
            )
        }
    }

    private fun submissionConfirmationDismissed() {
        _uiState.update {
            it.copy(
                showSubmissionConfirmation = false
            )
        }
    }

    private fun sendSubmission() {
        _uiState.update {
            it.copy(
                showSubmissionConfirmation = false,
                errorMessage = null
            )
        }
        val selectedSubmissionType =
            uiState.value.submissionTypes[uiState.value.selectedSubmissionTypeIndex]
        if (selectedSubmissionType is AddSubmissionTypeUiState.Text) {
            submissionHelper.startTextSubmission(
                canvasContext = CanvasContext.emptyCourseContext(id = courseId),
                assignmentId = assignmentId,
                text = selectedSubmissionType.text,
                assignmentName = assignmentName,
                deleteBySubmissionTypeFilter = Assignment.SubmissionType.ONLINE_TEXT_ENTRY
            )
        } else if (selectedSubmissionType is AddSubmissionTypeUiState.File) {
            submissionHelper.startFileSubmission(
                canvasContext = CanvasContext.emptyCourseContext(id = courseId),
                assignmentId = assignmentId,
                assignmentName = assignmentName,
                files = ArrayList(files),
                deleteBySubmissionTypeFilter = Assignment.SubmissionType.ONLINE_UPLOAD
            )
        }

        viewModelScope.launch {
            createSubmissionDao.findSubmissionByAssignmentIdAndTypeFlow(
                assignmentId,
                apiPrefs.user?.id.orDefault(),
                selectedSubmissionType.submissionType.apiString
            ).collect { entity ->
                if (entity != null) {
                    updateSubmissionProgress(entity)
                }
            }
        }
    }

    private suspend fun updateSubmissionProgress(entity: CreateSubmissionEntity) {
        val progress = entity.progress ?: 0f
        val showProgress = progress > 0f && progress < 100.0
        if (entity.errorFlag) {
            _uiState.update {
                it.copy(
                    submissionInProgress = false,
                    errorMessage = context.getString(R.string.assignmentDetails_submissionError),
                    submissionTypes = getUpdatedSubmissionTypesWithUploadFileEnabled(it.submissionTypes, true)
                )
            }
        } else if (progress == 100.0f) {
            onSubmissionSuccess?.invoke()
            onTextSubmissionChanged("")
            _uiState.update {
                it.copy(
                    submissionInProgress = showProgress,
                    submissionTypes = getUpdatedSubmissionTypesWithUploadFileEnabled(it.submissionTypes, !showProgress))
            }
        } else if (progress > 0f) {
            _uiState.update {
                it.copy(
                    submissionInProgress = showProgress,
                    submissionTypes = getUpdatedSubmissionTypesWithUploadFileEnabled(it.submissionTypes, !showProgress)
                )
            }
        }
    }

    private fun getUpdatedSubmissionTypesWithUploadFileEnabled(
        submissionTypes: List<AddSubmissionTypeUiState>,
        enabled: Boolean
    ): List<AddSubmissionTypeUiState> {
        return submissionTypes.map { submissionTypeUiState ->
            if (submissionTypeUiState is AddSubmissionTypeUiState.File) {
                submissionTypeUiState.copy(uploadFileEnabled = enabled)
            } else {
                submissionTypeUiState
            }
        }
    }

    private fun onTextSubmissionChanged(text: String) {
        val textSubmission =
            uiState.value.submissionTypes[uiState.value.selectedSubmissionTypeIndex]
        if (textSubmission is AddSubmissionTypeUiState.Text) {
            _uiState.update {
                it.copy(
                    submissionTypes = it.submissionTypes.mapIndexed { index, submissionType ->
                        if (index == uiState.value.selectedSubmissionTypeIndex) {
                            textSubmission.copy(text = text)
                        } else {
                            submissionType
                        }
                    }
                )
            }
            viewModelScope.launch {
                // We need to replace the line breaks from the RCE because if we delete any text it will still leave a <br> tag in the RCE
                if (text.replace("<br>", "").isNotBlank()) {
                    submissionHelper.saveDraft(CanvasContext.emptyCourseContext(id = courseId), assignmentId, assignmentName, text)
                    updateDraftTextForSubmissionType(Assignment.SubmissionType.ONLINE_TEXT_ENTRY, Date())
                    updateSubmissionEnabled(Assignment.SubmissionType.ONLINE_TEXT_ENTRY, true)
                } else {
                    createSubmissionDao.deleteDraftByAssignmentIdAndType(
                        assignmentId,
                        apiPrefs.user?.id.orDefault(),
                        Assignment.SubmissionType.ONLINE_TEXT_ENTRY.apiString
                    )
                    updateDraftTextForSubmissionType(Assignment.SubmissionType.ONLINE_TEXT_ENTRY)
                    updateSubmissionEnabled(Assignment.SubmissionType.ONLINE_TEXT_ENTRY, false)
                }
            }
        }
    }

    private fun updateDraftTextForSubmissionType(submissionType: Assignment.SubmissionType, date: Date? = null) {
        getDraftDateString(date)

        _uiState.update {
            it.copy(
                submissionTypes = it.submissionTypes.map { submissionTypeUiState ->
                    if (submissionTypeUiState.submissionType == submissionType) {
                        submissionTypeUiState.copyWith(submissionTypeUiState.draftUiState.copy(draftDateString = getDraftDateString(date)))
                    } else {
                        submissionTypeUiState
                    }
                }
            )
        }
    }

    private fun getDraftDateString(date: Date? = null): String {
        return if (date == null) {
            ""
        } else {
            context.getString(R.string.assignmentDetails_draftSaved, date.format("dd/MM, h:mm a"))
        }
    }

    private fun updateSubmissionEnabled(submissionType: Assignment.SubmissionType, enabled: Boolean) {
        _uiState.update {
            it.copy(
                submissionTypes = it.submissionTypes.map { submissionTypeUiState ->
                    if (submissionTypeUiState.submissionType == submissionType) {
                        submissionTypeUiState.copyWith(submitEnabled = enabled)
                    } else {
                        submissionTypeUiState
                    }
                }
            )
        }
    }

    private fun onFileAdded(uri: Uri) {
        val extension = fileUploadUtilsHelper.getFileExtension(uri)
        val fso = getUriContents(uri)
        val allowedExtensions = assignment?.allowedExtensions.orEmpty()
        if (allowedExtensions.isEmpty() || allowedExtensions.contains(extension)) {
            _uiState.update {
                it.copy(
                    submissionTypes = it.submissionTypes.mapIndexed { index, submissionType ->
                        if (index == it.selectedSubmissionTypeIndex && submissionType is AddSubmissionTypeUiState.File) {
                            submissionType.copy(
                                files = submissionType.files + AddSubmissionFileUiState(
                                    name = fileUploadUtilsHelper.getFileNameWithDefault(uri),
                                    path = fso?.fullPath,
                                    onDeleteClicked = { deleteFile(fso?.fullPath) })
                            )
                        } else {
                            submissionType
                        }
                    }
                )
            }

            if (fso != null) files.add(fso)

            val addSubmissionUiState = _uiState.value
            val hasFiles =
                addSubmissionUiState.submissionTypes[addSubmissionUiState.selectedSubmissionTypeIndex] is AddSubmissionTypeUiState.File &&
                        (addSubmissionUiState.submissionTypes[addSubmissionUiState.selectedSubmissionTypeIndex] as AddSubmissionTypeUiState.File).files.isNotEmpty()
            updateSubmissionEnabled(Assignment.SubmissionType.ONLINE_UPLOAD, hasFiles)

            viewModelScope.launch {
                submissionHelper.saveDraftWithFiles(
                    CanvasContext.emptyCourseContext(id = courseId),
                    assignmentId,
                    assignmentName,
                    files
                )
            }
            updateDraftTextForSubmissionType(Assignment.SubmissionType.ONLINE_UPLOAD, Date())
        } else {
            _uiState.update {
                it.copy(
                    snackbarMessage = context.getString(R.string.assignmentDetails_fileTypeNotSupported),
                    onSnackbarDismiss = {
                        _uiState.update { uiState ->
                            uiState.copy(snackbarMessage = null)
                        }
                    })
            }
        }
    }

    private fun deleteFile(path: String?) {
        _uiState.update { uiState ->
            uiState.copy(
                submissionTypes = uiState.submissionTypes.mapIndexed { index, submissionType ->
                    if (index == uiState.selectedSubmissionTypeIndex && submissionType is AddSubmissionTypeUiState.File) {
                        submissionType.copy(
                            files = submissionType.files.filterNot { it.path == path }
                        )
                    } else {
                        submissionType
                    }
                }
            )
        }

        val addSubmissionUiState = _uiState.value
        val hasFiles =
            addSubmissionUiState.submissionTypes[addSubmissionUiState.selectedSubmissionTypeIndex] is AddSubmissionTypeUiState.File &&
                    (addSubmissionUiState.submissionTypes[addSubmissionUiState.selectedSubmissionTypeIndex] as AddSubmissionTypeUiState.File).files.isNotEmpty()
        updateSubmissionEnabled(Assignment.SubmissionType.ONLINE_UPLOAD, hasFiles)

        files.removeIf { it.fullPath == path }

        if (hasFiles) {
            viewModelScope.launch {
                submissionHelper.saveDraftWithFiles(
                    CanvasContext.emptyCourseContext(id = courseId),
                    assignmentId,
                    assignmentName,
                    files
                )
            }
            updateDraftTextForSubmissionType(Assignment.SubmissionType.ONLINE_UPLOAD, Date())
        } else {
            viewModelScope.launch {
                createSubmissionDao.deleteDraftByAssignmentIdAndType(
                    assignmentId,
                    apiPrefs.user?.id.orDefault(),
                    Assignment.SubmissionType.ONLINE_UPLOAD.apiString
                )
            }
            updateDraftTextForSubmissionType(Assignment.SubmissionType.ONLINE_UPLOAD)
        }
    }

    private fun deleteAllFiles() {
        files.clear()
        _uiState.update { uiState ->
            uiState.copy(
                submissionTypes = uiState.submissionTypes.mapIndexed { index, submissionType ->
                    if (index == uiState.selectedSubmissionTypeIndex && submissionType is AddSubmissionTypeUiState.File) {
                        submissionType.copy(
                            files = emptyList()
                        )
                    } else {
                        submissionType
                    }
                }
            )
        }

        updateSubmissionEnabled(Assignment.SubmissionType.ONLINE_UPLOAD, false)
    }

    private fun getUriContents(fileUri: Uri): FileSubmitObject? {
        val mimeType = fileUploadUtilsHelper.getFileMimeType(fileUri)
        val fileName = fileUploadUtilsHelper.getFileNameWithDefault(fileUri)

        return fileUploadUtilsHelper.getFileSubmitObjectFromInputStream(fileUri, fileName, mimeType)
    }
}