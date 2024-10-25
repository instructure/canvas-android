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

import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.text.format.DateFormat
import android.view.Gravity
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import com.instructure.canvasapi2.models.Attachment
import com.instructure.canvasapi2.models.BasicUser
import com.instructure.canvasapi2.models.Conversation
import com.instructure.canvasapi2.models.Message
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.canvasapi2.utils.Pronouns
import com.instructure.canvasapi2.utils.asAttachment
import com.instructure.canvasapi2.utils.toDate
import com.instructure.pandautils.utils.ProfileUtils
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.setVisible
import com.instructure.pandautils.utils.setupAvatarA11y
import com.instructure.teacher.holders.MessageHolder
import com.instructure.teacher.interfaces.MessageAdapterCallback
import com.instructure.teacher.utils.linkifyTextView
import java.text.SimpleDateFormat
import java.util.Locale

object MessageBinder {
    fun bind(
        message: Message,
        conversation: Conversation,
        author: BasicUser?,
        position: Int,
        callback: MessageAdapterCallback,
        holder: MessageHolder
    ): Unit = with(holder.binding) {
        // Set author info
        with(authorName) {
            if (author != null) {
                text = getAuthorTitle(author.id, conversation, message)
                ProfileUtils.loadAvatarForUser(authorAvatar, author.name, author.avatarUrl)
                setupAvatarA11y(author.name)
                setOnClickListener { callback.onAvatarClicked(author) }
            } else {
                text = ""
                with(authorAvatar) {
                    setImageDrawable(null)
                    setOnClickListener(null)
                }
            }
        }

        // Set attachments
        with(attachmentContainer) {
            val attachments: MutableList<Attachment> = message.attachments.toMutableList()
            message.mediaComment?.let { attachments.add(it.asAttachment()) }
            setVisible(attachments.isNotEmpty()).setAttachments(attachments) { action, attachment ->
                callback.onAttachmentClicked(action, attachment)
            }
        }


        // Set body
        messageBody.setText(message.body, TextView.BufferType.SPANNABLE)
        messageBody.linkifyTextView()

        // Set message date/time
        val messageDate = message.createdAt.toDate()
        dateTime.text = dateFormat.format(messageDate)

        // Set up message options
        messageOptions.setOnClickListener { v ->
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

        with(reply) {
            if (!conversation.cannotReply) {
                setTextColor(ThemePrefs.textButtonColor)
                setVisible(position == 0)
                setOnClickListener {
                    callback.onMessageAction(
                        MessageAdapterCallback.MessageClickAction.REPLY,
                        message
                    )
                }
            } else {
                setVisible(false)
            }
        }
    }

    private var dateFormat = SimpleDateFormat(if (DateFormat.is24HourFormat(ContextKeeper.appContext)) "MMM d, yyyy, HH:mm" else "MMM d, yyyy, h:mm a",
            Locale.getDefault())

    private fun getAuthorTitle(myUserId: Long, conversation: Conversation, message: Message): CharSequence {

        // We don't want to filter by the messages participating user ids because they don't always contain the correct information
        val users = conversation.participants

        val author = users.firstOrNull { it.id == myUserId }

        // We want the author first
        if (author != null) {
            users.remove(author)
            users.add(0, author)
        }

        return when (users.size) {
            0 -> ""
            1, 2 -> users.joinTo(SpannableStringBuilder()) { Pronouns.span(it.name, it.pronouns) }
            else -> TextUtils.concat(
                Pronouns.span(users[0].name, users[0].pronouns),
                ", +${message.participatingUserIds.size - 1}"
            )
        }
    }
}
