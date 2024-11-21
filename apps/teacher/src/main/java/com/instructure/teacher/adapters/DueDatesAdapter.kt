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
import androidx.viewbinding.ViewBinding
import com.instructure.teacher.databinding.AdapterAssignmentDueDateBinding
import com.instructure.teacher.holders.DueDateViewHolder
import com.instructure.teacher.models.DueDateGroup
import com.instructure.teacher.presenters.DueDatesPresenter
import com.instructure.teacher.viewinterface.DueDatesView
import com.instructure.pandautils.blueprint.SyncRecyclerAdapter

class DueDatesAdapter(context: Context, val presenter: DueDatesPresenter) :
    SyncRecyclerAdapter<DueDateGroup, DueDateViewHolder, DueDatesView>(context, presenter) {
    override fun bindingInflater(viewType: Int): (LayoutInflater, ViewGroup, Boolean) -> ViewBinding = AdapterAssignmentDueDateBinding::inflate

    override fun createViewHolder(binding: ViewBinding, viewType: Int) = DueDateViewHolder(binding as AdapterAssignmentDueDateBinding)

    override fun bindHolder(model: DueDateGroup, holder: DueDateViewHolder, position: Int) = holder.bind(
        model, list.size(), presenter.sectionMap, presenter.groupMap, presenter.studentMap
    )
}
