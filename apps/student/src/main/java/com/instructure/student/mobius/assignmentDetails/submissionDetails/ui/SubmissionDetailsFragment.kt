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
package com.instructure.student.mobius.assignmentDetails.submissionDetails.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.utils.pageview.PageViewUrlParam
import com.instructure.pandautils.utils.BooleanArg
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.LongArg
import com.instructure.pandautils.utils.ParcelableArg
import com.instructure.student.databinding.FragmentSubmissionDetailsBinding
import com.instructure.student.mobius.assignmentDetails.submissionDetails.SubmissionDetailsEffect
import com.instructure.student.mobius.assignmentDetails.submissionDetails.SubmissionDetailsEffectHandler
import com.instructure.student.mobius.assignmentDetails.submissionDetails.SubmissionDetailsEvent
import com.instructure.student.mobius.assignmentDetails.submissionDetails.SubmissionDetailsModel
import com.instructure.student.mobius.assignmentDetails.submissionDetails.SubmissionDetailsPresenter
import com.instructure.student.mobius.assignmentDetails.submissionDetails.SubmissionDetailsRepository
import com.instructure.student.mobius.assignmentDetails.submissionDetails.SubmissionDetailsUpdate
import com.instructure.student.mobius.common.ui.MobiusFragment

abstract class SubmissionDetailsFragment : MobiusFragment<SubmissionDetailsModel, SubmissionDetailsEvent,
        SubmissionDetailsEffect, SubmissionDetailsView, SubmissionDetailsViewState, FragmentSubmissionDetailsBinding>() {

    @get:PageViewUrlParam("canvasContext")
    val canvasContext by ParcelableArg<Course>(key = Const.CANVAS_CONTEXT)

    @get:PageViewUrlParam(name = "assignmentId")
    val assignmentId by LongArg(key = Const.ASSIGNMENT_ID)
    val isObserver by BooleanArg(key = Const.IS_OBSERVER, default = false)
    private val initialSelectedSubmissionAttempt by LongArg(key = Const.SUBMISSION_ATTEMPT)

    override fun makeEffectHandler() = SubmissionDetailsEffectHandler(getRepository())

    override fun makeUpdate() = SubmissionDetailsUpdate()

    override fun makeView(inflater: LayoutInflater, parent: ViewGroup) =
        SubmissionDetailsView(inflater, parent, canvasContext, childFragmentManager)

    override fun makePresenter() = SubmissionDetailsPresenter

    override fun makeInitModel() = SubmissionDetailsModel(
        canvasContext = canvasContext,
        assignmentId = assignmentId,
        isObserver = isObserver,
        initialSelectedSubmissionAttempt = initialSelectedSubmissionAttempt
    )

    abstract fun getRepository(): SubmissionDetailsRepository
}
