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
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.canvasapi2.utils.localized
import com.instructure.pandautils.utils.onClick
import com.instructure.pandautils.utils.setGone
import com.instructure.pandautils.utils.setInvisible
import com.instructure.pandautils.utils.setVisible
import com.instructure.student.R
import com.instructure.student.adapter.DiscussionListRecyclerAdapter
import kotlinx.android.synthetic.main.viewholder_discussion.view.*
import java.util.*

class DiscussionListHolder(view: View) : RecyclerView.ViewHolder(view) {

    fun bind(context: Context, discussionTopicHeader: DiscussionTopicHeader, courseColor: Int, isDiscussion: Boolean,
             callback: DiscussionListRecyclerAdapter.AdapterToDiscussionsCallback) = with(itemView) {

        discussionLayout.onClick { callback.onRowClicked(discussionTopicHeader, adapterPosition, true) }

        /*
        TODO - Blocked by COMMS-868
        if(isDiscussion) {
            // We only show the overflow if they are the author, have edit permissions and the topic has no replies
            if (discussionTopicHeader.author.id == ApiPrefs.user?.id) {
                discussionOverflow.setVisible()
                discussionOverflow.onClickWithRequireNetwork { callback.discussionOverflow(group, discussionTopicHeader) }
            } else {
                discussionOverflow.setGone()
            }
            // If its a discussion, we always show the read counts
            readUnreadCounts.setVisible()
        } else {
            discussionOverflow.setGone()
            readUnreadCounts.setGone()
        }
        */

        discussionOverflow.setGone()

        discussionTitle.text = discussionTopicHeader.title

        val isAssignmentType = discussionTopicHeader.assignment != null

        if(isDiscussion) {
            discussionIcon.setIcon(if (isAssignmentType) R.drawable.vd_assignment
                    else R.drawable.vd_discussion, courseColor)
            readUnreadCounts.setVisible()
        } else {
            discussionIcon.setIcon(R.drawable.vd_announcement, courseColor)
            readUnreadCounts.setGone()
        }

        if(discussionTopicHeader.lockedForUser) {
            discussionIcon.setNestedIcon(R.drawable.vd_lock, ContextCompat.getColor(context, R.color.lockedDiscussionColor))
            discussionIcon.setNestedIconContentDescription(context.getString(R.string.locked))
        } else {
            discussionIcon.hideNestedIcon()
        }

        dueDate.text = when {
            isAssignmentType -> {
                if (discussionTopicHeader.assignment!!.dueDate == null) getFormattedLastPost( context, discussionTopicHeader.lastReplyDate)
                else getFormattedDueDate(context, discussionTopicHeader.assignment!!.dueDate)
            }
            isDiscussion -> getFormattedLastPost(context, discussionTopicHeader.lastReplyDate)
            else -> getFormattedPostedOn(context, discussionTopicHeader.postedDate)
        }

        dueDate.setVisible(dueDate.text.isNotBlank())

        val entryCount = discussionTopicHeader.discussionSubentryCount
        val unreadDisplayCount = if (discussionTopicHeader.unreadCount > 99) context.getString(R.string.max_count)
        else discussionTopicHeader.unreadCount.localized

        if(discussionTopicHeader.unreadCount != 0) {
            statusIndicator.setVisible()
        } else {
            statusIndicator.setInvisible()
        }

        val entryCountString = context.resources.getQuantityString(R.plurals.utils_discussionsReplies, entryCount, entryCount.localized)
        val unreadCountString = context.resources.getQuantityString(R.plurals.utils_discussionsUnread, discussionTopicHeader.unreadCount , unreadDisplayCount)

        readUnreadCounts.text = context.getString(R.string.utils_discussionsUnreadRepliesBlank,
                entryCountString, context.getString(R.string.utils_dotWithSpaces), unreadCountString)
    }

    private fun getFormattedLastPost(context: Context, date: Date?): String {
        if(date == null) return ""
        return context.getString(R.string.utils_lastPost).format(DateHelper.getFormattedDate(context, date))
    }

    private fun getFormattedPostedOn(context: Context, date: Date?): String {
        if(date == null) return ""
        return context.getString(R.string.utils_postedOnDate).format(DateHelper.getFormattedDate(context, date))
    }

    private fun getFormattedDueDate(context: Context, date: Date?): String {
        if(date == null) return ""
        val dueDate = DateHelper.dayMonthDateFormatUniversal.format(date)
        val dueTime = DateHelper.getPreferredTimeFormat(context).format(date)
        return context.getString(R.string.utils_dueDateAtTime).format(dueDate, dueTime)
    }

    companion object {
        const val HOLDER_RES_ID = R.layout.viewholder_discussion
    }
}
