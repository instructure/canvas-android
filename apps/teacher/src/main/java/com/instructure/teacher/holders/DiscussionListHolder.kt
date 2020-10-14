/*
 * Copyright (C) 2017 - present Instructure, Inc.
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
package com.instructure.teacher.holders

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.canvasapi2.utils.NumberHelper
import com.instructure.pandautils.utils.onClick
import com.instructure.pandautils.utils.onClickWithRequireNetwork
import com.instructure.pandautils.utils.setGone
import com.instructure.pandautils.utils.setVisible
import com.instructure.teacher.R
import kotlinx.android.synthetic.main.adapter_discussion.view.*
import java.util.*

class DiscussionListHolder(view: View) : RecyclerView.ViewHolder(view) {

    fun bind(context: Context, discussionTopicHeader: DiscussionTopicHeader, group: String?, courseColor: Int, isAnnouncement: Boolean,
             callback: (DiscussionTopicHeader) -> Unit,
             overflowCallback: (String?, DiscussionTopicHeader) -> Unit) = with(itemView) {
        discussionLayout.onClick { callback(discussionTopicHeader) }
        if(isAnnouncement) {
            discussionOverflow.setGone()
            readUnreadCounts.setGone()
        } else {
            discussionOverflow.setVisible()
            readUnreadCounts.setVisible()
            discussionOverflow.onClickWithRequireNetwork { overflowCallback(group, discussionTopicHeader) }
        }

        discussionTitle.text = discussionTopicHeader.title

        val isAssignmentType = discussionTopicHeader.assignment != null

        if(isAnnouncement) {
            discussionIcon.setIcon(R.drawable.vd_announcement, courseColor)
            discussionIcon.setPublishedStatus(true)
        } else {
            discussionIcon.setIcon(if (isAssignmentType) R.drawable.vd_assignment else R.drawable.vd_discussion, courseColor)
            discussionIcon.setPublishedStatus(discussionTopicHeader.published)
        }
        publishedBar.visibility = if (discussionTopicHeader.published) View.VISIBLE else View.INVISIBLE

        if(isAssignmentType) {
            dueDate.text = when {
                discussionTopicHeader.assignment?.dueAt == null -> getFormattedLastPost(context, discussionTopicHeader.lastReplyDate)
                (discussionTopicHeader.assignment?.allDates?.size ?: 0) > 1 -> context.getString(R.string.multiple_due_dates)
                else -> getFormattedDueDate(context, discussionTopicHeader.assignment!!.dueDate)
            }
            points.text = resources.getQuantityString(
                    R.plurals.quantityPointsAbbreviated,
                    discussionTopicHeader.assignment?.pointsPossible?.toInt() ?: -1,
                    NumberHelper.formatDecimal(discussionTopicHeader.assignment?.pointsPossible ?: -1.0, 1, true)
            )
            points.setVisible()
        } else {
            dueDate.text = if (isAnnouncement) {
                getFormattedPostedOn(context, discussionTopicHeader.postedDate)
            } else {
                getFormattedLastPost(context, discussionTopicHeader.lastReplyDate)
            }
            points.setGone()
        }

        dueDate.setVisible(dueDate.text.isNotBlank())

        val entryCount = discussionTopicHeader.discussionSubentryCount
        val unreadDisplayCount = if (discussionTopicHeader.unreadCount > 99) context.getString(R.string.max_count)
                                 else discussionTopicHeader.unreadCount.toString()

        statusIndicator.setVisible(discussionTopicHeader.status == DiscussionTopicHeader.ReadState.UNREAD)

        readUnreadCounts.text = context.getString(R.string.discussions_unread_replies_blank,
                                context.getString(R.string.discussions_replies, entryCount.toString()),
                                context.getString(R.string.utils_dotWithSpaces),
                                context.getString(R.string.discussions_unread, unreadDisplayCount))
    }

    private fun getFormattedLastPost(context: Context, date: Date?): String {
        if(date == null) return ""
        return context.getString(R.string.last_post).format(DateHelper.getFormattedDate(context, date))
    }

    private fun getFormattedPostedOn(context: Context, date: Date?): String {
        if(date == null) return ""
        return context.getString(R.string.utils_postedOnDate).format(DateHelper.getFormattedDate(context, date))
    }

    private fun getFormattedDueDate(context: Context, date: Date?): String {
        if(date == null) return ""
        val dueDate = DateHelper.dayMonthDateFormatUniversal.format(date)
        val dueTime = DateHelper.getPreferredTimeFormat(context).format(date)
        return context.getString(R.string.due_date_at_time).format(dueDate, dueTime)
    }

    companion object {
        const val HOLDER_RES_ID = R.layout.adapter_discussion
    }
}
