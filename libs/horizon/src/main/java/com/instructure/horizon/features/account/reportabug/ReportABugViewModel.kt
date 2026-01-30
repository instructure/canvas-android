/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.horizon.features.account.reportabug

import android.content.Context
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.apis.ErrorReportAPI
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.horizon.R
import com.instructure.horizon.features.account.AccountEvent
import com.instructure.horizon.features.account.AccountEventHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReportABugViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: ReportABugRepository,
    private val apiPrefs: ApiPrefs,
    private val accountEventHandler: AccountEventHandler
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportABugUiState())
    val uiState: StateFlow<ReportABugUiState> = _uiState.asStateFlow()

    init {
        _uiState.update {
            it.copy(
                onTopicSelected = ::updateTopic,
                onTopicMenuOpenChanged = ::updateTopicMenuOpen,
                onSubjectChanged = ::updateSubject,
                onDescriptionChanged = ::updateDescription,
                onSubmit = ::submitReport,
                onSnackbarDismissed = ::clearSnackbar
            )
        }
    }

    private fun updateTopic(topic: String) {
        _uiState.update { it.copy(selectedTopic = topic, topicError = null) }
    }

    private fun updateTopicMenuOpen(isOpen: Boolean) {
        _uiState.update { it.copy(isTopicMenuOpen = isOpen) }
    }

    private fun updateSubject(subject: TextFieldValue) {
        _uiState.update { it.copy(subject = subject, subjectError = null) }
    }

    private fun updateDescription(description: TextFieldValue) {
        _uiState.update { it.copy(description = description, descriptionError = null) }
    }

    private fun submitReport() {
        val currentState = _uiState.value
        val email = apiPrefs.user?.email ?: ""

        val topicError = if (currentState.selectedTopic == null) {
            context.getString(R.string.reportAProblemTopicRequired)
        } else null

        val subjectError = if (currentState.subject.text.isBlank()) {
            context.getString(R.string.reportAProblemSubjectRequired)
        } else null

        val descriptionError = if (currentState.description.text.isBlank()) {
            context.getString(R.string.reportAProblemDescriptionRequired)
        } else null

        if (topicError != null || subjectError != null || descriptionError != null) {
            _uiState.update {
                it.copy(
                    topicError = topicError,
                    subjectError = subjectError,
                    descriptionError = descriptionError
                )
            }
            return
        }

        val severity = mapTopicToSeverity(currentState.selectedTopic!!)

        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            try {
                repository.submitErrorReport(
                    subject = currentState.subject.text,
                    description = currentState.description.text,
                    email = email,
                    severity = severity
                )

                accountEventHandler.postEvent(
                    AccountEvent.ShowSnackbar(
                        context.getString(R.string.reportAProblemSuccessMessage)
                    )
                )

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        shouldNavigateBack = true
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        snackbarMessage = context.getString(R.string.reportAProblemErrorMessage)
                    )
                }
            }
        }
    }

    private fun mapTopicToSeverity(topic: String): String {
        return when (topic) {
            context.getString(R.string.reportAProblemTopicSuggestion) -> ErrorReportAPI.Severity.COMMENT.tag
            context.getString(R.string.reportAProblemTopicGeneralHelp) -> ErrorReportAPI.Severity.NOT_URGENT.tag
            context.getString(R.string.reportAProblemTopicMinorIssue) -> ErrorReportAPI.Severity.WORKAROUND_POSSIBLE.tag
            context.getString(R.string.reportAProblemTopicUrgentIssue) -> ErrorReportAPI.Severity.BLOCKING.tag
            context.getString(R.string.reportAProblemTopicCriticalError) -> ErrorReportAPI.Severity.CRITICAL.tag
            else -> ErrorReportAPI.Severity.COMMENT.tag
        }
    }

    private fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }
}
