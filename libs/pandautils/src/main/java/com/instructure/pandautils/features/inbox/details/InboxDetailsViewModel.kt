/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
package com.instructure.pandautils.features.inbox.details

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.models.Conversation
import com.instructure.canvasapi2.models.Message
import com.instructure.pandares.R
import com.instructure.pandautils.features.inbox.utils.InboxComposeOptions
import com.instructure.pandautils.features.inbox.utils.InboxMessageUiState
import com.instructure.pandautils.features.inbox.utils.MessageAction
import com.instructure.pandautils.utils.ScreenState
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
    private val behavior: InboxDetailsBehavior,
    private val repository: InboxDetailsRepository,
): ViewModel() {

    val conversationId: Long? = savedStateHandle.get<Long>(InboxDetailsFragment.CONVERSATION_ID)
    val unread: Boolean = savedStateHandle.get<Boolean>(InboxDetailsFragment.UNREAD) ?: false

    private val _uiState = MutableStateFlow(InboxDetailsUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = Channel<InboxDetailsFragmentAction>()
    val events = _events.receiveAsFlow()

    init {
        _uiState.update { it.copy(
            conversationId = conversationId,
            showBackButton = behavior.getShowBackButton(context)
        ) }
        getConversation()
    }

    fun messageActionHandler(action: MessageAction) {
        when (action) {
            is MessageAction.Reply -> handleAction(InboxDetailsAction.Reply(action.message))
            is MessageAction.ReplyAll -> handleAction(InboxDetailsAction.ReplyAll(action.message))
            is MessageAction.Forward -> handleAction(InboxDetailsAction.Forward(action.message))
            is MessageAction.DeleteMessage -> handleAction(InboxDetailsAction.DeleteMessage(conversationId ?: 0, action.message))
            is MessageAction.OpenAttachment -> {
                viewModelScope.launch {
                    _events.send(InboxDetailsFragmentAction.OpenAttachment(action.attachment))
                }
            }
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

            is InboxDetailsAction.DeleteConversation -> _uiState.update { it.copy(confirmationDialogState = ConfirmationDialogState(
                showDialog = true,
                title = context.getString(R.string.deleteConversation),
                message = context.getString(R.string.confirmDeleteConversation),
                positiveButton = context.getString(R.string.delete),
                negativeButton = context.getString(R.string.cancel),
                onPositiveButtonClick = {
                    deleteConversation(action.conversationId)
                    _uiState.update { it.copy(confirmationDialogState = ConfirmationDialogState()) }
                },
                onNegativeButtonClick = {
                    _uiState.update { it.copy(confirmationDialogState = ConfirmationDialogState()) }
                }
            )) }
            is InboxDetailsAction.DeleteMessage -> _uiState.update { it.copy(confirmationDialogState = ConfirmationDialogState(
                showDialog = true,
                title = context.getString(R.string.deleteMessage),
                message = context.getString(R.string.confirmDeleteMessage),
                positiveButton = context.getString(R.string.delete),
                negativeButton = context.getString(R.string.cancel),
                onPositiveButtonClick = {
                    deleteMessage(action.conversationId, action.message)
                    _uiState.update { it.copy(confirmationDialogState = ConfirmationDialogState()) }
                },
                onNegativeButtonClick = {
                    _uiState.update { it.copy(confirmationDialogState = ConfirmationDialogState()) }
                }
            )) }
            is InboxDetailsAction.Forward -> {
                viewModelScope.launch {
                    uiState.value.conversation?.let {
                        _events.send(InboxDetailsFragmentAction.NavigateToCompose(InboxComposeOptions.buildForward(context, it, action.message)))
                    }
                }
            }
            is InboxDetailsAction.Reply -> {
                viewModelScope.launch {
                    uiState.value.conversation?.let {
                        _events.send(InboxDetailsFragmentAction.NavigateToCompose(InboxComposeOptions.buildReply(context, it, action.message)))
                    }
                }
            }
            is InboxDetailsAction.ReplyAll -> {
                viewModelScope.launch {
                    uiState.value.conversation?.let {
                        _events.send(InboxDetailsFragmentAction.NavigateToCompose(InboxComposeOptions.buildReplyAll(context, it, action.message)))
                    }
                }
            }
            is InboxDetailsAction.UpdateState -> updateState(action.conversationId, action.workflowState)
            is InboxDetailsAction.UpdateStarred -> updateStarred(action.conversationId, action.newStarValue)
        }
    }

    private fun refreshParentFragment() {
        viewModelScope.launch { _events.send(InboxDetailsFragmentAction.UpdateParentFragment) }
    }

    fun refreshConversation() {
        getConversation(true)
    }

    private fun getConversation(forceRefresh: Boolean = false) {
        conversationId?.let {
            viewModelScope.launch {
                _uiState.update { it.copy(state = ScreenState.Loading) }

                val conversationResult = repository.getConversation(conversationId, true, forceRefresh)

                try {
                    val conversation = conversationResult.dataOrThrow
                    if (unread) {
                        refreshParentFragment()
                    }
                    if (conversation.messages.isEmpty()) {
                        _uiState.update { it.copy(state = ScreenState.Empty, conversation =  conversation) }
                    } else {
                        _uiState.update { uiState -> uiState.copy(
                            state = ScreenState.Content,
                            conversation =  conversation,
                            messageStates = conversation.messages.map { getMessageViewState(conversation, it) },
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
            cannotReply = conversation.cannotReply,
        )
    }

    private fun deleteConversation(conversationId: Long) {
        viewModelScope.launch {
            val result = repository.deleteConversation(conversationId)
            if (result.isSuccess) {
                refreshParentFragment()
                _events.send(InboxDetailsFragmentAction.ShowScreenResult(context.getString(R.string.conversationDeleted)))
                _events.send(InboxDetailsFragmentAction.CloseFragment)

            } else {
                _events.send(InboxDetailsFragmentAction.ShowScreenResult(context.getString(R.string.conversationDeletedFailed)))
            }
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

                refreshParentFragment()
            } else {
                _events.send(InboxDetailsFragmentAction.ShowScreenResult(context.getString(R.string.messageDeletedFailed)))
            }
        }
    }

    private fun updateStarred(conversationId: Long, isStarred: Boolean) {
        viewModelScope.launch {
            val result = repository.updateStarred(conversationId, isStarred)
            if (result.isSuccess) {
                _uiState.update { it.copy(conversation = it.conversation?.copy(isStarred = result.dataOrNull?.isStarred ?: false)) }

                refreshParentFragment()
            } else {
                _events.send(InboxDetailsFragmentAction.ShowScreenResult(context.getString(R.string.conversationUpdateFailed)))
            }
        }
    }

    private fun updateState(conversationId: Long, state: Conversation.WorkflowState) {
        viewModelScope.launch {
            val result = repository.updateState(conversationId, state)
            if (result.isSuccess) {
                _uiState.update { it.copy(conversation = it.conversation?.copy(workflowState = result.dataOrNull?.workflowState)) }

                refreshParentFragment()
            } else {
                _events.send(InboxDetailsFragmentAction.ShowScreenResult(context.getString(R.string.conversationUpdateFailed)))
            }
        }
    }
}