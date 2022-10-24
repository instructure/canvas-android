/*
 * Copyright (C) 2016 - present Instructure, Inc.
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
package com.instructure.student.adapter

import android.content.Context
import android.view.View
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.MasteryPathAssignment
import com.instructure.student.holders.MasteryAssignmentViewHolder
import com.instructure.student.interfaces.AdapterToFragmentCallback

class MasteryPathOptionsRecyclerAdapter(
    context: Context,
    assignments: Array<MasteryPathAssignment>,
    private val iconColor: Int,
    private val mAdapterToFragmentCallback: AdapterToFragmentCallback<Assignment>
) : BaseListRecyclerAdapter<MasteryPathAssignment, MasteryAssignmentViewHolder>(
    context,
    MasteryPathAssignment::class.java
) {
    init {
        addAll(assignments)
        isRefresh = false
    }

    override fun createViewHolder(v: View, viewType: Int) = MasteryAssignmentViewHolder(v)

    override fun bindHolder(assignment: MasteryPathAssignment, holder: MasteryAssignmentViewHolder, position: Int) {
        holder.bind(context, assignment, iconColor, mAdapterToFragmentCallback)
    }

    override fun itemLayoutResId(viewType: Int) = MasteryAssignmentViewHolder.HOLDER_RES_ID

    override fun contextReady() {}
}
