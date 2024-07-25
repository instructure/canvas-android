package com.instructure.pandautils.features.inbox.compose

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.models.CanvasContext
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InboxComposeViewModel @Inject constructor(
    private val inboxComposeRepository: InboxComposeRepository
): ViewModel() {
    private val _uiState = MutableStateFlow(
        InboxComposeUiState()
    )
    val uiState = _uiState.asStateFlow()

    private val _contextPickerUiState = MutableStateFlow(
        ContextPickerUiState(
            courses = emptyList(),
            groups = emptyList(),
            selectedContext = null,
            isLoading = true
        )
    )
    val contextPickerUiState = _contextPickerUiState.asStateFlow()

    private val _recipientPickerUiState = MutableStateFlow(
        RecipientPickerUiState(
            recipients = emptyList(),
            selectedRecipients = emptyList(),
            isLoading = true
        )
    )
    val recipientPickerUiState = _recipientPickerUiState.asStateFlow()

    init {
        loadContexts()
    }

    fun updateUiState(uiState: InboxComposeUiState) {
        viewModelScope.launch {
            _uiState.emit(uiState)
        }
    }

    fun updateUiState(uiState: ContextPickerUiState) {
        viewModelScope.launch {
            _contextPickerUiState.emit(uiState)
        }
    }

    fun updateUiState(uiState: RecipientPickerUiState) {
        viewModelScope.launch {
            _recipientPickerUiState.emit(uiState)
        }
    }


    fun loadContexts() {
        updateUiState(
            contextPickerUiState.value.copy(
                isLoading = true
            )
        )

        viewModelScope.launch {
            val courses = inboxComposeRepository.getCourses()
            val groups = inboxComposeRepository.getGroups()
            updateUiState(
                contextPickerUiState.value.copy(
                    courses = courses,
                    groups = groups,
                    isLoading = false
                )
            )
        }
    }

    fun loadRecipients(searchQuery: String, context: CanvasContext) {
        viewModelScope.launch {
            val recipients = inboxComposeRepository.getRecipients(searchQuery, context)
            updateUiState(
                recipientPickerUiState.value.copy(
                    recipients = recipients,
                    roles = recipients.map { it.enrollment }.distinct(),
                    isLoading = false
                )
            )
        }
    }

    fun createConversation() {
        uiState.value.selectedContext?.let { context ->
            viewModelScope.launch {
                inboxComposeRepository.createConversation(
                    recipients = uiState.value.selectedRecipients,
                    subject = uiState.value.subject.text,
                    message = uiState.value.body.text,
                    context = context,
                    attachments = emptyList(),
                    isIndividual = uiState.value.sendIndividual
                )
            }
        }
    }
}