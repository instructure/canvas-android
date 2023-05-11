/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
package com.emeritus.student.mobius.assignmentDetails.submissionDetails.drawer.rubric.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import com.emeritus.student.mobius.assignmentDetails.submissionDetails.drawer.rubric.*
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Submission
import com.instructure.pandautils.analytics.SCREEN_VIEW_SUBMISSION_RUBRIC
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.ParcelableArg
import com.emeritus.student.mobius.assignmentDetails.submissionDetails.drawer.rubric.*
import com.emeritus.student.mobius.assignmentDetails.submissionDetails.ui.SubmissionDetailsTabData
import com.emeritus.student.mobius.common.ui.MobiusFragment

@ScreenView(SCREEN_VIEW_SUBMISSION_RUBRIC)
class SubmissionRubricFragment :
    MobiusFragment<SubmissionRubricModel, SubmissionRubricEvent, SubmissionRubricEffect, SubmissionRubricView, SubmissionRubricViewState>() {
    private var submission by ParcelableArg<Submission>(key = Const.SUBMISSION)
    private var assignment by ParcelableArg<Assignment>(key = Const.ASSIGNMENT)

    override fun makeEffectHandler() = SubmissionRubricEffectHandler()

    override fun makeUpdate() = SubmissionRubricUpdate()

    override fun makeView(inflater: LayoutInflater, parent: ViewGroup) = SubmissionRubricView(inflater, parent)

    override fun makePresenter() = SubmissionRubricPresenter

    override fun makeInitModel() = SubmissionRubricModel(assignment, submission)

    companion object {
        fun newInstance(data: SubmissionDetailsTabData.RubricData) = SubmissionRubricFragment().apply {
            submission = data.submission
            assignment = data.assignment
        }
    }

}
