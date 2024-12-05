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
import com.instructure.canvasapi2.models.Attendance
import com.instructure.teacher.databinding.AdapterAttendanceBinding
import com.instructure.teacher.holders.AttendanceViewHolder
import com.instructure.teacher.interfaces.AttendanceToFragmentCallback
import com.instructure.teacher.viewinterface.AttendanceListView
import com.instructure.pandautils.blueprint.SyncPresenter
import com.instructure.pandautils.blueprint.SyncRecyclerAdapter

class AttendanceListRecyclerAdapter(
    mContext: Context,
    presenter: SyncPresenter<Attendance, AttendanceListView>,
    private val mCallback: AttendanceToFragmentCallback<Attendance>
) : SyncRecyclerAdapter<Attendance, AttendanceViewHolder, AttendanceListView>(mContext, presenter) {

    override fun bindHolder(model: Attendance, holder: AttendanceViewHolder, position: Int) {
        holder.bind(model, mCallback, position, context!!)
    }

    override fun createViewHolder(binding: ViewBinding, viewType: Int) = AttendanceViewHolder(binding as AdapterAttendanceBinding)

    override fun bindingInflater(viewType: Int): (LayoutInflater, ViewGroup, Boolean) -> AdapterAttendanceBinding = AdapterAttendanceBinding::inflate
}
