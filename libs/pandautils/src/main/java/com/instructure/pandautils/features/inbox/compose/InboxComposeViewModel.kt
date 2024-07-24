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
    private val contextPickerUiState = ContextPickerUiState(
        courses = emptyList(),
        groups = emptyList(),
        selectedContext = null,
        isLoading = true
    )
    private val recipientPickerUiState = RecipientPickerUiState(
        recipients = emptyList(),
        selectedRecipients = emptyList(),
        isLoading = true
    )
    private val _uiState = MutableStateFlow(
        InboxComposeUiState(
            contextPickerUiState = contextPickerUiState,
            recipientsState = recipientPickerUiState,
        )
    )
    val uiState = _uiState.asStateFlow()

    init {
        loadContexts()
    }

    fun updateUiState(uiState: InboxComposeUiState) {
        viewModelScope.launch {
            _uiState.emit(uiState)
        }
    }

    fun loadContexts() {
        updateUiState(
            uiState.value.copy(
                contextPickerUiState = contextPickerUiState.copy(
                    isLoading = true
                )
            )
        )

        viewModelScope.launch {
            val courses = inboxComposeRepository.getCourses()
            val groups = inboxComposeRepository.getGroups()
            updateUiState(
                uiState.value.copy(
                    contextPickerUiState = contextPickerUiState.copy(
                        courses = courses,
                        groups = groups,
                        isLoading = false
                    )
                )
            )
        }
    }

    fun loadRecipients(searchQuery: String, context: CanvasContext) {
        viewModelScope.launch {
            val recipients = inboxComposeRepository.getRecipients(searchQuery, context)
            updateUiState(
                uiState.value.copy(
                    recipientsState = recipientPickerUiState.copy(
                        recipients = recipients,
                        roles = recipients.map { it.recipientType.name }.distinct(),
                        isLoading = false
                    )
                )
            )
        }
    }
}