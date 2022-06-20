/*
 * Copyright (C) 2018 - present Instructure, Inc.
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
 *
 */
package com.instructure.student.holders

import android.content.Context
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.instructure.canvasapi2.models.Conversation
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.canvasapi2.utils.Pronouns
import com.instructure.canvasapi2.utils.localized
import com.instructure.pandautils.utils.*
import com.instructure.student.R
import com.instructure.student.interfaces.AdapterToFragmentCallback
import kotlinx.android.synthetic.main.viewholder_inbox.view.*
import java.util.Date

class InboxViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(conversation: Conversation, currentUserId: Long, callback: AdapterToFragmentCallback<Conversation>) = with(itemView){
        ProfileUtils.configureAvatarForConversation(avatar, conversation)

        userName.setVisible().text = getConversationTitle(context, currentUserId, conversation)

        message.text = conversation.lastMessagePreview
        message.setVisible(message.text.isNotBlank())

        if (conversation.hasAttachments() || conversation.hasMedia()) {
            attachment.setImageDrawable(ColorUtils.colorIt(ContextCompat.getColor(context, R.color.textDark), attachment.drawable))
            attachment.setVisible()
        } else {
            attachment.setGone()
        }

        date.text = getParsedDate(context, conversation.lastAuthoredMessageSent ?: conversation.lastMessageSent)
        date.setVisible(date.text.isNotBlank())

        if (!conversation.subject.isNullOrBlank()) {
            subjectView.setVisible()
            subjectView.text = conversation.subject
            message.maxLines = 1
        } else {
            subjectView.setGone()
            message.maxLines = 2
        }

        if (conversation.workflowState == Conversation.WorkflowState.UNREAD) {
            unreadMark.setVisible()
            unreadMark.setImageDrawable(ColorUtils.colorIt(ThemePrefs.accentColor, unreadMark.drawable))
        } else {
            unreadMark.setGone()
        }

        if (conversation.isStarred) {
            star.setImageDrawable(ColorUtils.colorIt(ThemePrefs.brandColor, star.drawable))
            star.setVisible()
        } else {
            star.setGone()
        }

        onClick { callback.onRowClicked(conversation, adapterPosition, true) }
    }

    private fun getParsedDate(context: Context, date: Date?): String {
        return date?.let { DateHelper.dateToDayMonthYearString(context, it) } ?: ""
    }

    private fun getConversationTitle(context: Context, myUserId: Long, conversation: Conversation): CharSequence {
        if (conversation.isMonologue(myUserId)) return context.getString(R.string.monologue)

        val users = conversation.participants

        return when (users.size) {
            0 -> ""
            1, 2 -> users.joinTo(SpannableStringBuilder()) { Pronouns.span(it.name, it.pronouns) }
            else -> TextUtils.concat(
                Pronouns.span(users[0].name, users[0].pronouns),
                ", +${(users.size - 1).localized}"
            )
        }
    }

    companion object {
        const val HOLDER_RES_ID = R.layout.viewholder_inbox
    }

}
