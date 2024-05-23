/*
 * Copyright (C) 2023 - present Instructure, Inc.
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
package com.instructure.pandautils.features.inbox.list

import android.content.Context
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.view.View
import com.instructure.canvasapi2.models.Conversation
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.canvasapi2.utils.Pronouns
import com.instructure.canvasapi2.utils.localized
import com.instructure.pandautils.R
import com.instructure.pandautils.features.inbox.list.itemviewmodels.InboxEntryItemViewModel

class InboxEntryItemCreator(private val context: Context, private val apiPrefs: ApiPrefs) {

    fun createInboxEntryItem(
        conversation: Conversation,
        openConversationCallback: (Boolean, Boolean) -> Unit,
        selectionModeCallback: (View, Boolean) -> Unit,
        avatarClickedCallback: (Boolean) -> Unit
    ): InboxEntryItemViewModel {
        val viewData = InboxEntryViewData(
            conversation.id,
            createAvatarData(conversation),
            createMessageTitle(conversation),
            conversation.subject.takeIf { it?.isNotBlank() == true } ?: context.getString(R.string.noSubject),
            conversation.lastMessagePreview ?: "",
            createDateText(conversation),
            conversation.workflowState == Conversation.WorkflowState.UNREAD,
            conversation.isStarred,
            conversation.hasAttachments() || conversation.hasMedia()
        )

        return InboxEntryItemViewModel(viewData, openConversationCallback, selectionModeCallback, avatarClickedCallback)
    }

    private fun createAvatarData(conversation: Conversation): AvatarViewData {
        return AvatarViewData(
            conversation.avatarUrl ?: "",
            conversation.participants.firstOrNull()?.name ?: "",
            conversation.participants.size > 2
        )
    }

    private fun createMessageTitle(conversation: Conversation): String {
        if (conversation.isMonologue(apiPrefs.user?.id ?: 0)) return context.getString(R.string.monologue)

        val users = conversation.participants

        return when (users.size) {
            0 -> ""
            1, 2 -> users.joinTo(SpannableStringBuilder()) { Pronouns.span(it.name, it.pronouns) }.toString()
            else -> TextUtils.concat(
                Pronouns.span(users[0].name, users[0].pronouns),
                ", +${(users.size - 1).localized}"
            ).toString()
        }
    }

    private fun createDateText(conversation: Conversation): String {
        val date = conversation.lastAuthoredMessageSent ?: conversation.lastMessageSent
        return DateHelper.dateToDayMonthYearString(context, date) ?: ""
    }
}