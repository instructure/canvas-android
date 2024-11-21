/*
 * Copyright (C) 2017 - present  Instructure, Inc.
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
 */
package com.instructure.teacher.presenters

import com.instructure.canvasapi2.apis.InboxApi
import com.instructure.canvasapi2.managers.InboxManager.archiveConversation
import com.instructure.canvasapi2.managers.InboxManager.deleteConversation
import com.instructure.canvasapi2.managers.InboxManager.deleteMessages
import com.instructure.canvasapi2.managers.InboxManager.getConversation
import com.instructure.canvasapi2.managers.InboxManager.markConversationAsUnread
import com.instructure.canvasapi2.managers.InboxManager.starConversation
import com.instructure.canvasapi2.models.BasicUser
import com.instructure.canvasapi2.models.Conversation
import com.instructure.canvasapi2.models.Message
import com.instructure.canvasapi2.utils.weave.*
import com.instructure.teacher.R
import com.instructure.teacher.viewinterface.MessageThreadView
import com.instructure.pandautils.blueprint.SyncPresenter
import kotlinx.coroutines.Job
import java.util.*

class MessageThreadPresenter(
    conversation: Conversation? = null,
    private var conversationId: Long
) : SyncPresenter<Message, MessageThreadView>(Message::class.java) {

    private lateinit var conversation: Conversation
    private var loadJob: Job? = null
    private val participantMap = HashMap<Long, BasicUser>()

    init {
        if (conversation != null) this.conversation = conversation
    }

    override fun loadData(forceNetwork: Boolean) {
        loadJob?.cancel()
        loadJob = weave {
            try {
                // Get workflow state prior to fetching; fetching will automatically mark the conversation as read
                val workflowState = getConversation()?.workflowState

                // Fetch the conversation if we are refreshing or if we don't have a conversation yet
                if (forceNetwork || !this@MessageThreadPresenter::conversation.isInitialized) {
                    conversation = awaitApi { getConversation(conversationId, forceNetwork, it) }
                }

                // Mark as read if it is unread and forceNetwork is true (it's always true for the initial call to loadData)
                if (workflowState == Conversation.WorkflowState.UNREAD && forceNetwork) {
                    conversation.workflowState = Conversation.WorkflowState.READ
                    viewCallback?.onConversationRead()
                }

                // Update conversation details in the view
                viewCallback?.setupConversationDetails()

                // Map out conversation participants
                participantMap += conversation.participants.associateBy { it.id }

                // Add/update messages
                data.addOrUpdate(conversation.messages.flatMap { it.forwardedMessages + it })
                viewCallback?.onRefreshFinished()
                viewCallback?.checkIfEmpty()
            } catch (e: Throwable) {
                viewCallback?.onConversationLoadFailed()
            }
        }
    }

    override fun refresh(forceNetwork: Boolean) {
        onRefreshStarted()
        clearData()
        loadData(forceNetwork)
    }

    fun getConversation(): Conversation? = if (this::conversation.isInitialized) conversation else null

    fun getParticipantById(id: Long): BasicUser? = participantMap[id]

    fun toggleArchived() {
        tryWeave {
            val archive = conversation.workflowState !== Conversation.WorkflowState.ARCHIVED
            awaitApi<Conversation> { archiveConversation(conversation.id, archive, it) }
            viewCallback?.showUserMessage(if (archive) R.string.message_archived else R.string.message_unarchived)
            if (viewCallback != null) {
                if (conversation.workflowState == Conversation.WorkflowState.ARCHIVED) {
                    conversation.workflowState = Conversation.WorkflowState.UNKNOWN
                } else {
                    conversation.workflowState = Conversation.WorkflowState.ARCHIVED
                }
                viewCallback?.onConversationArchived()
                viewCallback?.refreshConversationData()
            }
        } catch {
            viewCallback?.showUserMessage(R.string.error_conversation_generic)
        }
    }

    fun toggleStarred() {
        tryWeave {
            awaitApi<Conversation> {
                starConversation(conversation.id, !conversation.isStarred, conversation.workflowState!!, it)
            }
            conversation.isStarred = !conversation.isStarred
            viewCallback?.onConversationStarred()
            viewCallback?.refreshConversationData()
        } catch {
            viewCallback?.showUserMessage(R.string.error_conversation_generic)
        }
    }

    fun deleteConversation() {
        tryWeave {
            awaitApi<Conversation> { deleteConversation(conversation.id, it) }
            viewCallback?.onConversationDeleted()
        } catch {
            viewCallback?.showUserMessage(R.string.error_conversation_generic)
        }
    }

    fun deleteMessage(message: Message) {
        tryWeave {
            awaitApi<Conversation> { deleteMessages(conversation.id, listOf(message.id), it) }
            // If the top one was removed, we need to refresh the list so the reply button is on the top message
            val needsUpdate = data.indexOf(message) == 0
            data.remove(message)
            if (data.size() == 0) {
                // If there are no more messages, we have effectively deleted the conversation for this user
                viewCallback?.onConversationDeleted()
            } else {
                viewCallback?.showUserMessage(R.string.message_deleted)
                if (needsUpdate) viewCallback?.onMessageDeleted()
            }
        } catch {
            viewCallback?.showUserMessage(R.string.error_conversation_generic)
        }
    }

    fun markConversationUnread() {
        tryWeave {
            awaitApi<Void> { markConversationAsUnread(conversation.id, it) }
            conversation.workflowState = Conversation.WorkflowState.UNREAD
            viewCallback?.onConversationMarkedAsUnread()
        } catch {
            viewCallback?.showUserMessage(R.string.error_conversation_generic)
        }
    }

    fun getMessageChainForMessage(message: Message?): List<Message> {
        if (message == null) return emptyList()
        return (data.indexOf(message) downTo 0).map { data[it] }
    }

    val participants: ArrayList<BasicUser>
        get() = ArrayList(participantMap.values)

    override fun compare(m1: Message, m2: Message): Int {
        return m1.comparisonDate?.let { m2.comparisonDate?.compareTo(it) } ?: super.compare(m1, m2)
    }

    override fun areContentsTheSame(oldItem: Message, newItem: Message) = areItemsTheSame(oldItem, newItem)

    override fun areItemsTheSame(item1: Message, item2: Message): Boolean = item1.id == item2.id
}
