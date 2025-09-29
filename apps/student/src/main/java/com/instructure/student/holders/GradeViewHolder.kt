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
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.accessibilityClassName
import com.instructure.pandautils.utils.color
import com.instructure.pandautils.utils.getGrade
import com.instructure.pandautils.utils.setGone
import com.instructure.pandautils.utils.setVisible
import com.instructure.student.R
import com.instructure.student.databinding.ViewholderGradeBinding
import com.instructure.student.dialog.WhatIfDialogStyled
import com.instructure.student.features.grades.GradesListRecyclerAdapter
import com.instructure.student.interfaces.AdapterToFragmentCallback
import com.instructure.student.util.BinderUtils

class GradeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(
        context: Context,
        canvasContext: CanvasContext?,
        assignment: Assignment,
        isEdit: Boolean,
        whatIfDialogCallback: WhatIfDialogStyled.WhatIfDialogCallback,
        adapterToFragmentCallback: AdapterToFragmentCallback<Assignment>,
        selectedItemCallback: GradesListRecyclerAdapter.SetSelectedItemCallback
    ): Unit = with(ViewholderGradeBinding.bind(itemView)) {
        root.setOnClickListener {
            adapterToFragmentCallback.onRowClicked(assignment, adapterPosition, true)
            selectedItemCallback.setSelected(adapterPosition)
        }

        title.text = assignment.name

        icon.setIcon(BinderUtils.getAssignmentIcon(assignment), canvasContext.color)
        icon.hideNestedIcon()

        points.setTextColor(ThemePrefs.brandColor)

        // Posted At now determines if an assignment is muted, even for old gradebook
        if (assignment.submission?.postedAt == null && !isEdit) {
            // Mute that score
            points.setGone()
        } else {
            val submission = assignment.submission
            val course = canvasContext as? Course
            val restrictQuantitativeData = course?.settings?.restrictQuantitativeData ?: false
            val gradingScheme = course?.gradingScheme ?: emptyList()
            if (submission != null && Const.PENDING_REVIEW == submission.workflowState) {
                points.setGone()
                icon.setNestedIcon(R.drawable.ic_complete_solid, canvasContext.color)
            } else if (restrictQuantitativeData && assignment.isGradingTypeQuantitative && submission?.excused != true && gradingScheme.isEmpty()) {
                points.setGone()
            } else {
                points.setVisible()
                val (grade, contentDescription) = assignment.getGrade(submission, context.resources, restrictQuantitativeData, gradingScheme)
                points.text = grade
                points.contentDescription = contentDescription
            }
        }


        // Configures whatIf editing boxes and listener for dialog
        edit.setVisible(isEdit)
        if (isEdit) {
            edit.setImageDrawable(ColorKeeper.getColoredDrawable(context, R.drawable.ic_edit, ContextCompat.getColor(context, R.color.textDarkest)))
            edit.setOnClickListener { whatIfDialogCallback.onClick(assignment, adapterPosition) }
        }

        if (assignment.dueAt != null) {
            date.text = DateHelper.getDayMonthDateString(context, assignment.dueDate!!)
        } else {
            date.text = context.getString(R.string.gradesNoDueDate)
        }
        date.setVisible(date.text.isNotBlank())

        if (assignment.isMissing() && !isEdit) {
            submissionState.text = context.getString(R.string.missingAssignment)
            submissionState.setTextColor(ContextCompat.getColor(context, R.color.textDanger))
            submissionState.setVisible()
        } else if (!assignment.isSubmitted && !isEdit) {
            submissionState.text = context.getString(R.string.notSubmitted)
            submissionState.setTextColor(ContextCompat.getColor(context, R.color.textDark))
            submissionState.setVisible()
        } else {
            submissionState.setGone()
        }

        root.accessibilityClassName("android.widget.Button")
    }

    companion object {
        const val HOLDER_RES_ID: Int = R.layout.viewholder_grade
    }
}
