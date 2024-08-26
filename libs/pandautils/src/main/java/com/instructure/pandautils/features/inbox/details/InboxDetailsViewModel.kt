package com.instructure.pandautils.features.inbox.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InboxDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: InboxDetailsRepository,
): ViewModel() {

    val conversationId: Long? = savedStateHandle.get<Long>(InboxDetailsFragment.CONVERSATION_ID)

    private val _uiState = MutableStateFlow(InboxDetailsUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = Channel<InboxDetailsFragmentAction>()
    val events = _events.receiveAsFlow()

    init {
        _uiState.update { it.copy(conversationId = conversationId) }
        getConversation()
    }

    fun handleAction(action: InboxDetailsAction) {
        when (action) {
            is InboxDetailsAction.CloseFragment -> {
                viewModelScope.launch {
                    _events.send(InboxDetailsFragmentAction.CloseFragment)
                }
            }

            InboxDetailsAction.RefreshCalled -> {
                getConversation()
            }
        }
    }

    private fun getConversation() {
        conversationId?.let {
            viewModelScope.launch {
                _uiState.update { it.copy(state = ScreenState.Loading) }

                val conversationResult = repository.getConversation(conversationId)

                try {
                    val conversation = conversationResult.dataOrThrow
                    if (conversation.messages.isEmpty()) {
                        _uiState.update { it.copy(state = ScreenState.Empty, conversation =  conversation) }
                    } else {
                        _uiState.update { it.copy(state = ScreenState.Success, conversation =  conversation) }
                    }
                } catch (e: Exception) {
                    _uiState.update { it.copy(state = ScreenState.Error) }
                }
            }
        }
    }
}