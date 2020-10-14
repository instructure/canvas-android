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
import com.instructure.canvasapi2.models.Attendance

import com.instructure.teacher.holders.AttendanceViewHolder
import com.instructure.teacher.interfaces.AttendanceToFragmentCallback
import com.instructure.teacher.viewinterface.AttendanceListView

import instructure.androidblueprint.SyncPresenter
import instructure.androidblueprint.SyncRecyclerAdapter

class AttendanceListRecyclerAdapter(
        mContext: Context,
        presenter: SyncPresenter<Attendance, AttendanceListView>,
        private val mCallback: AttendanceToFragmentCallback<Attendance>
) : SyncRecyclerAdapter<Attendance, AttendanceViewHolder, AttendanceListView>(mContext, presenter) {

    override fun bindHolder(attendance: Attendance, holder: AttendanceViewHolder, position: Int) {
        holder.bind(attendance, mCallback, position)
    }

    override fun createViewHolder(v: View, viewType: Int): AttendanceViewHolder {
        return AttendanceViewHolder(v)
    }

    override fun itemLayoutResId(viewType: Int): Int {
        return AttendanceViewHolder.HOLDER_RES_ID
    }
}
