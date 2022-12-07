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
package com.instructure.teacher.adapters

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.pandarecycler.util.Types
import com.instructure.teacher.holders.DiscussionExpandableViewHolder
import com.instructure.teacher.holders.DiscussionListHolder
import com.instructure.teacher.holders.EmptyViewHolder
import com.instructure.teacher.presenters.DiscussionListPresenter
import com.instructure.teacher.viewinterface.DiscussionListView
import instructure.androidblueprint.SyncExpandableRecyclerAdapter

class DiscussionListAdapter(
    context: Context,
    expandablePresenter: DiscussionListPresenter,
    private val iconColor: Int,
    private val mIsAnnouncement: Boolean,
    private val mCallback: (DiscussionTopicHeader) -> Unit,
    private val mOverflowCallback: (String?, DiscussionTopicHeader) -> Unit
) : SyncExpandableRecyclerAdapter<String, DiscussionTopicHeader, RecyclerView.ViewHolder, DiscussionListView>(context, expandablePresenter) {

    init {
        setExpandedByDefault(true)
    }

    override fun createViewHolder(v: View, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            Types.TYPE_ITEM -> DiscussionListHolder(v)
            else -> if (mIsAnnouncement) EmptyViewHolder(v) else DiscussionExpandableViewHolder(v)
        }
    }

    override fun itemLayoutResId(viewType: Int): Int {
        return when (viewType) {
            Types.TYPE_ITEM -> DiscussionListHolder.HOLDER_RES_ID
            else -> if (mIsAnnouncement) EmptyViewHolder.HOLDER_RES_ID else DiscussionExpandableViewHolder.HOLDER_RES_ID
        }
    }

    override fun onBindHeaderHolder(holder: RecyclerView.ViewHolder, group: String, isExpanded: Boolean) {
        if(!mIsAnnouncement) {
            context?.let {
                (holder as DiscussionExpandableViewHolder).bind(isExpanded, holder, group) { discussionGroup ->
                    expandCollapseGroup(discussionGroup)
                }
            }
        }
    }

    override fun onBindChildHolder(holder: RecyclerView.ViewHolder, group: String, item: DiscussionTopicHeader) {
        context?.let { (holder as DiscussionListHolder).bind(it, item, group, iconColor, mIsAnnouncement, mCallback, mOverflowCallback) }
    }
}
