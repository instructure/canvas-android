package com.instructure.pandautils.features.inbox.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.models.Conversation
import com.instructure.canvasapi2.models.Message
import com.instructure.pandautils.features.inbox.util.InboxMessageUiState
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
                getConversation(true)
            }
        }
    }

    private fun getConversation(forceRefresh: Boolean = false) {
        conversationId?.let {
            viewModelScope.launch {
                _uiState.update { it.copy(state = ScreenState.Loading) }

                val conversationResult = repository.getConversation(conversationId, forceRefresh)

                try {
                    val conversation = conversationResult.dataOrThrow
                    if (conversation.messages.isEmpty()) {
                        _uiState.update { it.copy(state = ScreenState.Empty, conversation =  conversation) }
                    } else {
                        _uiState.update { uiState -> uiState.copy(
                            state = ScreenState.Success,
                            conversation =  conversation,
                            messageStates = conversation.messages.map { getMessageViewState(conversation, it) }
                        ) }
                    }
                } catch (e: Exception) {
                    _uiState.update { it.copy(state = ScreenState.Error) }
                }
            }
        }
    }

    private fun getMessageViewState(conversation: Conversation, message: Message): InboxMessageUiState {
        val author = conversation.participants.find { it.id == message.authorId }
        val recipients = conversation.participants.filter { it.id != message.authorId }
        return InboxMessageUiState(
            message = message,
            author = author,
            recipients = recipients,
            enabledActions = true,
        )
    }
}