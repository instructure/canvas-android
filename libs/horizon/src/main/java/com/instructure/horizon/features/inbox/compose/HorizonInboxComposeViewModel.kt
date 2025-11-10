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

import android.content.Context
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Recipient
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.horizon.R
import com.instructure.horizon.features.inbox.InboxEvent
import com.instructure.horizon.features.inbox.InboxEventHandler
import com.instructure.horizon.features.inbox.attachment.HorizonInboxAttachment
import com.instructure.horizon.features.inbox.attachment.HorizonInboxAttachmentState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class HorizonInboxComposeViewModel @Inject constructor(
    private val repository: HorizonInboxComposeRepository,
    @ApplicationContext private val context: Context,
    private val inboxEventHandler: InboxEventHandler
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
            onBodyChanged = ::onBodyChanged,
            onDismissSnackbar = ::onDismissSnackbar,
            onShowAttachmentPickerChanged = ::onShowAttachmentPickerChanged,
            onAttachmentsChanged = ::onAttachmentsChanged,
            updateShowExitConfirmationDialog = ::updateShowExitConfirmationDialog
        )
    )

    val uiState = _uiState.asStateFlow()

    private val searchQuery: MutableStateFlow<String> = MutableStateFlow("")

    init {
        viewModelScope.tryLaunch {
            val courses = repository.getAllInboxCourses(forceNetwork = true)
            val selectedCourse = if (courses.size == 1) {
                courses.firstOrNull()
            } else {
                null
            }
            _uiState.update {
                it.copy(
                    selectedCourse = selectedCourse,
                    coursePickerOptions = courses,
                )
            }

            searchQuery
                .debounce(200)
                .filter { it.length >= uiState.value.minQueryLength }
                .collectLatest { query ->
                    fetchRecipients()
                }
        } catch {
            _uiState.update { it.copy(snackbarMessage = context.getString(R.string.inboxComposeCourseErrorMessage)) }
        }
    }

    private fun fetchRecipients() {
        uiState.value.selectedCourse?.id?.let { courseId ->
            viewModelScope.tryLaunch {
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
            } catch {
                _uiState.update { it.copy(snackbarMessage = context.getString(R.string.inboxComposeRecipientErrorMessage)) }
                _uiState.update { it.copy(isRecipientPickerLoading = false) }
            }
        }
    }

    private fun sendConversation(onFinished: () -> Unit = {}) {
        val selectedCourse = uiState.value.selectedCourse
        var isError = false
        if (selectedCourse == null) {
            _uiState.update { it.copy(courseErrorMessage = context.getString(R.string.inboxComposeSelectCourseErrorMessage)) }
            isError = true
        }
        if (uiState.value.selectedRecipients.isEmpty()) {
            _uiState.update { it.copy(recipientErrorMessage = context.getString(R.string.inboxComposeSelectRecipientErrorMessage)) }
            isError = true
        }
        if (uiState.value.subject.text.isBlank()) {
            _uiState.update { it.copy(subjectErrorMessage = context.getString(R.string.inboxComposeSubjectErrorMessage)) }
            isError = true
        }
        if (uiState.value.body.text.isBlank()) {
            _uiState.update { it.copy(bodyErrorMessage = context.getString(R.string.inboxComposeBodyErrorMessage)) }
            isError = true
        }
        if (!uiState.value.attachments.all { it.state is HorizonInboxAttachmentState.Success }) {
            _uiState.update { it.copy(attachmentsErrorMessage = context.getString(R.string.inboxComposeAttachmentsErrorMessage)) }
            isError = true
        }
        if (isError || selectedCourse == null) {
            return
        }

        viewModelScope.tryLaunch {
            _uiState.update { it.copy(isSendLoading = true) }
            repository.createConversation(
                recipientIds = uiState.value.selectedRecipients.mapNotNull { it.stringId },
                body = uiState.value.body.text,
                subject = uiState.value.subject.text,
                contextCode = selectedCourse.contextId,
                attachmentIds = uiState.value.attachments.map { it.id }.toLongArray(),
                isBulkMessage = uiState.value.isSendIndividually || uiState.value.selectedRecipients.size >= 100
            )
            repository.invalidateConversationListCachedResponse()

            viewModelScope.launch {
                inboxEventHandler.postEvent(
                    InboxEvent.ConversationCreated(
                        context.getString(R.string.inboxListConversationCreatedMessage)
                    )
                )
            }

            _uiState.update { it.copy(isSendLoading = false) }
            onFinished()
        } catch {
            _uiState.update { it.copy(snackbarMessage = context.getString(R.string.inboxComposeSendErrorMessage)) }
            _uiState.update { it.copy(isSendLoading = false) }
        }
    }

    private fun onCourseSelected(course: Course) {
        _uiState.update {
            it.copy(selectedCourse = course, courseErrorMessage = null)
        }
    }

    private fun onRecipientSearchQueryChanged(query: TextFieldValue) {
        _uiState.update {
            it.copy(recipientSearchQuery = query)
        }
        searchQuery.tryEmit(query.text)
    }

    private fun onRecipientSelected(recipient: Recipient) {
        _uiState.update {
            it.copy(
                recipientSearchQuery = TextFieldValue(""),
                selectedRecipients = it.selectedRecipients + recipient,
                recipientErrorMessage = null
            )
        }
    }

    fun onRecipientRemoved(recipient: Recipient) {
        _uiState.update {
            it.copy(
                selectedRecipients = it.selectedRecipients - recipient,
                recipientErrorMessage = null
            )
        }
    }

    private fun onSendIndividuallyChanged(isSendIndividually: Boolean) {
        _uiState.update {
            it.copy(isSendIndividually = isSendIndividually)
        }
    }

    private fun onSubjectChanged(subject: TextFieldValue) {
        _uiState.update {
            it.copy(subject = subject, subjectErrorMessage = null)
        }
    }

    private fun onBodyChanged(body: TextFieldValue) {
        _uiState.update {
            it.copy(body = body, bodyErrorMessage = null)
        }
    }

    private fun onDismissSnackbar() {
        _uiState.update {
            it.copy(snackbarMessage = null)
        }
    }

    private fun onAttachmentsChanged(attachments: List<HorizonInboxAttachment>) {
        _uiState.update {
            it.copy(attachments = attachments)
        }
    }

    private fun onShowAttachmentPickerChanged(show: Boolean) {
        _uiState.update {
            it.copy(showAttachmentPicker = show)
        }
    }

    private fun updateShowExitConfirmationDialog(show: Boolean) {
        _uiState.update {
            it.copy(showExitConfirmationDialog = show)
        }
    }
}