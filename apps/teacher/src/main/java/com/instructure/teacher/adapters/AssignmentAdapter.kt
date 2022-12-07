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
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.AssignmentGroup
import com.instructure.pandarecycler.util.Types
import com.instructure.teacher.holders.AssignmentGroupHeaderViewHolder
import com.instructure.teacher.holders.AssignmentViewHolder
import com.instructure.teacher.presenters.AssignmentListPresenter
import com.instructure.teacher.viewinterface.AssignmentListView
import instructure.androidblueprint.SyncExpandableRecyclerAdapter

class AssignmentAdapter(context: Context, expandablePresenter: AssignmentListPresenter, private val iconColor: Int, private val mCallback: (Assignment) -> Unit) :
        SyncExpandableRecyclerAdapter<AssignmentGroup, Assignment, RecyclerView.ViewHolder, AssignmentListView>(context, expandablePresenter) {

    override fun createViewHolder(v: View, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType) {
            Types.TYPE_ITEM -> AssignmentViewHolder(v)
            else -> AssignmentGroupHeaderViewHolder(v)
        }
    }

    override fun itemLayoutResId(viewType: Int): Int {
        return when(viewType) {
            Types.TYPE_ITEM -> AssignmentViewHolder.HOLDER_RES_ID
            else -> AssignmentGroupHeaderViewHolder.HOLDER_RES_ID
        }
    }

    override fun onBindHeaderHolder(holder: RecyclerView.ViewHolder, group: AssignmentGroup, isExpanded: Boolean) {
        (holder as AssignmentGroupHeaderViewHolder).bind(group, isExpanded) {
            assignmentGroup -> expandCollapseGroup(assignmentGroup)
        }
    }

    override fun onBindChildHolder(holder: RecyclerView.ViewHolder, group: AssignmentGroup, item: Assignment) {
        context?.let { (holder as AssignmentViewHolder).bind(it, item, iconColor, mCallback) }
    }

}
