package com.instructure.pandautils.features.inbox.details

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.models.Attachment
import com.instructure.canvasapi2.models.Conversation
import com.instructure.canvasapi2.models.Message
import com.instructure.pandares.R
import com.instructure.pandautils.features.inbox.util.InboxMessageUiState
import com.instructure.pandautils.features.inbox.util.MessageAction
import com.instructure.pandautils.utils.toast
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
            is MessageAction.OpenAttachment -> { downloadFileToDevice(action.attachment) }
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
        val recipients = conversation.participants.filter { it.id != message.authorId }
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
                context.toast(context.getString(R.string.conversationDeleted))
                _events.send(InboxDetailsFragmentAction.CloseFragment)
            } else {
                context.toast(context.getString(R.string.conversationDeletedFailed))
            }
        }
    }

    private fun deleteMessage(conversationId: Long, message: Message) {
        viewModelScope.launch {
            val result = repository.deleteMessage(conversationId, listOf(message.id))
            val conversationResult = repository.getConversation(conversationId, true, true)
            if (result.isSuccess) {
                context.toast(context.getString(R.string.messageDeleted))

                val conversation = conversationResult.dataOrNull

                _uiState.update {
                    it.copy(
                        conversation = conversation,
                        messageStates = conversation?.messages?.map { getMessageViewState(conversation, it) } ?: emptyList()
                    )
                }
            } else {
                context.toast(context.getString(R.string.messageDeletedFailed))
            }
        }
    }

    private fun updateStarred(conversationId: Long, isStarred: Boolean) {
        viewModelScope.launch {
            val result = repository.updateStarred(conversationId, isStarred)
            if (result.isSuccess) {
                _uiState.update { it.copy(conversation = result.dataOrNull) }
            } else {
                context.toast(context.getString(R.string.conversationUpdateFailed))
            }
        }
    }

    private fun updateState(conversationId: Long, state: Conversation.WorkflowState) {
        viewModelScope.launch {
            val result = repository.updateState(conversationId, state)
            if (result.isSuccess) {
                _uiState.update { it.copy(conversation = result.dataOrNull) }
            } else {
                context.toast(context.getString(R.string.conversationUpdateFailed))
            }
        }
    }

    private fun downloadFileToDevice(attachment: Attachment) {
        val downloadManager = context.getSystemService(DownloadManager::class.java)

        val request = DownloadManager.Request(Uri.parse(attachment.url))
        request
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setTitle(attachment.filename)
            .setMimeType(attachment.contentType)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "${attachment.filename}")

        downloadManager.enqueue(request)
    }
}