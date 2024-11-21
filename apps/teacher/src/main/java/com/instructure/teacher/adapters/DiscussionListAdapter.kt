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
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.pandarecycler.util.Types
import com.instructure.teacher.databinding.AdapterDiscussionBinding
import com.instructure.teacher.databinding.AdapterEmptyBinding
import com.instructure.teacher.databinding.ViewholderHeaderExpandableBinding
import com.instructure.teacher.holders.DiscussionExpandableViewHolder
import com.instructure.teacher.holders.DiscussionListHolder
import com.instructure.teacher.holders.EmptyViewHolder
import com.instructure.teacher.presenters.DiscussionListPresenter
import com.instructure.teacher.viewinterface.DiscussionListView
import com.instructure.pandautils.blueprint.SyncExpandableRecyclerAdapter

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

    override fun createViewHolder(binding: ViewBinding, viewType: Int) = when (viewType) {
        Types.TYPE_ITEM -> DiscussionListHolder(binding as AdapterDiscussionBinding)
        else -> if (mIsAnnouncement) EmptyViewHolder(binding.root) else DiscussionExpandableViewHolder(binding as ViewholderHeaderExpandableBinding)
    }

    override fun bindingInflater(viewType: Int): (LayoutInflater, ViewGroup, Boolean) -> ViewBinding = when (viewType) {
        Types.TYPE_ITEM -> AdapterDiscussionBinding::inflate
        else -> if (mIsAnnouncement) AdapterEmptyBinding::inflate else ViewholderHeaderExpandableBinding::inflate
    }

    override fun onBindHeaderHolder(holder: RecyclerView.ViewHolder, group: String, isExpanded: Boolean) {
        if (!mIsAnnouncement) {
            context?.let {
                (holder as DiscussionExpandableViewHolder).bind(
                    isExpanded,
                    holder,
                    group
                ) { discussionGroup -> expandCollapseGroup(discussionGroup) }
            }
        }
    }

    override fun onBindChildHolder(holder: RecyclerView.ViewHolder, group: String, item: DiscussionTopicHeader) {
        context?.let {
            (holder as DiscussionListHolder).bind(
                it,
                item,
                group,
                iconColor,
                mIsAnnouncement,
                mCallback,
                mOverflowCallback
            )
        }
    }
}
