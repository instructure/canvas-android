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
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.AssignmentGroup
import com.instructure.pandarecycler.util.Types
import com.instructure.teacher.databinding.AdapterAssignmentBinding
import com.instructure.teacher.databinding.AdapterAssignmentGroupHeaderBinding
import com.instructure.teacher.holders.AssignmentGroupHeaderViewHolder
import com.instructure.teacher.holders.AssignmentViewHolder
import com.instructure.teacher.features.assignment.list.AssignmentListPresenter
import com.instructure.teacher.viewinterface.AssignmentListView
import com.instructure.pandautils.blueprint.SyncExpandableRecyclerAdapter

class AssignmentAdapter(
    context: Context,
    expandablePresenter: AssignmentListPresenter,
    private val iconColor: Int,
    private val mCallback: (Assignment) -> Unit
) : SyncExpandableRecyclerAdapter<AssignmentGroup, Assignment, RecyclerView.ViewHolder, AssignmentListView>(context, expandablePresenter) {

    override fun createViewHolder(binding: ViewBinding, viewType: Int) = when (viewType) {
        Types.TYPE_ITEM -> AssignmentViewHolder(binding as AdapterAssignmentBinding)
        else -> AssignmentGroupHeaderViewHolder(binding as AdapterAssignmentGroupHeaderBinding)
    }

    override fun bindingInflater(viewType: Int): (LayoutInflater, ViewGroup, Boolean) -> ViewBinding = when (viewType) {
        Types.TYPE_ITEM -> AdapterAssignmentBinding::inflate
        else -> AdapterAssignmentGroupHeaderBinding::inflate
    }

    override fun onBindHeaderHolder(holder: RecyclerView.ViewHolder, group: AssignmentGroup, isExpanded: Boolean) {
        (holder as AssignmentGroupHeaderViewHolder).bind(
            group,
            isExpanded
        ) { assignmentGroup -> expandCollapseGroup(assignmentGroup) }
    }

    override fun onBindChildHolder(holder: RecyclerView.ViewHolder, group: AssignmentGroup, item: Assignment) {
        context?.let { (holder as AssignmentViewHolder).bind(it, item, iconColor, mCallback) }
    }
}
