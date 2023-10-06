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
package com.instructure.student.features.discussion.list.adapter

import android.animation.AnimatorInflater
import android.animation.ObjectAnimator
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.instructure.pandautils.utils.setVisible
import com.instructure.student.R
import com.instructure.student.databinding.ViewholderDiscussionGroupHeaderBinding

class DiscussionExpandableViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    var isExpanded = true

    fun bind(expanded: Boolean, isDiscussion: Boolean, group: String, callback: (String) -> Unit) = with(ViewholderDiscussionGroupHeaderBinding.bind(itemView)) {
        root.setVisible(isDiscussion)

        isExpanded = expanded

        groupName.text = when (group) {
            DiscussionListRecyclerAdapter.PINNED -> root.context.getString(R.string.utils_pinnedDiscussions)
            DiscussionListRecyclerAdapter.UNPINNED -> root.context.getString(R.string.utils_discussionUnpinned)
            DiscussionListRecyclerAdapter.CLOSED_FOR_COMMENTS -> root.context.getString(R.string.closed_discussion)
            else -> ""
        }

        collapseIcon.rotation = if (expanded) 180f else 0f

        root.setOnClickListener {
            val animRes = if (isExpanded) R.animator.rotation_from_neg90_to_0 else R.animator.rotation_from_0_to_neg90
            isExpanded = !isExpanded
            val flipAnimator = AnimatorInflater.loadAnimator(root.context, animRes) as ObjectAnimator
            flipAnimator.target = collapseIcon
            flipAnimator.duration = 200
            flipAnimator.start()
            callback(group)
        }
    }

    companion object {
        const val HOLDER_RES_ID: Int = R.layout.viewholder_discussion_group_header
    }
}
