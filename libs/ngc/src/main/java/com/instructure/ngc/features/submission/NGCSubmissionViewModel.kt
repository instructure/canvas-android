/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
package com.instructure.ngc.features.submission

import android.content.Context
import androidx.core.util.PatternsCompat
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.postmodels.FileSubmitObject
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.ngc.R
import com.instructure.ngc.features.submission.domain.DeleteDraftUseCase
import com.instructure.ngc.features.submission.domain.GetDraftFilesUseCase
import com.instructure.ngc.features.submission.domain.GetFileDraftUseCase
import com.instructure.ngc.features.submission.domain.GetSubmissionDataUseCase
import com.instructure.ngc.features.submission.domain.GetSubmissionFileStatesUseCase
import com.instructure.ngc.features.submission.domain.GetTextDraftUseCase
import com.instructure.ngc.features.submission.domain.GetUrlDraftUseCase
import com.instructure.ngc.features.submission.domain.ObserveSubmissionProgressUseCase
import com.instructure.ngc.features.submission.domain.ResumeInFlightSubmissionUseCase
import com.instructure.ngc.features.submission.domain.RetryFileSubmissionUseCase
import com.instructure.ngc.features.submission.domain.SaveFileDraftUseCase
import com.instructure.ngc.features.submission.domain.SaveTextDraftUseCase
import com.instructure.ngc.features.submission.domain.SaveUrlDraftUseCase
import com.instructure.ngc.features.submission.domain.SubmissionFileData
import com.instructure.ngc.features.submission.domain.SubmitFilesUseCase
import com.instructure.ngc.features.submission.domain.SubmitTextUseCase
import com.instructure.ngc.features.submission.domain.SubmitUrlUseCase
import com.instructure.ngc.features.submission.supportingpane.SubmissionSupportingPaneUiState
import com.instructure.pandautils.utils.FileUploadUtils
import com.instructure.pandautils.utils.isHtmlBlank
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class NGCSubmissionViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context,
    private val getSubmissionDataUseCase: GetSubmissionDataUseCase,
    private val getTextDraftUseCase: GetTextDraftUseCase,
    private val getFileDraftUseCase: GetFileDraftUseCase,
    private val saveTextDraftUseCase: SaveTextDraftUseCase,
    private val submitTextUseCase: SubmitTextUseCase,
    private val observeSubmissionProgressUseCase: ObserveSubmissionProgressUseCase,
    private val saveFileDraftUseCase: SaveFileDraftUseCase,
    private val submitFilesUseCase: SubmitFilesUseCase,
    private val getSubmissionFileStatesUseCase: GetSubmissionFileStatesUseCase,
    private val getDraftFilesUseCase: GetDraftFilesUseCase,
    private val retryFileSubmissionUseCase: RetryFileSubmissionUseCase,
    private val resumeInFlightSubmissionUseCase: ResumeInFlightSubmissionUseCase,
    private val getUrlDraftUseCase: GetUrlDraftUseCase,
    private val saveUrlDraftUseCase: SaveUrlDraftUseCase,
    private val deleteDraftUseCase: DeleteDraftUseCase,
    private val submitUrlUseCase: SubmitUrlUseCase,
    private val dateFormatter: SubmissionDateFormatter,
) : ViewModel() {

    val courseId: Long = savedStateHandle.get<Long>(ARG_COURSE_ID) ?: 0L
    val assignmentId: Long = savedStateHandle.get<Long>(ARG_ASSIGNMENT_ID) ?: 0L
    private val requestedTypeApiString: String? = savedStateHandle.get<String>(ARG_SUBMISSION_TYPE)

    private var assignmentName: String = ""
    private var lastAttempt: Long? = null

    // Each submission type keeps its own draft timestamp so switching types shows the right "Saved" time.
    private var textSavedAtText: String = ""
    private var fileSavedAtText: String = ""
    private var urlSavedAtText: String = ""

    private val _uiState = MutableStateFlow(NGCSubmissionUiState())
    val uiState: StateFlow<NGCSubmissionUiState> = _uiState.asStateFlow()

    private val pendingTextFlow = MutableStateFlow<String?>(null)
    private val pendingUrlFlow = MutableStateFlow<String?>(null)

    init {
        loadData()
        collectDebouncedTextChanges()
        collectDebouncedUrlChanges()
    }

    private fun loadData() {
        _uiState.update { it.copy(loading = true, loadError = null) }
        viewModelScope.launch {
            try {
                // Preselect the requested type (from the submit chooser) when it's actually available;
                // fall back to text-preferred default after fetching assignment data.
                // We need the available types first, so fetch with a provisional type and then
                // determine the real default.
                val provisionalType = requestedTypeApiString
                    ?.let { api ->
                        Assignment.SubmissionType.values().firstOrNull { it.apiString == api }
                    }
                    ?: Assignment.SubmissionType.ONLINE_TEXT_ENTRY

                val data = getSubmissionDataUseCase(
                    GetSubmissionDataUseCase.Params(
                        courseId = courseId,
                        assignmentId = assignmentId,
                        submissionType = provisionalType,
                    )
                )
                assignmentName = data.assignmentName
                lastAttempt = data.lastAttempt

                val dueDateText = data.dueDate?.let { date ->
                    val formatted = dateFormatter.formatDateTime(context, date)
                    context.getString(R.string.ngc_submission_dueDate, formatted)
                } ?: ""

                val requestedType = requestedTypeApiString
                    ?.let { api -> data.availableTypes.firstOrNull { it.apiString == api } }
                val defaultType = requestedType ?: when {
                    data.availableTypes.contains(Assignment.SubmissionType.ONLINE_TEXT_ENTRY) ->
                        Assignment.SubmissionType.ONLINE_TEXT_ENTRY
                    data.availableTypes.isNotEmpty() -> data.availableTypes.first()
                    else -> Assignment.SubmissionType.ONLINE_TEXT_ENTRY
                }

                // If the provisional type didn't match the default, reload with the correct type
                val finalData = if (provisionalType != defaultType) {
                    getSubmissionDataUseCase(
                        GetSubmissionDataUseCase.Params(
                            courseId = courseId,
                            assignmentId = assignmentId,
                            submissionType = defaultType,
                        )
                    )
                } else {
                    data
                }

                val draftFiles = finalData.draftFiles.map { it.toSubmissionFileUiState() }

                val textDraftHtml = finalData.draftHtml ?: ""
                textSavedAtText = if (defaultType != Assignment.SubmissionType.ONLINE_UPLOAD) {
                    finalData.draftSavedAt?.let { formatSavedAt(it) } ?: ""
                } else {
                    ""
                }
                fileSavedAtText = if (defaultType == Assignment.SubmissionType.ONLINE_UPLOAD) {
                    finalData.fileDraftSavedAt?.let { formatSavedAt(it) } ?: ""
                } else {
                    ""
                }
                val urlDraft = finalData.draftUrl ?: ""
                urlSavedAtText = if (defaultType == Assignment.SubmissionType.ONLINE_URL) {
                    finalData.urlDraftSavedAt?.let { formatSavedAt(it) } ?: ""
                } else {
                    ""
                }

                val initialSavedAtText = when (defaultType) {
                    Assignment.SubmissionType.ONLINE_UPLOAD -> fileSavedAtText
                    Assignment.SubmissionType.ONLINE_URL -> urlSavedAtText
                    else -> textSavedAtText
                }

                _uiState.update { state ->
                    state.copy(
                        loading = false,
                        loadError = null,
                        courseName = finalData.courseName,
                        assignmentName = finalData.assignmentName,
                        dueDateText = dueDateText,
                        availableTypes = finalData.availableTypes,
                        selectedType = defaultType,
                        onSubmissionTypeSelected = ::onSubmissionTypeSelected,
                        textState = state.textState.copy(
                            draftHtml = textDraftHtml,
                            contentResetKey = state.textState.contentResetKey + 1,
                        ),
                        fileState = state.fileState.copy(
                            files = draftFiles,
                            allowedExtensions = finalData.allowedExtensions,
                            onFilesPicked = ::onFilesPicked,
                            onFileRemoved = ::onFileRemoved,
                            onRetryFile = ::onRetryFile,
                        ),
                        urlState = state.urlState.copy(
                            draftUrl = urlDraft,
                            previewUrl = previewUrlFor(urlDraft),
                            urlError = urlErrorFor(urlDraft),
                            contentResetKey = state.urlState.contentResetKey + 1,
                        ),
                        savedAtText = initialSavedAtText,
                        submitEnabled = computeSubmitEnabled(defaultType, textDraftHtml, draftFiles, urlDraft),
                        onRetry = {},
                        supportingPane = SubmissionSupportingPaneUiState(
                            loading = false,
                            instructionsHtml = finalData.instructionsHtml.orEmpty(),
                        ),
                    )
                }

                // Re-attach to any in-flight upload that survived process death
                val inFlight = resumeInFlightSubmissionUseCase(
                    ResumeInFlightSubmissionUseCase.Params(assignmentId)
                )
                if (inFlight != null) {
                    _uiState.update { state ->
                        state.copy(
                            submitting = true,
                            fileState = state.fileState.copy(
                                files = state.fileState.files.map { it.copy(state = SubmissionFileState.InProgress()) }
                            ),
                        )
                    }
                    viewModelScope.launch {
                        try {
                            observeSubmissionTerminal()
                        } catch (e: Exception) {
                            setSubmissionError()
                        }
                    }
                }
            } catch (e: Exception) {
                val errorMessage = context.getString(R.string.ngc_submission_loadError)
                _uiState.update {
                    it.copy(
                        loading = false,
                        loadError = errorMessage,
                        onRetry = { loadData() },
                        supportingPane = it.supportingPane.copy(loading = false),
                    )
                }
            }
        }
    }

    private fun collectDebouncedTextChanges() {
        viewModelScope.launch {
            pendingTextFlow
                .filterNotNull()
                .debounce(400)
                .distinctUntilChanged()
                .collectLatest { html ->
                    if (!isHtmlBlank(html)) {
                        saveTextDraftUseCase(
                            SaveTextDraftUseCase.Params(
                                canvasContext = CanvasContext.emptyCourseContext(id = courseId),
                                assignmentId = assignmentId,
                                assignmentName = assignmentName,
                                html = html,
                            )
                        )
                        textSavedAtText = formatSavedAt(Date())
                        _uiState.update { it.copy(savedAtText = textSavedAtText) }
                    } else {
                        deleteDraftUseCase(DeleteDraftUseCase.Params(assignmentId, Assignment.SubmissionType.ONLINE_TEXT_ENTRY))
                        textSavedAtText = ""
                        _uiState.update { it.copy(savedAtText = "") }
                    }
                }
        }
    }

    fun onTextChanged(html: String) {
        _uiState.update { state ->
            state.copy(
                textState = state.textState.copy(draftHtml = html),
                submitEnabled = computeSubmitEnabled(state.selectedType, html, state.fileState.files, state.urlState.draftUrl),
            )
        }
        pendingTextFlow.value = html
    }

    private fun collectDebouncedUrlChanges() {
        viewModelScope.launch {
            pendingUrlFlow
                .filterNotNull()
                .debounce(400)
                .distinctUntilChanged()
                .collectLatest { url ->
                    // Skip a stale debounced write/preview if a submit or delete already reset the
                    // field within the 400ms window — otherwise it would re-save the draft and snap
                    // the preview back to the previously-typed URL.
                    if (_uiState.value.submitting || url != _uiState.value.urlState.draftUrl) return@collectLatest
                    // Recompute the preview here (not on every keystroke) so the WebView only
                    // (re)loads once typing settles, avoiding reload thrash while the user types.
                    val preview = previewUrlFor(url)
                    if (url.isNotBlank()) {
                        saveUrlDraftUseCase(
                            SaveUrlDraftUseCase.Params(
                                canvasContext = CanvasContext.emptyCourseContext(id = courseId),
                                assignmentId = assignmentId,
                                assignmentName = assignmentName,
                                url = url,
                            )
                        )
                        urlSavedAtText = formatSavedAt(Date())
                        _uiState.update {
                            it.copy(
                                savedAtText = urlSavedAtText,
                                urlState = it.urlState.copy(previewUrl = preview),
                            )
                        }
                    } else {
                        deleteDraftUseCase(DeleteDraftUseCase.Params(assignmentId, Assignment.SubmissionType.ONLINE_URL))
                        urlSavedAtText = ""
                        _uiState.update {
                            it.copy(
                                savedAtText = "",
                                urlState = it.urlState.copy(previewUrl = ""),
                            )
                        }
                    }
                }
        }
    }

    fun onUrlChanged(url: String) {
        _uiState.update { state ->
            state.copy(
                urlState = state.urlState.copy(draftUrl = url, urlError = urlErrorFor(url)),
                submitEnabled = computeSubmitEnabled(state.selectedType, state.textState.draftHtml, state.fileState.files, url),
            )
        }
        pendingUrlFlow.value = url
    }

    fun onDeleteDraft() {
        if (_uiState.value.submitting) return
        viewModelScope.launch {
            when (_uiState.value.selectedType) {
                Assignment.SubmissionType.ONLINE_UPLOAD -> {
                    val filesToDelete = _uiState.value.fileState.files.map { it.fullPath }
                    deleteDraftUseCase(DeleteDraftUseCase.Params(assignmentId, Assignment.SubmissionType.ONLINE_UPLOAD))
                    filesToDelete.forEach { path -> path?.let { FileUploadUtils.deleteTempFile(it) } }
                    fileSavedAtText = ""
                    _uiState.update { state ->
                        state.copy(
                            fileState = state.fileState.copy(files = emptyList()),
                            savedAtText = "",
                            submitEnabled = false,
                        )
                    }
                }
                Assignment.SubmissionType.ONLINE_URL -> {
                    deleteDraftUseCase(DeleteDraftUseCase.Params(assignmentId, Assignment.SubmissionType.ONLINE_URL))
                    urlSavedAtText = ""
                    _uiState.update { state ->
                        state.copy(
                            urlState = state.urlState.copy(
                                draftUrl = "",
                                previewUrl = "",
                                urlError = null,
                                contentResetKey = state.urlState.contentResetKey + 1,
                            ),
                            savedAtText = "",
                            submitEnabled = false,
                        )
                    }
                }
                else -> {
                    deleteDraftUseCase(DeleteDraftUseCase.Params(assignmentId, Assignment.SubmissionType.ONLINE_TEXT_ENTRY))
                    textSavedAtText = ""
                    _uiState.update { state ->
                        state.copy(
                            textState = state.textState.copy(
                                draftHtml = "",
                                contentResetKey = state.textState.contentResetKey + 1,
                            ),
                            savedAtText = "",
                            submitEnabled = false,
                        )
                    }
                }
            }
        }
    }

    fun onSubmit() {
        if (_uiState.value.submitting) return
        _uiState.update { it.copy(submitting = true, errorMessage = null) }
        when (_uiState.value.selectedType) {
            Assignment.SubmissionType.ONLINE_UPLOAD -> submitFiles()
            Assignment.SubmissionType.ONLINE_URL -> submitUrl()
            else -> submitText()
        }
    }

    private fun submitText() {
        val currentHtml = _uiState.value.textState.draftHtml
        viewModelScope.launch {
            try {
                submitTextUseCase(
                    SubmitTextUseCase.Params(
                        canvasContext = CanvasContext.emptyCourseContext(id = courseId),
                        assignmentId = assignmentId,
                        assignmentName = assignmentName,
                        html = currentHtml,
                        attempt = lastAttempt,
                    )
                )

                var seenNonNull = false
                var terminalIsSuccess = false
                observeSubmissionProgressUseCase(
                    ObserveSubmissionProgressUseCase.Params(assignmentId)
                )
                    .first { entity ->
                        when {
                            entity == null && seenNonNull -> { terminalIsSuccess = true; true }
                            entity != null -> {
                                seenNonNull = true
                                when {
                                    entity.errorFlag -> true
                                    (entity.progress ?: 0f) == 100.0f -> { terminalIsSuccess = true; true }
                                    else -> false
                                }
                            }
                            else -> false
                        }
                    }

                if (!terminalIsSuccess) {
                    setSubmissionError()
                } else {
                    textSavedAtText = ""
                    _uiState.update { state ->
                        state.copy(
                            submitting = false,
                            textState = state.textState.copy(
                                draftHtml = "",
                                contentResetKey = state.textState.contentResetKey + 1,
                            ),
                            savedAtText = "",
                            submitEnabled = false,
                            submissionSuccess = true,
                            onSubmissionSuccessHandled = {
                                _uiState.update { s -> s.copy(submissionSuccess = false) }
                            },
                        )
                    }
                }
            } catch (e: Exception) {
                setSubmissionError()
            }
        }
    }

    private fun submitUrl() {
        val currentUrl = normalizeUrlForSubmit(_uiState.value.urlState.draftUrl)
        viewModelScope.launch {
            try {
                submitUrlUseCase(
                    SubmitUrlUseCase.Params(
                        canvasContext = CanvasContext.emptyCourseContext(id = courseId),
                        assignmentId = assignmentId,
                        assignmentName = assignmentName,
                        url = currentUrl,
                        attempt = lastAttempt,
                    )
                )

                var seenNonNull = false
                var terminalIsSuccess = false
                observeSubmissionProgressUseCase(
                    ObserveSubmissionProgressUseCase.Params(
                        assignmentId = assignmentId,
                        submissionType = Assignment.SubmissionType.ONLINE_URL,
                    )
                )
                    .first { entity ->
                        when {
                            entity == null && seenNonNull -> { terminalIsSuccess = true; true }
                            entity != null -> {
                                seenNonNull = true
                                when {
                                    entity.errorFlag -> true
                                    (entity.progress ?: 0f) == 100.0f -> { terminalIsSuccess = true; true }
                                    else -> false
                                }
                            }
                            else -> false
                        }
                    }

                if (!terminalIsSuccess) {
                    setSubmissionError()
                } else {
                    urlSavedAtText = ""
                    _uiState.update { state ->
                        state.copy(
                            submitting = false,
                            urlState = state.urlState.copy(
                                draftUrl = "",
                                previewUrl = "",
                                urlError = null,
                                contentResetKey = state.urlState.contentResetKey + 1,
                            ),
                            savedAtText = "",
                            submitEnabled = false,
                            submissionSuccess = true,
                            onSubmissionSuccessHandled = {
                                _uiState.update { s -> s.copy(submissionSuccess = false) }
                            },
                        )
                    }
                }
            } catch (e: Exception) {
                setSubmissionError()
            }
        }
    }

    private fun submitFiles() {
        val currentFiles = _uiState.value.fileState.files.map { it.toFileSubmitObject() }
        if (currentFiles.isEmpty()) {
            _uiState.update { it.copy(submitting = false) }
            return
        }

        // Start with indeterminate rows. Once the worker begins emitting real progress values
        // the flow's onEach block below will switch them to determinate.
        _uiState.update { state ->
            state.copy(
                fileState = state.fileState.copy(
                    files = state.fileState.files.map { it.copy(state = SubmissionFileState.InProgress()) }
                )
            )
        }

        viewModelScope.launch {
            try {
                submitFilesUseCase(
                    SubmitFilesUseCase.Params(
                        canvasContext = CanvasContext.emptyCourseContext(id = courseId),
                        assignmentId = assignmentId,
                        assignmentName = assignmentName,
                        files = currentFiles,
                        attempt = lastAttempt ?: 1L,
                    )
                )
                observeSubmissionTerminal()
            } catch (e: Exception) {
                setSubmissionError()
            }
        }
    }

    private suspend fun observeSubmissionTerminal() {
        var seenNonNull = false
        var terminalIsSuccess = false

        observeSubmissionProgressUseCase(
            ObserveSubmissionProgressUseCase.Params(
                assignmentId = assignmentId,
                submissionType = Assignment.SubmissionType.ONLINE_UPLOAD,
            )
        )
            .distinctUntilChanged { a, b -> a?.progress == b?.progress && a?.errorFlag == b?.errorFlag }
            .onEach { entity ->
                if (entity != null) {
                    seenNonNull = true
                    val progress = entity.progress ?: 0f
                    if (!entity.errorFlag && progress < 100f && progress > 0f) {
                        val determinateProgress = progress / 100f
                        _uiState.update { state ->
                            state.copy(
                                fileState = state.fileState.copy(
                                    files = state.fileState.files.map {
                                        it.copy(state = SubmissionFileState.InProgress(determinateProgress))
                                    }
                                )
                            )
                        }
                    }
                }
            }
            .first { entity ->
                when {
                    // Row disappeared after being seen = worker deleted it = success
                    entity == null && seenNonNull -> { terminalIsSuccess = true; true }
                    entity != null && entity.errorFlag -> true
                    entity != null && (entity.progress ?: 0f) >= 100f -> { terminalIsSuccess = true; true }
                    else -> false
                }
            }

        if (!terminalIsSuccess) {
            val genericErrorMsg = context.getString(R.string.ngc_submission_submissionError)
            val perFileDomain = getSubmissionFileStatesUseCase(
                GetSubmissionFileStatesUseCase.Params(assignmentId)
            )
            val files = if (perFileDomain.isNotEmpty()) {
                perFileDomain.map { it.toSubmissionFileUiState(genericErrorMsg) }
            } else {
                _uiState.value.fileState.files.map { it.copy(state = SubmissionFileState.Error(genericErrorMsg)) }
            }
            _uiState.update { state ->
                state.copy(
                    submitting = false,
                    fileState = state.fileState.copy(files = files),
                    errorMessage = context.getString(R.string.ngc_submission_fileUploadFailed),
                )
            }
        } else {
            fileSavedAtText = ""
            _uiState.update { state ->
                state.copy(
                    submitting = false,
                    fileState = state.fileState.copy(files = emptyList()),
                    savedAtText = "",
                    submitEnabled = false,
                    submissionSuccess = true,
                    onSubmissionSuccessHandled = {
                        _uiState.update { s -> s.copy(submissionSuccess = false) }
                    },
                )
            }
        }
    }

    private fun onSubmissionTypeSelected(type: Assignment.SubmissionType) {
        if (_uiState.value.submitting) return
        if (type == _uiState.value.selectedType) return
        viewModelScope.launch {
            when (type) {
                Assignment.SubmissionType.ONLINE_UPLOAD -> {
                    // Load the file draft on demand (Room-only, no network).
                    // Always reads the real lastActivityDate so the "Saved" label is accurate.
                    val fileDraft = getFileDraftUseCase(GetFileDraftUseCase.Params(assignmentId))
                    val files = fileDraft.files.map { it.toSubmissionFileUiState() }
                    fileSavedAtText = fileDraft.savedAt?.let { formatSavedAt(it) } ?: ""
                    _uiState.update { state ->
                        state.copy(
                            selectedType = type,
                            fileState = state.fileState.copy(files = files),
                            savedAtText = fileSavedAtText,
                            submitEnabled = computeSubmitEnabled(type, state.textState.draftHtml, files, state.urlState.draftUrl),
                        )
                    }
                }
                Assignment.SubmissionType.ONLINE_URL -> {
                    // Load the URL draft on demand (Room-only, no network).
                    val urlDraft = getUrlDraftUseCase(GetUrlDraftUseCase.Params(assignmentId))
                    val url = urlDraft?.url ?: ""
                    urlSavedAtText = urlDraft?.savedAt?.let { formatSavedAt(it) } ?: ""
                    _uiState.update { state ->
                        state.copy(
                            selectedType = type,
                            urlState = state.urlState.copy(
                                draftUrl = url,
                                previewUrl = previewUrlFor(url),
                                urlError = urlErrorFor(url),
                                contentResetKey = state.urlState.contentResetKey + 1,
                            ),
                            savedAtText = urlSavedAtText,
                            submitEnabled = computeSubmitEnabled(type, state.textState.draftHtml, state.fileState.files, url),
                        )
                    }
                }
                else -> {
                    // Load the text draft on demand (Room-only, no network).
                    // Always reads html + real lastActivityDate so a draft saved while on the
                    // file tab (or when the screen opened on the file tab) is fully restored.
                    val textDraft = getTextDraftUseCase(GetTextDraftUseCase.Params(assignmentId))
                    val html = textDraft?.html ?: ""
                    textSavedAtText = textDraft?.savedAt?.let { formatSavedAt(it) } ?: ""
                    _uiState.update { state ->
                        state.copy(
                            selectedType = type,
                            textState = state.textState.copy(
                                draftHtml = html,
                                contentResetKey = state.textState.contentResetKey + 1,
                            ),
                            savedAtText = textSavedAtText,
                            submitEnabled = computeSubmitEnabled(type, html, state.fileState.files, state.urlState.draftUrl),
                        )
                    }
                }
            }
        }
    }

    private fun onFilesPicked(newFiles: List<FileSubmitObject>) {
        if (newFiles.isEmpty()) return
        if (_uiState.value.submitting) return
        val existing = _uiState.value.fileState.files.map { it.toFileSubmitObject() }
        val combined = existing + newFiles
        viewModelScope.launch {
            saveFileDraftUseCase(
                SaveFileDraftUseCase.Params(
                    canvasContext = CanvasContext.emptyCourseContext(id = courseId),
                    assignmentId = assignmentId,
                    assignmentName = assignmentName,
                    files = combined,
                )
            )
            fileSavedAtText = formatSavedAt(Date())
            val updatedDomain = getDraftFilesUseCase(GetDraftFilesUseCase.Params(assignmentId))
            val updatedFiles = updatedDomain.map { it.toSubmissionFileUiState() }
            _uiState.update { state ->
                state.copy(
                    fileState = state.fileState.copy(files = updatedFiles),
                    savedAtText = fileSavedAtText,
                    submitEnabled = computeSubmitEnabled(state.selectedType, state.textState.draftHtml, updatedFiles),
                )
            }
        }
    }

    private fun onFileRemoved(id: Long) {
        if (_uiState.value.submitting) return
        val fileToRemove = _uiState.value.fileState.files.find { it.id == id }
        val remaining = _uiState.value.fileState.files.filter { it.id != id }
        val snapshot = remaining.map { it.toFileSubmitObject() }
        viewModelScope.launch {
            // Delete the removed file's temp copy — but only if no surviving file shares the same
            // path, otherwise we'd orphan the temp file the other row still needs to upload.
            fileToRemove?.fullPath?.let { path ->
                if (remaining.none { it.fullPath == path }) FileUploadUtils.deleteTempFile(path)
            }
            if (snapshot.isEmpty()) {
                deleteDraftUseCase(DeleteDraftUseCase.Params(assignmentId, Assignment.SubmissionType.ONLINE_UPLOAD))
                fileSavedAtText = ""
                _uiState.update { state ->
                    state.copy(
                        fileState = state.fileState.copy(files = emptyList()),
                        savedAtText = "",
                        submitEnabled = false,
                    )
                }
            } else {
                saveFileDraftUseCase(
                    SaveFileDraftUseCase.Params(
                        canvasContext = CanvasContext.emptyCourseContext(id = courseId),
                        assignmentId = assignmentId,
                        assignmentName = assignmentName,
                        files = snapshot,
                    )
                )
                fileSavedAtText = formatSavedAt(Date())
                val updatedDomain = getDraftFilesUseCase(GetDraftFilesUseCase.Params(assignmentId))
                val updatedFiles = updatedDomain.map { it.toSubmissionFileUiState() }
                _uiState.update { state ->
                    state.copy(
                        fileState = state.fileState.copy(files = updatedFiles),
                        savedAtText = fileSavedAtText,
                        submitEnabled = computeSubmitEnabled(state.selectedType, state.textState.draftHtml, updatedFiles, state.urlState.draftUrl),
                    )
                }
            }
        }
    }

    private fun onRetryFile(id: Long) {
        if (_uiState.value.submitting) return
        _uiState.update { it.copy(submitting = true, errorMessage = null) }
        viewModelScope.launch {
            val retried = retryFileSubmissionUseCase(RetryFileSubmissionUseCase.Params(assignmentId))
            if (retried != null) {
                // Existing submission re-enqueued; mark rows InProgress and observe
                _uiState.update { state ->
                    state.copy(
                        fileState = state.fileState.copy(
                            files = state.fileState.files.map { it.copy(state = SubmissionFileState.InProgress()) }
                        )
                    )
                }
                try {
                    observeSubmissionTerminal()
                } catch (e: Exception) {
                    setSubmissionError()
                }
            } else {
                // Fallback: no errored submission found, rebuild from scratch
                submitFiles()
            }
        }
    }

    private fun setSubmissionError() {
        val errorMsg = context.getString(R.string.ngc_submission_submissionError)
        _uiState.update { state ->
            state.copy(
                submitting = false,
                errorMessage = errorMsg,
                // Mark all in-progress file rows as error so the retry affordance is visible.
                fileState = state.fileState.copy(
                    files = state.fileState.files.map { file ->
                        if (file.state is SubmissionFileState.InProgress) {
                            file.copy(state = SubmissionFileState.Error(errorMsg))
                        } else {
                            file
                        }
                    }
                ),
            )
        }
    }

    private fun computeSubmitEnabled(
        type: Assignment.SubmissionType,
        html: String,
        files: List<SubmissionFileUiState>,
        url: String = "",
    ): Boolean = when (type) {
        Assignment.SubmissionType.ONLINE_UPLOAD -> files.isNotEmpty()
        Assignment.SubmissionType.ONLINE_URL -> url.isNotBlank() && urlErrorFor(url) == null
        else -> !isHtmlBlank(html)
    }

    /**
     * Returns an inline error message for [url], or null when the field is blank or a valid web URL.
     * A scheme-less host (e.g. "example.com") is accepted; the scheme is added on submit.
     */
    private fun urlErrorFor(url: String): String? {
        val trimmed = url.trim()
        if (trimmed.isBlank()) return null
        return if (PatternsCompat.WEB_URL.matcher(trimmed).matches()) {
            null
        } else {
            context.getString(R.string.submissionUrlNotAUrlError)
        }
    }

    /** Prepends `https://` when the URL has no scheme, matching the legacy submit behavior. */
    private fun normalizeUrlForSubmit(raw: String): String {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return trimmed
        val hasScheme = trimmed.startsWith("http://", ignoreCase = true) ||
            trimmed.startsWith("https://", ignoreCase = true)
        return if (hasScheme) trimmed else "https://$trimmed"
    }

    /**
     * The https-normalized URL to render in the preview WebView, or blank when there is nothing to
     * preview: empty/invalid input, or a cleartext `http://` URL (which a WebView cannot load).
     */
    private fun previewUrlFor(url: String): String {
        val trimmed = url.trim()
        if (trimmed.isBlank() || urlErrorFor(trimmed) != null) return ""
        val normalized = normalizeUrlForSubmit(trimmed)
        return if (normalized.startsWith("http://", ignoreCase = true)) "" else normalized
    }

    private fun formatSavedAt(date: Date): String {
        val formatted = dateFormatter.formatDateTime(context, date)
        return context.getString(R.string.ngc_submission_draftSaved, formatted)
    }

    companion object {
        const val ARG_COURSE_ID = "courseId"
        const val ARG_ASSIGNMENT_ID = "assignmentId"
        // Optional submission-type apiString to preselect (e.g. when routed from the assignment-details submit chooser).
        const val ARG_SUBMISSION_TYPE = "submissionType"
    }
}

interface SubmissionDateFormatter {
    fun formatDateTime(context: Context, date: Date): String
}

object DefaultSubmissionDateFormatter : SubmissionDateFormatter {
    override fun formatDateTime(context: Context, date: Date): String {
        val datePart = DateHelper.dayMonthDateFormatUniversal.format(date)
        val timePart = DateHelper.getPreferredTimeFormat(context).format(date)
        return "$datePart, $timePart"
    }
}
