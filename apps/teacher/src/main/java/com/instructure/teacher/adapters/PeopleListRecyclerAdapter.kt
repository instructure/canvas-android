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

import com.instructure.canvasapi2.models.User
import com.instructure.teacher.binders.UserBinder
import com.instructure.teacher.holders.UserViewHolder
import com.instructure.teacher.interfaces.AdapterToFragmentCallback

import instructure.androidblueprint.SyncPresenter
import instructure.androidblueprint.SyncRecyclerAdapter


class PeopleListRecyclerAdapter(context: Context, presenter: SyncPresenter<*, *>, private val mCallback: AdapterToFragmentCallback<User>) : SyncRecyclerAdapter<User, UserViewHolder>(context, presenter) {

    override fun bindHolder(user: User, holder: UserViewHolder, position: Int) {
        UserBinder.bind(user, mCallback, holder, position)
    }

    override fun createViewHolder(v: View, viewType: Int): UserViewHolder {
        return UserViewHolder(v)
    }

    override fun itemLayoutResId(viewType: Int): Int {
        return UserViewHolder.holderResId()
    }
}