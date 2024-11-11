/*
 * Copyright (C) 2018 - present  Instructure, Inc.
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
package com.instructure.student.adapter

import android.content.Context
import android.view.View
import com.instructure.student.holders.InboxMessageHolder
import com.instructure.student.interfaces.MessageAdapterCallback
import com.instructure.canvasapi2.managers.InboxManager
import com.instructure.canvasapi2.models.BasicUser
import com.instructure.canvasapi2.models.Conversation
import com.instructure.canvasapi2.models.Message
import com.instructure.canvasapi2.utils.weave.WeaveJob
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryWeave
import com.instructure.pandautils.utils.ConversationUpdatedEvent
import org.greenrobot.eventbus.EventBus

class InboxConversationAdapter(
    context: Context,
    private val conversation: Conversation,
    private val callback: MessageAdapterCallback
) : BaseListRecyclerAdapter<Message, InboxMessageHolder>(context, Message::class.java) {

    private var apiCall: WeaveJob? = null

    var participants = mapOf<Long, BasicUser>()
        private set

    val topMessage: Message get() = getItemAtPosition(0)!!

    val forwardMessage: Message get() = getItemAtPosition(size() - 1)!!

    init {
        itemCallback = object : ItemComparableCallback<Message>() {
            override fun areContentsTheSame(oldItem: Message, newItem: Message) = areItemsTheSame(oldItem, newItem)
            override fun areItemsTheSame(item1: Message, item2: Message): Boolean = item1.id == item2.id
            override fun compare(o1: Message, o2: Message): Int {
                if (o1.comparisonDate != null && o2.comparisonDate != null) {
                    return o2.comparisonDate!!.compareTo(o1.comparisonDate!!)
                }
                return super.compare(o1, o2)
            }
        }
        loadData()
    }

    override fun loadData() {
        apiCall?.cancel()
        apiCall = tryWeave {
            val freshConversation = awaitApi<Conversation> {
                // Specify forceNetwork = true, otherwise the conversation won't get marked as read
                InboxManager.getConversation(conversation.id, true, it)
            }
            participants = freshConversation.participants.associateBy { it.id }
            addAll(freshConversation.messages.flatMap { it.forwardedMessages + it })
            callback.onRefreshFinished()

            // Notify other components when a conversation is marked as read
            if (conversation.workflowState == Conversation.WorkflowState.UNREAD) {
                conversation.workflowState = Conversation.WorkflowState.READ
                EventBus.getDefault().postSticky(ConversationUpdatedEvent(freshConversation))
            }
        } catch {
            it.printStackTrace()
        }
    }

    override fun resetData() {
        apiCall?.cancel()
        super.resetData()
    }

    override fun bindHolder(message: Message, holder: InboxMessageHolder, position: Int) {
        holder.bind(message, conversation, callback.getParticipantById(message.authorId), position, callback)
    }

    override fun createViewHolder(v: View, viewType: Int): InboxMessageHolder {
        return InboxMessageHolder(v)
    }

    override fun itemLayoutResId(viewType: Int) = InboxMessageHolder.HOLDER_RES_ID

    override fun cancel() {
        apiCall?.cancel()
    }

    fun getMessageChainIdsForMessage(message: Message): LongArray {
        return (indexOf(message) downTo 0).map { getItemAtPosition(it)!!.id }.toLongArray()
    }

}
