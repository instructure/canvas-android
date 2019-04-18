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
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.ParcelableArg
import com.instructure.pandautils.utils.makeBundle
import com.instructure.pandautils.utils.withArgs
import com.instructure.student.mobius.assignmentDetails.submissionDetails.content.emptySubmission.*
import com.instructure.student.mobius.common.ui.MobiusFragment

class SubmissionDetailsEmptyFragment :
        MobiusFragment<SubmissionDetailsEmptyContentModel, SubmissionDetailsEmptyEvent, SubmissionDetailsEmptyEffect, SubmissionDetailsEmptyView, SubmissionDetailsEmptyContentViewState>() {

    val canvasContext by ParcelableArg<Course>(key = Const.CANVAS_CONTEXT)
    val assignment by ParcelableArg<Assignment>(key = Const.ASSIGNMENT)

    override fun makeEffectHandler() = SubmissionDetailsEmptyEffectHandler()
    override fun makeUpdate() = SubmissionDetailsEmptyUpdate()
    override fun makeView(inflater: LayoutInflater, parent: ViewGroup) = SubmissionDetailsEmptyView(inflater, parent)
    override fun makePresenter() = SubmissionDetailsEmptyContentPresenter()
    override fun makeInitModel() = SubmissionDetailsEmptyContentModel(assignment, canvasContext)

    companion object {
        @JvmStatic
        fun newInstance(course: Course, assignment: Assignment): SubmissionDetailsEmptyFragment {
            val bundle = course.makeBundle {
                putParcelable(Const.ASSIGNMENT, assignment)
            }

            return SubmissionDetailsEmptyFragment().withArgs(bundle)
        }
    }
}