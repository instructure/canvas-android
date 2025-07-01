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
package com.instructure.horizon.features.inbox.compose

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Recipient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class HorizonInboxComposeViewModel @Inject constructor(
    private val repository: HorizonInboxComposeRepository
): ViewModel() {
    private val _uiState = MutableStateFlow(
        HorizonInboxComposeUiState(
            onCourseSelected = ::onCourseSelected,
            onRecipientSearchQueryChanged = ::onRecipientSearchQueryChanged,
            onRecipientSelected = ::onRecipientSelected,
            onRecipientRemoved = ::onRecipientRemoved,
            onSendConversation = ::sendConversation,
            onSendIndividuallyChanged = ::onSendIndividuallyChanged,
            onSubjectChanged = ::onSubjectChanged,
            onBodyChanged = ::onBodyChanged
        )
    )

    val uiState = _uiState.asStateFlow()

    private val searchQuery: MutableStateFlow<String> = MutableStateFlow("")

    init {
        viewModelScope.launch {
            val courses = repository.getAllInboxCourses(forceNetwork = true)
            _uiState.update {
                it.copy(
                    coursePickerOptions = courses,
                )
            }

            searchQuery.debounce(200).collectLatest { query ->
                fetchRecipients()
            }
        }
    }

    private fun fetchRecipients() {
        uiState.value.selectedCourse?.id?.let { courseId ->
            viewModelScope.launch {
                _uiState.update { it.copy(isRecipientPickerLoading = true) }
                val recipients = repository.getRecipients(
                    courseId = courseId,
                    searchQuery = uiState.value.recipientSearchQuery.text,
                )
                _uiState.update {
                    it.copy(
                        recipientPickerOptions = recipients,
                        isRecipientPickerLoading = false
                    )
                }
            }
        }
    }

    private fun sendConversation(onFinished: () -> Unit = {}) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSendLoading = true) }

            repository.createConversation(
                recipientIds = uiState.value.selectedRecipients.mapNotNull { it.stringId },
                body = uiState.value.body.text,
                subject = uiState.value.subject.text,
                contextCode = uiState.value.selectedCourse!!.contextId,
                attachmentIds = emptyList<Long>().toLongArray(),
                isBulkMessage = uiState.value.isSendIndividually
            )

            _uiState.update { it.copy(isSendLoading = false) }

            onFinished()
        }
    }

    private fun onCourseSelected(course: Course) {
        _uiState.update {
            it.copy(selectedCourse = course)
        }
        fetchRecipients()
    }

    private fun onRecipientSearchQueryChanged(query: TextFieldValue) {
        _uiState.update {
            it.copy(recipientSearchQuery = query)
        }
        searchQuery.tryEmit(query.text)
    }

    private fun onRecipientSelected(recipient: Recipient) {
        _uiState.update {
            it.copy(selectedRecipients = it.selectedRecipients + recipient)
        }
    }

    fun onRecipientRemoved(recipient: Recipient) {
        _uiState.update {
            it.copy(selectedRecipients = it.selectedRecipients - recipient)
        }
    }

    private fun onSendIndividuallyChanged(isSendIndividually: Boolean) {
        _uiState.update {
            it.copy(isSendIndividually = isSendIndividually)
        }
    }

    private fun onSubjectChanged(subject: TextFieldValue) {
        _uiState.update {
            it.copy(subject = subject)
        }
    }

    private fun onBodyChanged(body: TextFieldValue) {
        _uiState.update {
            it.copy(body = body)
        }
    }
}