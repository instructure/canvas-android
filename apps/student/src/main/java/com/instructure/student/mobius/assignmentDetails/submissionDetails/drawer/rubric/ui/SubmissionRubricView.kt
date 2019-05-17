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
import com.instructure.student.R
import com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.rubric.SubmissionRubricEvent
import com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.rubric.SubmissionRubricViewState
import com.instructure.student.mobius.common.ui.MobiusView
import com.spotify.mobius.functions.Consumer

class SubmissionRubricView(
    inflater: LayoutInflater,
    parent: ViewGroup
) : MobiusView<SubmissionRubricViewState, SubmissionRubricEvent>(
    R.layout.fragment_submission_rubric,
    inflater,
    parent
) {
    override fun onConnect(output: Consumer<SubmissionRubricEvent>) {
        TODO()
    }

    override fun render(state: SubmissionRubricViewState) {
        TODO()
    }

    override fun onDispose() {
        TODO()
    }

    override fun applyTheme() {
        TODO()
    }

    fun displayLongDescription(description: String) {
        TODO()
    }
}
