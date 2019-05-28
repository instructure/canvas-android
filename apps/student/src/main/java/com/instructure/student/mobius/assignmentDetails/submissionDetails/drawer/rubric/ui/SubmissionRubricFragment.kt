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
package com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.rubric.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.rubric.*
import com.instructure.student.mobius.assignmentDetails.submissionDetails.ui.SubmissionDetailsTabData
import com.instructure.student.mobius.common.ui.MobiusFragment

class SubmissionRubricFragment :
    MobiusFragment<SubmissionRubricModel, SubmissionRubricEvent, SubmissionRubricEffect, SubmissionRubricView, SubmissionRubricViewState>() {
    lateinit var data: SubmissionDetailsTabData.RubricData

    override fun makeEffectHandler() = SubmissionRubricEffectHandler()

    override fun makeUpdate() = SubmissionRubricUpdate()

    override fun makeView(inflater: LayoutInflater, parent: ViewGroup) = SubmissionRubricView(inflater, parent)

    override fun makePresenter() = SubmissionRubricPresenter

    override fun makeInitModel() = SubmissionRubricModel(data.assignment, data.submission)
}
