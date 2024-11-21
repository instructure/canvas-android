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
import com.instructure.canvasapi2.models.Quiz
import com.instructure.pandarecycler.util.Types
import com.instructure.teacher.databinding.AdapterQuizBinding
import com.instructure.teacher.databinding.ViewholderHeaderExpandableBinding
import com.instructure.teacher.holders.QuizExpandableViewHolder
import com.instructure.teacher.holders.QuizViewHolder
import com.instructure.teacher.presenters.QuizListPresenter
import com.instructure.teacher.viewinterface.QuizListView
import com.instructure.pandautils.blueprint.SyncExpandableRecyclerAdapter

class QuizListAdapter(
    context: Context,
    expandablePresenter: QuizListPresenter,
    private val iconColor: Int,
    private val mCallback: (Quiz) -> Unit
) : SyncExpandableRecyclerAdapter<String, Quiz, RecyclerView.ViewHolder, QuizListView>(context, expandablePresenter) {

    override fun createViewHolder(binding: ViewBinding, viewType: Int) = when (viewType) {
        Types.TYPE_ITEM -> QuizViewHolder(binding as AdapterQuizBinding)
        else -> QuizExpandableViewHolder(binding as ViewholderHeaderExpandableBinding)
    }

    override fun bindingInflater(viewType: Int): (LayoutInflater, ViewGroup, Boolean) -> ViewBinding = when (viewType) {
        Types.TYPE_ITEM -> AdapterQuizBinding::inflate
        else -> ViewholderHeaderExpandableBinding::inflate
    }

    override fun onBindHeaderHolder(holder: RecyclerView.ViewHolder, group: String, isExpanded: Boolean) {
        context?.let {
            (holder as QuizExpandableViewHolder).bind(
                it,
                isExpanded,
                holder,
                group
            ) { assignmentGroup -> expandCollapseGroup(assignmentGroup) }
        }
    }

    override fun onBindChildHolder(holder: RecyclerView.ViewHolder, group: String, item: Quiz) {
        context?.let { (holder as QuizViewHolder).bind(it, item, iconColor, mCallback) }
    }
}
