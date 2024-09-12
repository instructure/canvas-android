package com.instructure.pandautils.features.inbox.details

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.models.Conversation
import com.instructure.canvasapi2.models.Message
import com.instructure.pandares.R
import com.instructure.pandautils.features.inbox.utils.InboxMessageUiState
import com.instructure.pandautils.features.inbox.utils.MessageAction
import com.instructure.pandautils.utils.FileDownloader
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InboxDetailsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle,
    private val repository: InboxDetailsRepository,
    private val fileDownloader: FileDownloader
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

    fun messageActionHandler(action: MessageAction) {
        when (action) {
            is MessageAction.Reply -> handleAction(InboxDetailsAction.Reply)
            is MessageAction.ReplyAll -> handleAction(InboxDetailsAction.ReplyAll)
            is MessageAction.Forward -> handleAction(InboxDetailsAction.Forward)
            is MessageAction.DeleteMessage -> handleAction(InboxDetailsAction.DeleteMessage(conversationId ?: 0, action.message))
            is MessageAction.OpenAttachment -> { fileDownloader.downloadFileToDevice(action.attachment) }
            is MessageAction.UrlSelected -> {
                viewModelScope.launch {
                    _events.send(InboxDetailsFragmentAction.UrlSelected(action.url))
                }
            }
        }
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

            is InboxDetailsAction.DeleteConversation -> _uiState.update { it.copy(alertDialogState = AlertDialogState(
                showDialog = true,
                title = context.getString(R.string.deleteConversation),
                message = context.getString(R.string.confirmDeleteConversation),
                positiveButton = context.getString(R.string.delete),
                negativeButton = context.getString(R.string.cancel),
                onPositiveButtonClick = {
                    deleteConversation(action.conversationId)
                    _uiState.update { it.copy(alertDialogState = AlertDialogState()) }
                },
                onNegativeButtonClick = {
                    _uiState.update { it.copy(alertDialogState = AlertDialogState()) }
                }
            )) }
            is InboxDetailsAction.DeleteMessage -> _uiState.update { it.copy(alertDialogState = AlertDialogState(
                showDialog = true,
                title = context.getString(R.string.deleteMessage),
                message = context.getString(R.string.confirmDeleteMessage),
                positiveButton = context.getString(R.string.delete),
                negativeButton = context.getString(R.string.cancel),
                onPositiveButtonClick = {
                    deleteMessage(action.conversationId, action.message)
                    _uiState.update { it.copy(alertDialogState = AlertDialogState()) }
                },
                onNegativeButtonClick = {
                    _uiState.update { it.copy(alertDialogState = AlertDialogState()) }
                }
            )) }
            is InboxDetailsAction.Forward -> TODO()
            is InboxDetailsAction.Reply -> TODO()
            is InboxDetailsAction.ReplyAll -> TODO()
            is InboxDetailsAction.UpdateState -> updateState(action.conversationId, action.workflowState)
            is InboxDetailsAction.UpdateStarred -> updateStarred(action.conversationId, action.newStarValue)
        }
    }

    private fun getConversation(forceRefresh: Boolean = false) {
        conversationId?.let {
            viewModelScope.launch {
                _uiState.update { it.copy(state = ScreenState.Loading) }

                val conversationResult = repository.getConversation(conversationId, true, forceRefresh)

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
        val recipients = conversation.participants.filter { message.participatingUserIds.filter { it != message.authorId }.contains(it.id) }
        return InboxMessageUiState(
            message = message,
            author = author,
            recipients = recipients,
            enabledActions = true,
        )
    }

    private fun deleteConversation(conversationId: Long) {
        viewModelScope.launch {
            val result = repository.deleteConversation(conversationId)
            if (result.isSuccess) {
                _events.send(InboxDetailsFragmentAction.ShowScreenResult(context.getString(R.string.conversationDeleted)))
                _events.send(InboxDetailsFragmentAction.CloseFragment)
            } else {
                _events.send(InboxDetailsFragmentAction.ShowScreenResult(context.getString(R.string.conversationDeletedFailed)))
            }

            _events.send(InboxDetailsFragmentAction.UpdateParentFragment)
        }
    }

    private fun deleteMessage(conversationId: Long, message: Message) {
        viewModelScope.launch {
            val result = repository.deleteMessage(conversationId, listOf(message.id))
            val conversationResult = repository.getConversation(conversationId, true, true)
            if (result.isSuccess) {
                _events.send(InboxDetailsFragmentAction.ShowScreenResult(context.getString(R.string.messageDeleted)))

                val conversation = conversationResult.dataOrNull

                _uiState.update {
                    it.copy(
                        conversation = conversation,
                        messageStates = conversation?.messages?.map { getMessageViewState(conversation, it) } ?: emptyList()
                    )
                }
            } else {
                _events.send(InboxDetailsFragmentAction.ShowScreenResult(context.getString(R.string.messageDeletedFailed)))
            }

            _events.send(InboxDetailsFragmentAction.UpdateParentFragment)
        }
    }

    private fun updateStarred(conversationId: Long, isStarred: Boolean) {
        viewModelScope.launch {
            val result = repository.updateStarred(conversationId, isStarred)
            if (result.isSuccess) {
                _uiState.update { it.copy(conversation = result.dataOrNull) }
            } else {
                _events.send(InboxDetailsFragmentAction.ShowScreenResult(context.getString(R.string.conversationUpdateFailed)))
            }

            _events.send(InboxDetailsFragmentAction.UpdateParentFragment)
        }
    }

    private fun updateState(conversationId: Long, state: Conversation.WorkflowState) {
        viewModelScope.launch {
            val result = repository.updateState(conversationId, state)
            if (result.isSuccess) {
                _uiState.update { it.copy(conversation = result.dataOrNull) }
            } else {
                _events.send(InboxDetailsFragmentAction.ShowScreenResult(context.getString(R.string.conversationUpdateFailed)))
            }

            _events.send(InboxDetailsFragmentAction.UpdateParentFragment)
        }
    }
}