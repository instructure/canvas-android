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

package com.emeritus.student.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

import com.emeritus.student.R
import com.instructure.canvasapi2.models.Assignment

import java.util.ArrayList
import java.util.Date

class FileUploadAssignmentsAdapter(aContext: Context, val assignments: ArrayList<Assignment> = arrayListOf()) : ArrayAdapter<Assignment>(aContext, R.layout.spinner_row_courses, assignments) {

    ///////////////////////////////////////////////////////////////////////////
    // Adapter Overrides
    ///////////////////////////////////////////////////////////////////////////
    override fun getCount(): Int = this.assignments.size

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View = getCustomView(position, convertView)
    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View = getCustomView(position, convertView)

    fun getCustomView(position: Int, convertView: View?): View {
        var convertView = convertView
        val holder: AssignmentViewHolder?
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.spinner_row_courses, null)

            holder = AssignmentViewHolder()
            holder.assignmentName = convertView!!.findViewById<View>(R.id.courseName) as TextView

            convertView.tag = holder
        } else {
            holder = convertView.tag as AssignmentViewHolder
        }

        if (assignments[position] != null) {
            holder.assignmentName!!.text = assignments[position].name
        }

        return convertView
    }

    private class AssignmentViewHolder {
        internal var assignmentName: TextView? = null
    }

    ///////////////////////////////////////////////////////////////////////////
    // Helpers
    ///////////////////////////////////////////////////////////////////////////

    @Suppress("unused") // Used in AssignmentsTest.kt in SoSeedy
    fun setAssignments(assignments: ArrayList<Assignment>) {
        clearAssignments()
        this.assignments.addAll(getOnlineUploadAssignmentsList(context, assignments))
        notifyDataSetChanged()
    }

    private fun clearAssignments() {
        this.assignments.clear()
    }

    companion object {
        fun getOnlineUploadAssignmentsList(context: Context, newAssignments: List<Assignment>): ArrayList<Assignment> {
            val onlineUploadAssignments = ArrayList<Assignment>()
            val currentDate = Date()

            for (assignment in newAssignments) {
                val isUnlocked = (assignment.lockDate == null || assignment.lockDate != null && currentDate.before(assignment.lockDate)) && (assignment.unlockDate == null || assignment.unlockDate != null && currentDate.after(assignment.unlockDate))
                if (assignment.getSubmissionTypes().contains(Assignment.SubmissionType.ONLINE_UPLOAD) && isUnlocked) {
                    onlineUploadAssignments.add(assignment)
                }
            }

            // If empty, add no assignments assignment. Else add a selection prompt.
            if (onlineUploadAssignments.size == 0) {
                val noAssignments = Assignment(
                        id = Long.MIN_VALUE,
                        name = context.getString(R.string.noAssignmentsWithFileUpload)
                )

                onlineUploadAssignments.add(noAssignments)
            }

            return onlineUploadAssignments
        }
    }
}

