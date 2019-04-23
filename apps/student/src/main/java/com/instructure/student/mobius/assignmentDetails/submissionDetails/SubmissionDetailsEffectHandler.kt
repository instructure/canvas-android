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

package com.instructure.student.mobius.assignmentDetails.submissionDetails

import com.instructure.canvasapi2.managers.AssignmentManager
import com.instructure.canvasapi2.managers.SubmissionManager
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.student.mobius.assignmentDetails.submissionDetails.ui.SubmissionDetailsView
import com.instructure.student.mobius.common.ui.EffectHandler
import com.instructure.student.util.isArcEnabled
import kotlinx.coroutines.launch

class SubmissionDetailsEffectHandler : EffectHandler<SubmissionDetailsView, SubmissionDetailsEvent, SubmissionDetailsEffect>() {
    override fun accept(effect: SubmissionDetailsEffect) {
        when (effect) {
            is SubmissionDetailsEffect.LoadData -> loadData(effect)
            is SubmissionDetailsEffect.ShowSubmissionContentType -> {
                view?.showSubmissionContent(effect.submissionContentType)
            }
        }
    }

    private fun loadData(effect: SubmissionDetailsEffect.LoadData) {
        launch {
            val submission = SubmissionManager.getSingleSubmissionAsync(effect.courseId, effect.assignmentId, ApiPrefs.user!!.id, true)
            val assignment = AssignmentManager.getAssignmentAsync(effect.assignmentId, effect.courseId, true).await()

            // We need to know if they can make submissions through arc, only for file uploads - This is for empty submissions
            val isArcEnabled = if (assignment.isSuccess && assignment.dataOrThrow.getSubmissionTypes().contains(Assignment.SubmissionType.ONLINE_UPLOAD)) {
                effect.courseId.isArcEnabled()
            } else false

            consumer.accept(SubmissionDetailsEvent.DataLoaded(assignment, submission.await(), isArcEnabled))
        }
    }
}
