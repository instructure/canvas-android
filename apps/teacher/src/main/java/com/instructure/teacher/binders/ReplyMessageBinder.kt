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

package com.instructure.teacher.binders

import android.content.Context
import com.instructure.canvasapi2.models.Conversation
import com.instructure.canvasapi2.models.Message
import com.instructure.pandautils.utils.onClick
import com.instructure.pandautils.utils.setGone
import com.instructure.teacher.R
import com.instructure.teacher.holders.MessageHolder
import com.instructure.teacher.interfaces.MessageAdapterCallback
import kotlinx.android.synthetic.main.adapter_message.view.*


class ReplyMessageBinder : BaseBinder() {
    companion object {

        fun bind(message: Message, conversation: Conversation, holder: MessageHolder, position: Int, callback: MessageAdapterCallback) {

            MessageBinder.bind(message, conversation, callback.getParticipantById(message.authorId), holder, position, callback)

            // Hide attachments
            holder.itemView.attachmentContainer.setGone()

            // Set up remove button
            holder.itemView.messageOptions.setImageResource(R.drawable.vd_utils_close)
            holder.itemView.messageOptions.onClick { callback.onMessageAction(MessageAdapterCallback.MessageClickAction.DELETE, message) }
        }
    }
}
