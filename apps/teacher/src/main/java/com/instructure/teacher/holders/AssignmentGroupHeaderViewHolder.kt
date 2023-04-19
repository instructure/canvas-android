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
package com.instructure.teacher.holders

import android.animation.AnimatorInflater
import android.animation.ObjectAnimator
import androidx.recyclerview.widget.RecyclerView
import com.instructure.canvasapi2.models.AssignmentGroup
import com.instructure.teacher.R
import com.instructure.teacher.databinding.AdapterAssignmentGroupHeaderBinding

class AssignmentGroupHeaderViewHolder(private val binding: AdapterAssignmentGroupHeaderBinding) : RecyclerView.ViewHolder(binding.root) {

    private var mIsExpanded = false

    fun bind(
        assignmentGroup: AssignmentGroup,
        isExpanded: Boolean,
        callback: (AssignmentGroup) -> Unit
    ) = with(binding) {

        mIsExpanded = isExpanded

        groupName.text = assignmentGroup.name

        assignmentGroupContainer.setOnClickListener {
            val animationType = if (mIsExpanded) R.animator.rotation_from_0_to_neg90 else R.animator.rotation_from_neg90_to_0
            mIsExpanded = !mIsExpanded
            val flipAnimator = AnimatorInflater.loadAnimator(root.context, animationType) as ObjectAnimator
            flipAnimator.target = collapseIcon
            flipAnimator.duration = 200
            flipAnimator.start()
            callback(assignmentGroup)
        }
    }
}

