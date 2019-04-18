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
package com.instructure.student.mobius.assignmentDetails.submissionDetails.content.emptySubmission

import com.instructure.student.mobius.common.ui.UpdateInit
import com.spotify.mobius.First
import com.spotify.mobius.Next

class SubmissionDetailsEmptyUpdate : UpdateInit<SubmissionDetailsEmptyContentModel, SubmissionDetailsEmptyEvent, SubmissionDetailsEmptyEffect>() {
    override fun performInit(model: SubmissionDetailsEmptyContentModel): First<SubmissionDetailsEmptyContentModel, SubmissionDetailsEmptyEffect> {
        return First.first(model, setOf())
    }

    override fun update(model: SubmissionDetailsEmptyContentModel, event: SubmissionDetailsEmptyEvent): Next<SubmissionDetailsEmptyContentModel, SubmissionDetailsEmptyEffect> {
        return when(event) {
            SubmissionDetailsEmptyEvent.SubmitAssignmentClicked -> {
                // If a user is trying to submit something to an assignment and the assignment is null, something is terribly wrong
                val submissionTypes = model.assignment.getSubmissionTypes()
                if(submissionTypes.size == 1) {
                    Next.dispatch<SubmissionDetailsEmptyContentModel, SubmissionDetailsEmptyEffect>(setOf(SubmissionDetailsEmptyEffect.ShowCreateSubmissionView(submissionTypes.first(), model.course.id, model.assignment)))
                } else {
                    Next.dispatch<SubmissionDetailsEmptyContentModel, SubmissionDetailsEmptyEffect>(setOf(SubmissionDetailsEmptyEffect.ShowSubmitDialogView(model.assignment.id, model.course)))
                }
            }
        }
    }

}