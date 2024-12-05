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

package com.instructure.teacher.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.instructure.canvasapi2.models.Conversation
import com.instructure.canvasapi2.models.Message
import com.instructure.teacher.binders.MessageBinder
import com.instructure.teacher.databinding.AdapterMessageBinding
import com.instructure.teacher.holders.MessageHolder
import com.instructure.teacher.interfaces.MessageAdapterCallback
import com.instructure.teacher.presenters.MessageThreadPresenter
import com.instructure.teacher.viewinterface.MessageThreadView
import com.instructure.pandautils.blueprint.SyncRecyclerAdapter

open class MessageAdapter(
    context: Context,
    presenter: MessageThreadPresenter,
    private var mConversation: Conversation,
    protected var mCallback: MessageAdapterCallback
) : SyncRecyclerAdapter<Message, MessageHolder, MessageThreadView>(context, presenter) {

    fun updateConversation(conversation: Conversation?) {
        conversation?.let {
            if (conversation != mConversation) {
                mConversation = conversation
                notifyDataSetChanged()
            }
        }
    }

    override fun bindHolder(model: Message, holder: MessageHolder, position: Int) = MessageBinder.bind(
        model, mConversation, mCallback.getParticipantById(model.authorId), position, mCallback, holder
    )

    override fun createViewHolder(binding: ViewBinding, viewType: Int) = MessageHolder(binding as AdapterMessageBinding)

    override fun bindingInflater(viewType: Int): (LayoutInflater, ViewGroup, Boolean) -> ViewBinding = AdapterMessageBinding::inflate
}
