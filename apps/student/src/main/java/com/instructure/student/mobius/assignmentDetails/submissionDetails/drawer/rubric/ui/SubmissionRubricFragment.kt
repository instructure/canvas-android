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
import com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.rubric.SubmissionRubricEffect
import com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.rubric.SubmissionRubricEvent
import com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.rubric.SubmissionRubricModel
import com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.rubric.SubmissionRubricViewState
import com.instructure.student.mobius.common.ui.EffectHandler
import com.instructure.student.mobius.common.ui.MobiusFragment
import com.instructure.student.mobius.common.ui.Presenter
import com.instructure.student.mobius.common.ui.UpdateInit

class SubmissionRubricFragment :
    MobiusFragment<SubmissionRubricModel, SubmissionRubricEvent, SubmissionRubricEffect, SubmissionRubricView, SubmissionRubricViewState>() {
    override fun makeEffectHandler(): EffectHandler<SubmissionRubricView, SubmissionRubricEvent, SubmissionRubricEffect> {
        TODO()
    }

    override fun makeUpdate(): UpdateInit<SubmissionRubricModel, SubmissionRubricEvent, SubmissionRubricEffect> {
        TODO()
    }

    override fun makeView(inflater: LayoutInflater, parent: ViewGroup): SubmissionRubricView {
        TODO()
    }

    override fun makePresenter(): Presenter<SubmissionRubricModel, SubmissionRubricViewState> {
        TODO()
    }

    override fun makeInitModel(): SubmissionRubricModel {
        TODO()
    }
}
