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
import android.view.View
import com.instructure.canvasapi2.models.Tab
import com.instructure.teacher.holders.CourseBrowserViewHolder
import com.instructure.teacher.presenters.CourseBrowserPresenter
import com.instructure.teacher.viewinterface.CourseBrowserView
import instructure.androidblueprint.SyncRecyclerAdapter

class CourseBrowserAdapter(
    context: Context,
    presenter: CourseBrowserPresenter,
    val iconTint: Int,
    val mCallback: (Tab) -> Unit) : SyncRecyclerAdapter<Tab, CourseBrowserViewHolder, CourseBrowserView>(context, presenter) {

    override fun bindHolder(tab: Tab, holder: CourseBrowserViewHolder, position: Int) {
        context?.let {
            holder.bind(tab, mCallback)
        }
    }

    override fun createViewHolder(v: View, viewType: Int): CourseBrowserViewHolder = CourseBrowserViewHolder(v, iconTint)
    override fun itemLayoutResId(viewType: Int) = CourseBrowserViewHolder.HOLDER_RES_ID
}
