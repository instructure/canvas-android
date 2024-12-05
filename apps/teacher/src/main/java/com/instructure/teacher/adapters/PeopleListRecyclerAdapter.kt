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
import com.instructure.canvasapi2.models.User
import com.instructure.teacher.binders.UserBinder
import com.instructure.teacher.databinding.AdapterUsersBinding
import com.instructure.teacher.holders.UserViewHolder
import com.instructure.teacher.interfaces.AdapterToFragmentCallback
import com.instructure.teacher.presenters.PeopleListPresenter
import com.instructure.teacher.viewinterface.PeopleListView
import com.instructure.pandautils.blueprint.SyncRecyclerAdapter

class PeopleListRecyclerAdapter(
    context: Context, presenter: PeopleListPresenter, private val mCallback: AdapterToFragmentCallback<User>
) : SyncRecyclerAdapter<User, UserViewHolder, PeopleListView>(context, presenter) {

    override fun bindHolder(model: User, holder: UserViewHolder, position: Int) {
        UserBinder.bind(model, mCallback, position, holder)
    }

    override fun createViewHolder(binding: ViewBinding, viewType: Int) = UserViewHolder(binding as AdapterUsersBinding)

    override fun bindingInflater(viewType: Int): (LayoutInflater, ViewGroup, Boolean) -> ViewBinding = AdapterUsersBinding::inflate
}
