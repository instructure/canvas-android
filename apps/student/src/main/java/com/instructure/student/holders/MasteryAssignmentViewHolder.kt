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
package com.instructure.student.holders

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.MasteryPathAssignment
import com.instructure.pandautils.utils.ColorKeeper.getColoredDrawable
import com.instructure.student.R
import com.instructure.student.databinding.ViewholderMasteryPathsAssignmentBinding
import com.instructure.student.interfaces.AdapterToFragmentCallback
import com.instructure.student.util.BinderUtils

class MasteryAssignmentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun bind(
        context: Context,
        masteryPathAssignment: MasteryPathAssignment,
        iconColor: Int,
        adapterToFragmentCallback: AdapterToFragmentCallback<Assignment>
    ) = with(ViewholderMasteryPathsAssignmentBinding.bind(itemView)) {
        title.text = masteryPathAssignment.model!!.name
        val drawable = BinderUtils.getAssignmentIcon(masteryPathAssignment.model)
        icon.setImageDrawable(getColoredDrawable(context, drawable, iconColor))
        rootView.setOnClickListener {
            adapterToFragmentCallback.onRowClicked(masteryPathAssignment.model!!, 0, true)
        }
    }

    companion object {
        const val HOLDER_RES_ID: Int = R.layout.viewholder_mastery_paths_assignment
    }
}
