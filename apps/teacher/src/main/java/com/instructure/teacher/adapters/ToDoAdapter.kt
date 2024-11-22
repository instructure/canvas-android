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
import androidx.viewbinding.ViewBinding
import com.instructure.canvasapi2.models.ToDo
import com.instructure.teacher.databinding.AdapterTodoBinding
import com.instructure.teacher.holders.ToDoViewHolder
import com.instructure.teacher.interfaces.AdapterToFragmentCallback
import com.instructure.teacher.presenters.ToDoPresenter
import com.instructure.teacher.viewinterface.ToDoView
import com.instructure.pandautils.blueprint.SyncRecyclerAdapter

class ToDoAdapter(
    context: Context,
    presenter: ToDoPresenter,
    private val mCallback: AdapterToFragmentCallback<ToDo>
) :
    SyncRecyclerAdapter<ToDo, ToDoViewHolder, ToDoView>(context, presenter) {

    override fun createViewHolder(binding: ViewBinding, viewType: Int) = ToDoViewHolder(binding as AdapterTodoBinding)

    override fun bindingInflater(viewType: Int): (LayoutInflater, ViewGroup, Boolean) -> ViewBinding = AdapterTodoBinding::inflate

    override fun bindHolder(model: ToDo, holder: ToDoViewHolder, position: Int) {
        context?.let { holder.bind(it, model, mCallback, position) }
    }
}
