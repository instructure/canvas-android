/*
 * Copyright (C) 2019 - present  Instructure, Inc.
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

package com.instructure.teacher.holders

import android.content.Context
import android.content.res.ColorStateList
import android.text.SpannableStringBuilder
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.instructure.canvasapi2.models.BasicUser
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Conversation
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.utils.*
import com.instructure.interactions.router.Route
import com.instructure.pandautils.utils.ProfileUtils
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.setTextForVisibility
import com.instructure.pandautils.utils.setVisible
import com.instructure.teacher.R
import com.instructure.teacher.adapters.StudentContextFragment
import com.instructure.teacher.interfaces.AdapterToFragmentCallback
import com.instructure.teacher.router.RouteMatcher
import kotlinx.android.synthetic.main.adapter_inbox.view.*


class InboxViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    init {
        itemView.star.imageTintList = ColorStateList.valueOf(ThemePrefs.brandColor)
        itemView.message.maxLines = 1
    }

    fun bind(
        conversation: Conversation,
        callback: AdapterToFragmentCallback<Conversation>
    ) = with(itemView) {
        setOnClickListener { callback.onRowClicked(conversation, adapterPosition) }

        var onClick: ((user: BasicUser) -> Unit)? = null
        val canvasContext = CanvasContext.fromContextCode(conversation.contextCode)
        if (canvasContext is Course) {
            onClick = { (id) ->
                val bundle = StudentContextFragment.makeBundle(id, canvasContext.id, false)
                RouteMatcher.route(context, Route(StudentContextFragment::class.java, null, bundle))
            }
        }

        ProfileUtils.configureAvatarForConversation(avatar, conversation, onClick)

        userName.text = getConversationTitle(context, ApiPrefs.user!!.id, conversation)
        message.setTextForVisibility(conversation.lastMessagePreview)
        attachment.setVisible(conversation.hasAttachments() || conversation.hasMedia())
        subject.text = conversation.subject.validOrNull() ?: context.getString(R.string.no_subject)
        unreadMark.setVisible(conversation.workflowState == Conversation.WorkflowState.UNREAD)
        star.setVisible(conversation.isStarred)
        date.setTextForVisibility(
            getParsedDate(context, conversation.lastAuthoredMessageAt ?: conversation.lastMessageAt)
        )
    }

    private fun getParsedDate(context: Context, messageDate: String?): String? {
        val date = messageDate.toDate()
        return DateHelper.dateToDayMonthYearString(context, date)
    }

    private fun getConversationTitle(context: Context, myUserId: Long, conversation: Conversation): CharSequence {
        if (conversation.isMonologue(myUserId)) {
            return context.getString(R.string.monologue)
        }

        val users = conversation.participants
        return when (users.size) {
            0 -> ""
            1, 2 -> users.joinTo(SpannableStringBuilder()) { Pronouns.span(it.name, it.pronouns) }
            else -> {
                val user = users[0]
                Pronouns.resource(
                    context,
                    R.string.conversation_message_title,
                    user.pronouns,
                    Pronouns.span(user.name, user.pronouns),
                    (users.size - 1).toString()
                )
            }
        }
    }

    companion object {
        fun holderResId(): Int {
            return R.layout.adapter_inbox
        }
    }
}
