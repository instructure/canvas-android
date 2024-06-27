/*
 * Copyright (C) 2019 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package com.instructure.student.mobius.assignmentDetails.submissionDetails.content.emptySubmission.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.LTITool
import com.instructure.canvasapi2.models.Quiz
import com.instructure.pandautils.analytics.SCREEN_VIEW_SUBMISSION_DETAILS_EMPTY_CONTENT
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.utils.BooleanArg
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.NullableParcelableArg
import com.instructure.pandautils.utils.ParcelableArg
import com.instructure.student.databinding.FragmentSubmissionDetailsEmptyContentBinding
import com.instructure.student.mobius.assignmentDetails.submissionDetails.content.emptySubmission.SubmissionDetailsEmptyContentEffect
import com.instructure.student.mobius.assignmentDetails.submissionDetails.content.emptySubmission.SubmissionDetailsEmptyContentEvent
import com.instructure.student.mobius.assignmentDetails.submissionDetails.content.emptySubmission.SubmissionDetailsEmptyContentModel
import com.instructure.student.mobius.assignmentDetails.submissionDetails.content.emptySubmission.SubmissionDetailsEmptyContentPresenter
import com.instructure.student.mobius.assignmentDetails.submissionDetails.content.emptySubmission.SubmissionDetailsEmptyContentUpdate
import com.instructure.student.mobius.common.ui.MobiusFragment

@ScreenView(SCREEN_VIEW_SUBMISSION_DETAILS_EMPTY_CONTENT)
abstract class BaseSubmissionDetailsEmptyContentFragment :
        MobiusFragment<SubmissionDetailsEmptyContentModel, SubmissionDetailsEmptyContentEvent, SubmissionDetailsEmptyContentEffect, SubmissionDetailsEmptyContentView, SubmissionDetailsEmptyContentViewState, FragmentSubmissionDetailsEmptyContentBinding>() {

    val assignment by ParcelableArg<Assignment>(key = Const.ASSIGNMENT)
    val isStudioEnabled by BooleanArg(key = Const.IS_STUDIO_ENABLED)
    val canvasContext by ParcelableArg<Course>(key = Const.CANVAS_CONTEXT)
    val quiz by NullableParcelableArg<Quiz>(key = Const.QUIZ)
    val studioLTITool by NullableParcelableArg<LTITool>(key = Const.STUDIO_LTI_TOOL)
    val isObserver by BooleanArg(key = Const.IS_OBSERVER, default = false)
    private var ltiTool: LTITool? by NullableParcelableArg(key = Const.LTI_TOOL, default = null)

    override fun makeUpdate() = SubmissionDetailsEmptyContentUpdate()
    override fun makeView(inflater: LayoutInflater, parent: ViewGroup) = SubmissionDetailsEmptyContentView(canvasContext, inflater, parent)
    override fun makePresenter() = SubmissionDetailsEmptyContentPresenter
    override fun makeInitModel() = SubmissionDetailsEmptyContentModel(assignment, canvasContext, isStudioEnabled, quiz, studioLTITool = studioLTITool, isObserver = isObserver, ltiTool = ltiTool)
}
