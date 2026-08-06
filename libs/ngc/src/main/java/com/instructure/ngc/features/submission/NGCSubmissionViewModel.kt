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
import com.instructure.ngc.features.submission.domain.GetCanvaDocSessionUseCase
import com.instructure.ngc.features.submission.domain.GetDraftFilesUseCase
import com.instructure.ngc.features.submission.domain.GetFileDraftUseCase
import com.instructure.ngc.features.submission.domain.GetSubmissionDataUseCase
import com.instructure.ngc.features.submission.domain.GetMediaDraftUseCase
import com.instructure.ngc.features.submission.domain.GetSubmissionFileStatesUseCase
import com.instructure.ngc.features.submission.domain.GetTextDraftUseCase
import com.instructure.ngc.features.submission.domain.GetUrlDraftUseCase
import com.instructure.ngc.features.submission.domain.ObserveSubmissionProgressUseCase
import com.instructure.ngc.features.submission.domain.ResumeInFlightSubmissionUseCase
import com.instructure.ngc.features.submission.domain.RetryFileSubmissionUseCase
import com.instructure.ngc.features.submission.domain.SaveFileDraftUseCase
import com.instructure.ngc.features.submission.domain.SaveMediaDraftUseCase
import com.instructure.ngc.features.submission.domain.SaveTextDraftUseCase
import com.instructure.ngc.features.submission.domain.SaveUrlDraftUseCase
import com.instructure.ngc.features.submission.domain.SubmitFilesUseCase
import com.instructure.ngc.features.submission.domain.SubmitStudentAnnotationUseCase
import com.instructure.ngc.features.submission.domain.SubmitMediaUseCase
import com.instructure.ngc.features.submission.domain.SubmitTextUseCase
import com.instructure.ngc.features.submission.domain.SubmitUrlUseCase
import com.instructure.ngc.features.submission.supportingpane.SubmissionSupportingPaneUiState
import com.instructure.pandautils.utils.FileUploadUtils
import com.instructure.pandautils.utils.isHtmlBlank
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
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
    private val getCanvaDocSessionUseCase: GetCanvaDocSessionUseCase,
    private val submitStudentAnnotationUseCase: SubmitStudentAnnotationUseCase,
    private val getMediaDraftUseCase: GetMediaDraftUseCase,
    private val saveMediaDraftUseCase: SaveMediaDraftUseCase,
    private val submitMediaUseCase: SubmitMediaUseCase,
    private val dateFormatter: SubmissionDateFormatter,
) : ViewModel() {

    val courseId: Long = savedStateHandle.get<Long>(ARG_COURSE_ID) ?: 0L
    val assignmentId: Long = savedStateHandle.get<Long>(ARG_ASSIGNMENT_ID) ?: 0L
    private val requestedTypeApiString: String? = savedStateHandle.get<String>(ARG_SUBMISSION_TYPE)

    private var assignmentName: String = ""
    private var lastAttempt: Long? = null

    // Populated by loadData() from SubmissionData.annotatableAttachmentId (the assignment is already
    // fetched once there; no separate use case call needed), read by submitStudentAnnotation().
    private var lastAnnotatableAttachmentId: Long? = null

    // Populated by loadData() from SubmissionData.submissionId; used to key the canvadocs "draft" session.
    private var submissionId: Long? = null

    // Registered by the Compose layer via StudentAnnotationUiState.registerAnnotationFlush while a
    // PdfAnnotationHostFragment is hosted; null when no fragment is currently attached.
    private var annotationFlush: (suspend () -> Boolean)? = null

    // The in-flight canvadocs draft-session load, cancelled on re-entry so overlapping calls can't each
    // create a session server-side and race on sessionUrl — see loadCanvaDocSession().
    private var canvaDocSessionJob: Job? = null

    // Each submission type keeps its own draft timestamp so switching types shows the right "Saved" time.
    private var textSavedAtText: String = ""
    private var fileSavedAtText: String = ""
    private var urlSavedAtText: String = ""
    private var mediaSavedAtText: String = ""

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

                submissionId = finalData.submissionId
                lastAnnotatableAttachmentId = finalData.annotatableAttachmentId.takeIf { it != 0L }

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
                mediaSavedAtText = if (defaultType == Assignment.SubmissionType.MEDIA_RECORDING) {
                    finalData.mediaDraftSavedAt?.let { formatSavedAt(it) } ?: ""
                } else {
                    ""
                }

                val initialSavedAtText = when (defaultType) {
                    Assignment.SubmissionType.ONLINE_UPLOAD -> fileSavedAtText
                    Assignment.SubmissionType.ONLINE_URL -> urlSavedAtText
                    Assignment.SubmissionType.MEDIA_RECORDING -> mediaSavedAtText
                    // No local draft for annotations — see the STUDENT_ANNOTATION branch of
                    // onSubmissionTypeSelected. Without this the screen would open on the annotation
                    // pane already showing a text draft's timestamp and Delete draft button.
                    Assignment.SubmissionType.STUDENT_ANNOTATION -> ""
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
                        rubricUiState = finalData.rubricUiState,
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
                        mediaState = state.mediaState.copy(
                            clipName = finalData.draftMediaName,
                            clipPath = finalData.draftMediaPath,
                            clipContentType = finalData.draftMediaContentType,
                            onClipReady = ::onMediaClipReady,
                        ),
                        studentAnnotationState = state.studentAnnotationState.copy(
                            registerAnnotationFlush = ::registerAnnotationFlush,
                        ),
                        savedAtText = initialSavedAtText,
                        submitEnabled = computeSubmitEnabled(defaultType, textDraftHtml, draftFiles, urlDraft, finalData.draftMediaPath),
                        onRetry = {},
                        supportingPane = SubmissionSupportingPaneUiState(
                            loading = false,
                            instructionsHtml = finalData.instructionsHtml.orEmpty(),
                        ),
                    )
                }

                // STUDENT_ANNOTATION can legitimately be the default/only available type for an
                // assignment (unlike the other types, its content pane has no on-demand load path via
                // onSubmissionTypeSelected unless the user switches away and back), so the canvadoc
                // session must be kicked off here too, not only from onSubmissionTypeSelected.
                if (defaultType == Assignment.SubmissionType.STUDENT_ANNOTATION) {
                    loadCanvaDocSession()
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
                Assignment.SubmissionType.MEDIA_RECORDING -> {
                    val clipPath = _uiState.value.mediaState.clipPath
                    deleteDraftUseCase(DeleteDraftUseCase.Params(assignmentId, Assignment.SubmissionType.MEDIA_RECORDING))
                    clipPath?.let { FileUploadUtils.deleteTempFile(it) }
                    mediaSavedAtText = ""
                    _uiState.update { state ->
                        state.copy(
                            mediaState = state.mediaState.copy(clipName = null, clipPath = null, clipContentType = null),
                            savedAtText = "",
                            submitEnabled = false,
                        )
                    }
                }
                // STUDENT_ANNOTATION has no local draft to delete: annotations are persisted to the
                // canvadocs draft session as they are drawn, not stored in a CreateSubmissionEntity row.
                // An explicit no-op branch is required — without it the type falls into the `else` below
                // and deletes the student's ONLINE_TEXT_ENTRY draft instead, which is silent data loss.
                // Giving Student Annotation real draft semantics (including what "delete draft" should
                // mean for server-side annotations) is tracked separately; this only closes the data-loss
                // path. The Delete draft affordance itself is kept off the annotation pane by clearing
                // savedAtText in onSubmissionTypeSelected.
                Assignment.SubmissionType.STUDENT_ANNOTATION -> Unit
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
            Assignment.SubmissionType.MEDIA_RECORDING -> submitMedia()
            Assignment.SubmissionType.STUDENT_ANNOTATION -> submitStudentAnnotation()
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

    private fun submitMedia() {
        val clip = _uiState.value.mediaState
        val clipPath = clip.clipPath
        if (clipPath == null) {
            _uiState.update { it.copy(submitting = false) }
            return
        }
        val mediaType = mediaTypeFor(clip.clipContentType)
        if (mediaType == null) {
            // Should not happen — onMediaClipReady already rejects non-audio/video clips — but
            // guard rather than silently mislabeling an unexpected contentType as "video".
            setSubmissionError()
            return
        }
        viewModelScope.launch {
            try {
                submitMediaUseCase(
                    SubmitMediaUseCase.Params(
                        canvasContext = CanvasContext.emptyCourseContext(id = courseId),
                        assignmentId = assignmentId,
                        assignmentName = assignmentName,
                        mediaFilePath = clipPath,
                        mediaType = mediaType,
                        attempt = lastAttempt,
                    )
                )

                var seenNonNull = false
                var terminalIsSuccess = false
                observeSubmissionProgressUseCase(
                    ObserveSubmissionProgressUseCase.Params(
                        assignmentId = assignmentId,
                        submissionType = Assignment.SubmissionType.MEDIA_RECORDING,
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
                    mediaSavedAtText = ""
                    _uiState.update { state ->
                        state.copy(
                            submitting = false,
                            mediaState = state.mediaState.copy(clipName = null, clipPath = null, clipContentType = null),
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

    // Registers (or clears, when null) the currently-hosted PdfAnnotationHostFragment's flush
    // function, so submitStudentAnnotation() can await any in-flight annotation writes before
    // finalizing the attempt. See StudentAnnotationUiState.registerAnnotationFlush.
    private fun registerAnnotationFlush(flush: (suspend () -> Boolean)?) {
        annotationFlush = flush
    }

    /**
     * Loads the canvadocs "draft" session so the inline PSPDFKit view has a document to render/annotate.
     *
     * Held in a cancellable [canvaDocSessionJob] and cancelled on re-entry, because this is reachable
     * more than once concurrently (double-tapping Retry, or `loadData`'s default-type kick-off racing an
     * `onSubmissionTypeSelected`). Overlapping calls would each create a NEW draft session server-side and
     * race to write `sessionUrl` — and since `sessionUrl` keys the `PdfAnnotationHostFragment` in
     * `StudentAnnotationContent`, a late-arriving loser would swap in a fresh `PdfFragment` and discard
     * whatever the student had already annotated against the first session.
     *
     * Both terminal branches also re-check the selected type before writing: the load is not cancelled by
     * a type switch alone (the user may switch back), so without the guard a slow session load that lands
     * after the user moved to ONLINE_TEXT_ENTRY would enable Submit for an empty text draft — or, on the
     * failure path, disable Submit for a type that has a perfectly valid one.
     */
    private fun loadCanvaDocSession() {
        canvaDocSessionJob?.cancel()
        _uiState.update { state ->
            state.copy(
                submitEnabled = false,
                studentAnnotationState = state.studentAnnotationState.copy(
                    sessionLoading = true,
                    sessionLoadError = null,
                    registerAnnotationFlush = ::registerAnnotationFlush,
                )
            )
        }
        canvaDocSessionJob = viewModelScope.launch {
            try {
                val session = getCanvaDocSessionUseCase(
                    GetCanvaDocSessionUseCase.Params(submissionId = submissionId)
                )
                _uiState.update { state ->
                    if (state.selectedType != Assignment.SubmissionType.STUDENT_ANNOTATION) {
                        state
                    } else {
                        state.copy(
                            submitEnabled = true,
                            studentAnnotationState = state.studentAnnotationState.copy(
                                sessionUrl = session.sessionUrl,
                                sessionLoading = false,
                                sessionLoadError = null,
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { state ->
                    if (state.selectedType != Assignment.SubmissionType.STUDENT_ANNOTATION) {
                        state
                    } else {
                        state.copy(
                            submitEnabled = false,
                            studentAnnotationState = state.studentAnnotationState.copy(
                                sessionLoading = false,
                                sessionLoadError = context.getString(R.string.ngc_submission_loadError),
                                onRetrySessionLoad = { loadCanvaDocSession() },
                            )
                        )
                    }
                }
            }
        }
    }

    // Finalizes the attempt via the same startStudentAnnotationSubmission() → SubmissionUploadWorker
    // path the file/url types use. Awaits any in-flight PSPDFKit annotation writes first (see
    // annotationFlush / StudentAnnotationUiState.registerAnnotationFlush) so the latest edits have had
    // a chance to reach the canvadocs draft session before the attempt is marked submitted, and now
    // aborts (surfacing the same inline error/retry state the other types use) if any tracked write is
    // known to have failed — see PdfSubmissionView.awaitPendingAnnotationWrites. If annotationFlush is
    // still null (the Compose layer hasn't registered the hosted fragment's flush function yet — e.g.
    // the user tapped Submit before AndroidFragment's onUpdate fired), there is nothing to safely await,
    // so this treats that as a failure too, rather than risk silently finalizing without flushing.
    private fun submitStudentAnnotation() {
        val annotatableAttachmentId = lastAnnotatableAttachmentId
        if (annotatableAttachmentId == null) {
            setSubmissionError()
            return
        }
        viewModelScope.launch {
            try {
                val flushed = annotationFlush?.invoke() ?: false
                if (!flushed) {
                    setSubmissionError()
                    return@launch
                }

                submitStudentAnnotationUseCase(
                    SubmitStudentAnnotationUseCase.Params(
                        canvasContext = CanvasContext.emptyCourseContext(id = courseId),
                        assignmentId = assignmentId,
                        assignmentName = assignmentName,
                        annotatableAttachmentId = annotatableAttachmentId,
                        attempt = lastAttempt,
                    )
                )

                var seenNonNull = false
                var terminalIsSuccess = false
                observeSubmissionProgressUseCase(
                    ObserveSubmissionProgressUseCase.Params(
                        assignmentId = assignmentId,
                        submissionType = Assignment.SubmissionType.STUDENT_ANNOTATION,
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
                    // The flushed fragment is torn down with the screen; drop the reference so a
                    // later submit can never await a dead view's writes.
                    annotationFlush = null
                    _uiState.update { state ->
                        state.copy(
                            submitting = false,
                            // studentAnnotationState is deliberately left ALONE. It previously got a
                            // fresh StudentAnnotationUiState(), which blanked sessionUrl — and
                            // StudentAnnotationContent treats a blank sessionUrl as a load failure, so
                            // the pane flashed "Failed to load assignment" over the annotation surface
                            // in the moment between success and the screen being torn down, with a
                            // Retry button whose callback had just been reset to a no-op (MBL-19937 QA).
                            // The success overlay covers the pane anyway, so there is nothing to clear.
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
                Assignment.SubmissionType.MEDIA_RECORDING -> {
                    // Load the media draft on demand (Room-only, no network).
                    val mediaDraft = getMediaDraftUseCase(GetMediaDraftUseCase.Params(assignmentId))
                    mediaSavedAtText = mediaDraft?.savedAt?.let { formatSavedAt(it) } ?: ""
                    _uiState.update { state ->
                        state.copy(
                            selectedType = type,
                            mediaState = state.mediaState.copy(
                                clipName = mediaDraft?.name,
                                clipPath = mediaDraft?.path,
                                clipContentType = mediaDraft?.contentType,
                            ),
                            savedAtText = mediaSavedAtText,
                            submitEnabled = computeSubmitEnabled(
                                type, state.textState.draftHtml, state.fileState.files, state.urlState.draftUrl, mediaDraft?.path
                            ),
                        )
                    }
                }
                Assignment.SubmissionType.STUDENT_ANNOTATION -> {
                    _uiState.update { state ->
                        state.copy(
                            selectedType = type,
                            submitEnabled = false,
                            // Cleared, unlike the other types which set their own draft timestamp here.
                            // Student Annotation has no local draft, so leaving the previous type's
                            // value in place would render the "Saved … / Delete draft" footer over the
                            // annotation pane — an affordance that cannot do what it says, since the
                            // annotations live in the canvadocs draft session rather than a local row.
                            savedAtText = "",
                            studentAnnotationState = state.studentAnnotationState.copy(
                                registerAnnotationFlush = ::registerAnnotationFlush,
                            ),
                        )
                    }
                    loadCanvaDocSession()
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

    /**
     * Media is a single-clip draft: a new pick/capture replaces whatever was previously attached.
     * Saved immediately (no debounce — unlike text/url, this is a discrete pick action, not typing).
     */
    private fun onMediaClipReady(clip: FileSubmitObject) {
        if (_uiState.value.submitting) return
        // The chooser's EXTRA_MIME_TYPES filter is advisory only (some pickers ignore it), so a
        // non-audio/video pick must still be rejected here rather than attached as a draft.
        if (mediaTypeFor(clip.contentType) == null) {
            _uiState.update {
                it.copy(errorMessage = context.getString(R.string.ngc_submission_unsupportedMediaType))
            }
            return
        }
        val previousPath = _uiState.value.mediaState.clipPath
        // Attach the clip to UI state synchronously (no await on the Room write) so the media body
        // swaps from the recorder straight to the clip card in a single recomposition — the recorder
        // holder is already Idle by the time this runs, and any gap here would flash its default UI.
        mediaSavedAtText = formatSavedAt(Date())
        _uiState.update { state ->
            state.copy(
                mediaState = state.mediaState.copy(
                    clipName = clip.name,
                    clipPath = clip.fullPath,
                    clipContentType = clip.contentType,
                ),
                savedAtText = mediaSavedAtText,
                submitEnabled = computeSubmitEnabled(
                    state.selectedType, state.textState.draftHtml, state.fileState.files, state.urlState.draftUrl, clip.fullPath
                ),
            )
        }
        viewModelScope.launch {
            saveMediaDraftUseCase(
                SaveMediaDraftUseCase.Params(
                    canvasContext = CanvasContext.emptyCourseContext(id = courseId),
                    assignmentId = assignmentId,
                    assignmentName = assignmentName,
                    clip = clip,
                )
            )
            // Replacing an existing clip — clean up the old temp file now that the draft row
            // points at the new one. Guard against deleting a path that's (unexpectedly) shared.
            if (previousPath != null && previousPath != clip.fullPath) {
                FileUploadUtils.deleteTempFile(previousPath)
            }
        }
    }

    /**
     * Maps a resolved contentType to the API's media-submission `mediaType`, or null when it's
     * neither audio nor video. Never falls through to a default — an unrecognized contentType
     * (including null) must be treated as a rejection, not silently submitted as "video".
     */
    private fun mediaTypeFor(contentType: String?): String? = when {
        contentType?.startsWith("audio/") == true -> "audio"
        contentType?.startsWith("video/") == true -> "video"
        else -> null
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
        mediaClipPath: String? = null,
    ): Boolean = when (type) {
        Assignment.SubmissionType.ONLINE_UPLOAD -> files.isNotEmpty()
        Assignment.SubmissionType.ONLINE_URL -> url.isNotBlank() && urlErrorFor(url) == null
        Assignment.SubmissionType.MEDIA_RECORDING -> mediaClipPath != null
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
