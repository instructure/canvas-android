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

package com.instructure.teacher.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.instructure.canvasapi2.models.CanvasComparable
import com.instructure.pandarecycler.util.Types
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.teacher.R
import com.instructure.teacher.databinding.AdapterAssigneeBinding
import com.instructure.teacher.databinding.AdapterAssigneeHeaderBinding
import com.instructure.teacher.holders.AssigneeItemViewHolder
import com.instructure.teacher.holders.AssigneeTypeViewHolder
import com.instructure.teacher.holders.AssigneeViewHolder
import com.instructure.teacher.models.AssigneeCategory
import com.instructure.teacher.presenters.AssigneeListPresenter
import com.instructure.teacher.viewinterface.AssigneeListView
import com.instructure.pandautils.blueprint.SyncExpandableRecyclerAdapter

class AssigneeListAdapter(
    context: Context, val presenter: AssigneeListPresenter
) : SyncExpandableRecyclerAdapter<AssigneeCategory, CanvasComparable<*>, AssigneeViewHolder, AssigneeListView>(context, presenter) {
    override fun onBindHeaderHolder(holder: RecyclerView.ViewHolder, group: AssigneeCategory, isExpanded: Boolean) {
        (holder as? AssigneeTypeViewHolder)?.bind(group)
    }

    override fun onBindChildHolder(holder: RecyclerView.ViewHolder, group: AssigneeCategory, item: CanvasComparable<*>) {
        (holder as? AssigneeItemViewHolder)?.bind(
            item,
            presenter,
            context?.getColor(R.color.backgroundInfo) ?: ThemePrefs.brandColor
        )
    }

    override fun bindingInflater(viewType: Int): (LayoutInflater, ViewGroup, Boolean) -> ViewBinding = when (viewType) {
        Types.TYPE_ITEM -> AdapterAssigneeBinding::inflate
        else -> AdapterAssigneeHeaderBinding::inflate
    }

    override fun createViewHolder(binding: ViewBinding, viewType: Int) = when (viewType) {
        Types.TYPE_ITEM -> AssigneeItemViewHolder(binding as AdapterAssigneeBinding)
        else -> AssigneeTypeViewHolder(binding as AdapterAssigneeHeaderBinding)
    }
}
