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
package com.instructure.student.holders

import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.text.format.DateFormat
import android.view.Gravity
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.instructure.canvasapi2.models.Attachment
import com.instructure.canvasapi2.models.BasicUser
import com.instructure.canvasapi2.models.Conversation
import com.instructure.canvasapi2.models.Message
import com.instructure.canvasapi2.utils.Pronouns
import com.instructure.canvasapi2.utils.asAttachment
import com.instructure.canvasapi2.utils.localized
import com.instructure.canvasapi2.utils.toDate
import com.instructure.pandautils.utils.*
import com.instructure.student.R
import com.instructure.student.databinding.ViewholderMessageBinding
import com.instructure.student.interfaces.MessageAdapterCallback
import com.instructure.student.view.ViewUtils
import java.text.SimpleDateFormat
import java.util.*

class InboxMessageHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(
        message: Message,
        conversation: Conversation,
        author: BasicUser?,
        position: Int,
        callback: MessageAdapterCallback
    ) = with(ViewholderMessageBinding.bind(itemView)) {

        // Set author info
        if (author != null) {
            authorName.text = getAuthorTitle(author.id, conversation, message)
            ProfileUtils.loadAvatarForUser(authorAvatar, author.name, author.avatarUrl)
            authorAvatar.setupAvatarA11y(author.name)
            authorAvatar.onClick { callback.onAvatarClicked(author) }
        } else {
            authorName.text = ""
            authorAvatar.clearAvatarA11y()
            authorAvatar.setImageDrawable(null)
            authorAvatar.setOnClickListener(null)
        }

        // Set attachments
        val attachments: MutableList<Attachment> = message.attachments.toMutableList()
        message.mediaComment?.let { attachments.add(it.asAttachment()) }
        attachmentContainer.setVisible(attachments.isNotEmpty()).setAttachments(attachments) { action, attachment ->
            callback.onAttachmentClicked(action, attachment)
        }

        // Set body
        messageBody.setText(message.body, TextView.BufferType.SPANNABLE)
        ViewUtils.linkifyTextView(messageBody)

        // Set message date/time
        val messageDate = message.createdAt.toDate()
        dateTime.text = dateFormat.format(messageDate)

        // Set up message options
        messageOptions.onClick { v ->
            // Set up popup menu
            val actions = MessageAdapterCallback.MessageClickAction.values().toMutableList()
            if (conversation.cannotReply) {
                actions.remove(MessageAdapterCallback.MessageClickAction.REPLY)
                actions.remove(MessageAdapterCallback.MessageClickAction.REPLY_ALL)
            }
            val popup = PopupMenu(v.context, v, Gravity.START)
            val menu = popup.menu
            actions.forEachIndexed { index, action ->
                menu.add(0, index, index, action.labelResId)
            }

            // Add click listener
            popup.setOnMenuItemClickListener { item ->
                callback.onMessageAction(actions[item.itemId], message)
                true
            }

            // Show
            popup.show()
        }
        if (!conversation.cannotReply) {
            reply.setTextColor(ThemePrefs.textButtonColor)
            reply.setVisible(position == 0)
            reply.onClick {
                callback.onMessageAction(
                    MessageAdapterCallback.MessageClickAction.REPLY,
                    message
                )
            }
        } else {
            reply.setVisible(false)
        }
    }

    private val dateFormat = SimpleDateFormat(
        DateFormat.getBestDateTimePattern(Locale.getDefault(), "MMMdyyyyjmm"),
        Locale.getDefault()
    )

    private fun getAuthorTitle(myUserId: Long, conversation: Conversation, message: Message): CharSequence {
        // We don't want to filter by the messages participating user ids because they don't always contain the correct information
        val users = conversation.participants

        // We want the author first
        users.find { it.id == myUserId }?.let {
            users.remove(it)
            users.add(0, it)
        }
        return when (users.size) {
            0 -> ""
            1, 2 -> users.joinTo(SpannableStringBuilder()) { Pronouns.span(it.name, it.pronouns) }
            else -> TextUtils.concat(
                Pronouns.span(users[0].name, users[0].pronouns),
                ", +${(message.participatingUserIds.size - 1).localized}"
            )
        }
    }

    companion object {
        const val HOLDER_RES_ID: Int = R.layout.viewholder_message
    }
}
