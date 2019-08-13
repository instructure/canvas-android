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
import com.instructure.pandautils.utils.*
import com.instructure.student.mobius.assignmentDetails.submissionDetails.SubmissionDetailsEmptyContentEventBusSource
import com.instructure.student.mobius.assignmentDetails.submissionDetails.content.emptySubmission.*
import com.instructure.student.mobius.common.ui.MobiusFragment

class SubmissionDetailsEmptyContentFragment :
        MobiusFragment<SubmissionDetailsEmptyContentModel, SubmissionDetailsEmptyContentEvent, SubmissionDetailsEmptyContentEffect, SubmissionDetailsEmptyContentView, SubmissionDetailsEmptyContentViewState>() {

    val assignment by ParcelableArg<Assignment>(key = Const.ASSIGNMENT)
    val isStudioEnabled by BooleanArg(key = Const.IS_STUDIO_ENABLED)
    val canvasContext by ParcelableArg<Course>(key = Const.CANVAS_CONTEXT)
    val quiz by NullableParcelableArg<Quiz>(key = Const.QUIZ)
    val studioLTITool by NullableParcelableArg<LTITool>(key = Const.STUDIO_LTI_TOOL)

    override fun makeEffectHandler() = SubmissionDetailsEmptyContentEffectHandler(requireContext(), assignment.id)
    override fun makeUpdate() = SubmissionDetailsEmptyContentUpdate()
    override fun makeView(inflater: LayoutInflater, parent: ViewGroup) = SubmissionDetailsEmptyContentView(canvasContext, inflater, parent)
    override fun makePresenter() = SubmissionDetailsEmptyContentPresenter
    override fun makeInitModel() = SubmissionDetailsEmptyContentModel(assignment, canvasContext, isStudioEnabled, quiz, studioLTITool = studioLTITool)
    override fun getExternalEventSources() = listOf(SubmissionDetailsEmptyContentEventBusSource())

    companion object {
        const val VIDEO_REQUEST_CODE = 45520
        const val CHOOSE_MEDIA_REQUEST_CODE = 45521

        @JvmStatic
        fun newInstance(course: Course, assignment: Assignment, isStudioEnabled: Boolean, quiz: Quiz? = null, studioLTITool: LTITool? = null): SubmissionDetailsEmptyContentFragment {
            val bundle = course.makeBundle {
                putParcelable(Const.ASSIGNMENT, assignment)
                putParcelable(Const.QUIZ, quiz)
                putBoolean(Const.IS_STUDIO_ENABLED, isStudioEnabled)
                putParcelable(Const.STUDIO_LTI_TOOL, studioLTITool)
            }

            return SubmissionDetailsEmptyContentFragment().withArgs(bundle)
        }
    }
}