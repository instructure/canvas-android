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
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.GradeableStudentSubmission
import com.instructure.teacher.databinding.AdapterGradeableStudentSubmissionBinding
import com.instructure.teacher.holders.GradeableStudentSubmissionViewHolder
import com.instructure.teacher.features.assignment.submission.AssignmentSubmissionListPresenter
import com.instructure.teacher.viewinterface.AssignmentSubmissionListView
import com.instructure.pandautils.blueprint.SyncRecyclerAdapter

class GradeableStudentSubmissionAdapter(
    private val mAssignment: Assignment,
    private val mCourseId: Long,
    private val mContext: Context,
    private val presenter: AssignmentSubmissionListPresenter,
    val mCallback: (GradeableStudentSubmission) -> Unit
) : SyncRecyclerAdapter<GradeableStudentSubmission, GradeableStudentSubmissionViewHolder, AssignmentSubmissionListView>(mContext, presenter) {

    override fun bindHolder(model: GradeableStudentSubmission, holder: GradeableStudentSubmissionViewHolder, position: Int) {
        holder.bind(mContext, model, mAssignment, mCourseId, mCallback)
    }

    override fun createViewHolder(binding: ViewBinding, viewType: Int) = GradeableStudentSubmissionViewHolder(binding as AdapterGradeableStudentSubmissionBinding)

    override fun bindingInflater(viewType: Int): (LayoutInflater, ViewGroup, Boolean) -> ViewBinding = AdapterGradeableStudentSubmissionBinding::inflate
}
