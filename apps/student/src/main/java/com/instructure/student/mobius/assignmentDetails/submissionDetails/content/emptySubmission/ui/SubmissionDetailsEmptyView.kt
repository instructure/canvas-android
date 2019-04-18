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

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import com.instructure.canvasapi2.models.Course
import com.instructure.pandautils.utils.*
import com.instructure.student.R
import com.instructure.student.mobius.assignmentDetails.submissionDetails.content.emptySubmission.SubmissionDetailsEmptyEvent
import com.instructure.student.mobius.assignmentDetails.submissionDetails.content.emptySubmission.ui.SubmissionDetailsEmptyContentViewState.Loaded
import com.instructure.student.mobius.common.ui.MobiusView
import com.spotify.mobius.functions.Consumer
import kotlinx.android.synthetic.main.fragment_submission_details_empty.*

class SubmissionDetailsEmptyView(
        inflater: LayoutInflater,
        parent: ViewGroup
) : MobiusView<SubmissionDetailsEmptyContentViewState, SubmissionDetailsEmptyEvent>(
        R.layout.fragment_submission_details_empty,
        inflater,
        parent
) {

    init {
        submitButton.backgroundTintList = ColorStateList.valueOf(ThemePrefs.buttonColor)
        submitButton.setTextColor(ThemePrefs.buttonTextColor)
    }

    override fun onConnect(output: Consumer<SubmissionDetailsEmptyEvent>) {
        submitButton.onClick { output.accept(SubmissionDetailsEmptyEvent.SubmitAssignmentClicked) }
    }

    override fun render(state: SubmissionDetailsEmptyContentViewState) {
        when(state) {
            is Loaded -> {
                title.text = state.assignmentLocked ?: context.getString(R.string.submissionDetailsNoSubmissionYet)
                message.text = state.dueDate
                submitButton.setHidden(state.isAllowedToSubmit)
            }
        }
    }

    override fun onDispose() {}
    override fun applyTheme() {

    }


    fun showSubmitDialogView(assignmentId: Long, course: Course) {
        // TODO
        context.toast("Route to submission workflow")
    }
}