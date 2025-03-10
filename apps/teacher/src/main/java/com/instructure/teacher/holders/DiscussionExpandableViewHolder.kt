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
package com.instructure.teacher.holders

import android.animation.AnimatorInflater
import android.animation.ObjectAnimator
import android.content.Context
import android.view.accessibility.AccessibilityManager
import androidx.recyclerview.widget.RecyclerView
import com.instructure.pandautils.utils.hasSpokenFeedback
import com.instructure.teacher.R
import com.instructure.teacher.databinding.ViewholderHeaderExpandableBinding
import com.instructure.teacher.presenters.DiscussionListPresenter

class DiscussionExpandableViewHolder(private val binding: ViewholderHeaderExpandableBinding) : RecyclerView.ViewHolder(binding.root) {

    var mIsExpanded = true

    fun bind(
        isExpanded: Boolean,
        holder: DiscussionExpandableViewHolder,
        group: String,
        callback: (String) -> Unit
    ) = with(binding) {

        mIsExpanded = isExpanded

        var title = ""

        val context = binding.root.context

        when (group) {
            DiscussionListPresenter.PINNED -> title = context.getString(R.string.discussions_pinned)
            DiscussionListPresenter.UNPINNED -> title = context.getString(R.string.discussions_unpinned)
            DiscussionListPresenter.CLOSED_FOR_COMMENTS -> title = context.getString(R.string.discussions_closed)
        }

        groupName.text = title

        val a11yManager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        // Expand/collapse is disabled when TalkBack is enabled, so we prevent TalkBack announcing that functionality by not adding a click listener
        if (!a11yManager.hasSpokenFeedback) {
            holder.itemView.setOnClickListener {
                val animationType =
                    if (mIsExpanded) R.animator.rotation_from_0_to_neg90 else R.animator.rotation_from_neg90_to_0
                mIsExpanded = !mIsExpanded
                val flipAnimator =
                    AnimatorInflater.loadAnimator(context, animationType) as ObjectAnimator
                flipAnimator.target = collapseIcon
                flipAnimator.duration = 200
                flipAnimator.start()
                callback(group)
            }
        }
    }
}
