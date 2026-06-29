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
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.ngc.R
import com.instructure.ngc.features.submission.domain.DeleteTextDraftUseCase
import com.instructure.ngc.features.submission.domain.GetSubmissionDataUseCase
import com.instructure.ngc.features.submission.domain.ObserveSubmissionProgressUseCase
import com.instructure.ngc.features.submission.domain.SaveTextDraftUseCase
import com.instructure.ngc.features.submission.domain.SubmitTextUseCase
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
    private val saveTextDraftUseCase: SaveTextDraftUseCase,
    private val deleteTextDraftUseCase: DeleteTextDraftUseCase,
    private val submitTextUseCase: SubmitTextUseCase,
    private val observeSubmissionProgressUseCase: ObserveSubmissionProgressUseCase,
    private val dateFormatter: SubmissionDateFormatter,
) : ViewModel() {

    val courseId: Long = savedStateHandle.get<Long>(ARG_COURSE_ID) ?: 0L
    val assignmentId: Long = savedStateHandle.get<Long>(ARG_ASSIGNMENT_ID) ?: 0L

    private var assignmentName: String = ""
    private var lastAttempt: Long? = null

    private val _uiState = MutableStateFlow(NGCSubmissionUiState())
    val uiState: StateFlow<NGCSubmissionUiState> = _uiState.asStateFlow()

    // Text changes are pushed here so we can debounce the persistence writes (DB + savedAtText)
    // while keeping draftHtml / submitEnabled responsive on every keystroke.
    // Null signals "no pending change yet" so the debounce pipeline skips the initial value.
    private val pendingTextFlow = MutableStateFlow<String?>(null)

    init {
        loadData()
        collectDebouncedTextChanges()
    }

    private fun loadData() {
        _uiState.update { it.copy(loading = true, loadError = null) }
        viewModelScope.launch {
            try {
                val data = getSubmissionDataUseCase(courseId, assignmentId)
                assignmentName = data.assignmentName
                lastAttempt = data.lastAttempt

                val dueDateText = data.dueDate?.let { date ->
                    val formatted = dateFormatter.formatDateTime(context, date)
                    context.getString(R.string.ngc_submission_dueDate, formatted)
                } ?: ""

                val savedAtText = data.draftSavedAt?.let { formatSavedAt(it) } ?: ""
                val draftHtml = data.draftHtml ?: ""

                _uiState.update { state ->
                    state.copy(
                        loading = false,
                        loadError = null,
                        courseName = data.courseName,
                        assignmentName = data.assignmentName,
                        dueDateText = dueDateText,
                        submissionTypeLabel = context.getString(R.string.ngc_submission_textEntry),
                        draftHtml = draftHtml,
                        // Bump the reset key so the RCE seeds from the restored draft.
                        contentResetKey = state.contentResetKey + 1,
                        savedAtText = savedAtText,
                        submitEnabled = !isHtmlBlank(draftHtml),
                        onRetry = {},
                    )
                }
            } catch (e: Exception) {
                val errorMessage = context.getString(R.string.ngc_submission_loadError)
                _uiState.update {
                    it.copy(
                        loading = false,
                        loadError = errorMessage,
                        onRetry = { loadData() },
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
                            canvasContext = CanvasContext.emptyCourseContext(id = courseId),
                            assignmentId = assignmentId,
                            assignmentName = assignmentName,
                            html = html,
                        )
                        val savedAtText = formatSavedAt(Date())
                        _uiState.update { it.copy(savedAtText = savedAtText) }
                    } else {
                        deleteTextDraftUseCase(assignmentId)
                        _uiState.update { it.copy(savedAtText = "") }
                    }
                }
        }
    }

    fun onTextChanged(html: String) {
        // Update draftHtml (authoritative, survives rotation) and submitEnabled immediately.
        _uiState.update { it.copy(draftHtml = html, submitEnabled = !isHtmlBlank(html)) }
        // Push to the debounced pipeline for persistence + savedAtText update.
        pendingTextFlow.value = html
    }

    fun onDeleteDraft() {
        viewModelScope.launch {
            deleteTextDraftUseCase(assignmentId)
            _uiState.update { state ->
                state.copy(
                    draftHtml = "",
                    savedAtText = "",
                    submitEnabled = false,
                    contentResetKey = state.contentResetKey + 1,
                )
            }
        }
    }

    fun onSubmit() {
        if (_uiState.value.submitting) return
        val currentHtml = _uiState.value.draftHtml
        _uiState.update { it.copy(submitting = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                submitTextUseCase(
                    canvasContext = CanvasContext.emptyCourseContext(id = courseId),
                    assignmentId = assignmentId,
                    assignmentName = assignmentName,
                    html = currentHtml,
                    attempt = lastAttempt,
                )
                // Observe until the first terminal state (success or error); the Room flow never
                // completes on its own, so `first` both waits for and stops observing after it.
                val terminal = observeSubmissionProgressUseCase(assignmentId).first { entity ->
                    entity != null && (entity.errorFlag || (entity.progress ?: 0f) == 100.0f)
                }
                if (terminal?.errorFlag == true) {
                    setSubmissionError()
                } else {
                    _uiState.update { state ->
                        state.copy(
                            submitting = false,
                            draftHtml = "",
                            savedAtText = "",
                            submitEnabled = false,
                            contentResetKey = state.contentResetKey + 1,
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

    private fun setSubmissionError() {
        _uiState.update {
            it.copy(
                submitting = false,
                errorMessage = context.getString(R.string.ngc_submission_submissionError),
            )
        }
    }

    private fun formatSavedAt(date: Date): String {
        val formatted = dateFormatter.formatDateTime(context, date)
        return context.getString(R.string.ngc_submission_draftSaved, formatted)
    }

    companion object {
        const val ARG_COURSE_ID = "courseId"
        const val ARG_ASSIGNMENT_ID = "assignmentId"
    }
}

/**
 * Thin seam for date/time formatting so unit tests can stub it without needing
 * [android.text.format.DateFormat.is24HourFormat] (which requires a real Android Context).
 */
interface SubmissionDateFormatter {
    /** Formats [date] as a locale-aware date + time string, e.g. "Jun 19, 10:00 AM". */
    fun formatDateTime(context: Context, date: Date): String
}

/**
 * Production implementation that uses [DateHelper] to respect the system 12/24-hour setting
 * and locale, matching how Grades and Assignment Details screens format timestamps.
 */
object DefaultSubmissionDateFormatter : SubmissionDateFormatter {
    override fun formatDateTime(context: Context, date: Date): String {
        val datePart = DateHelper.dayMonthDateFormatUniversal.format(date)
        val timePart = DateHelper.getPreferredTimeFormat(context).format(date)
        return "$datePart, $timePart"
    }
}
