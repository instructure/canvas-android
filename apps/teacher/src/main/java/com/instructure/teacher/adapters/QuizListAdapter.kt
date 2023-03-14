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
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import com.instructure.canvasapi2.models.Quiz
import com.instructure.pandarecycler.util.Types
import com.instructure.teacher.holders.QuizExpandableViewHolder
import com.instructure.teacher.holders.QuizViewHolder
import com.instructure.teacher.presenters.QuizListPresenter
import com.instructure.teacher.viewinterface.QuizListView
import instructure.androidblueprint.SyncExpandableRecyclerAdapter

class QuizListAdapter(context: Context,
                      expandablePresenter: QuizListPresenter,
                      private val iconColor: Int,
                      private val mCallback: (Quiz) -> Unit) :
        SyncExpandableRecyclerAdapter<String, Quiz, RecyclerView.ViewHolder, QuizListView>(context, expandablePresenter) {

    override fun createViewHolder(v: View, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            Types.TYPE_ITEM -> return QuizViewHolder(v)
            else -> return QuizExpandableViewHolder(v)
        }
    }

    override fun itemLayoutResId(viewType: Int): Int {
        when (viewType) {
            Types.TYPE_ITEM -> return QuizViewHolder.HOLDER_RES_ID
            else -> return QuizExpandableViewHolder.HOLDER_RES_ID
        }
    }

    override fun onBindHeaderHolder(holder: RecyclerView.ViewHolder, group: String, isExpanded: Boolean) {
        context?.let {
            (holder as QuizExpandableViewHolder).bind(it, isExpanded, holder, group, {
                assignmentGroup ->
                expandCollapseGroup(assignmentGroup)
            })
        }
    }

    override fun onBindChildHolder(holder: RecyclerView.ViewHolder, group: String, item: Quiz) {
        context?.let { (holder as QuizViewHolder).bind(it, item, iconColor, mCallback) }
    }
}
